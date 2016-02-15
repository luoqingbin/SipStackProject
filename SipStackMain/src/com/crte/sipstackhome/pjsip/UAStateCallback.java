package com.crte.sipstackhome.pjsip;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.util.SparseArray;

import com.crte.sipstackhome.api.SipCallSession;
import com.crte.sipstackhome.api.SipConfigManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.api.SipProfileState;
import com.crte.sipstackhome.api.SipUri;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.models.CallState;
import com.crte.sipstackhome.service.SipCallSessionImpl;
import com.crte.sipstackhome.service.SipService;
import com.crte.sipstackhome.ui.login.AccountNotification;
import com.crte.sipstackhome.utils.log.Log;
import com.crte.sipstackhome.utils.log.LogUtils;

import org.pjsip.pjsua.Callback;
import org.pjsip.pjsua.SWIGTYPE_p_int;
import org.pjsip.pjsua.SWIGTYPE_p_p_pjmedia_port;
import org.pjsip.pjsua.SWIGTYPE_p_pjmedia_sdp_session;
import org.pjsip.pjsua.SWIGTYPE_p_pjmedia_stream;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_rx_data;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_status_code;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_transaction;
import org.pjsip.pjsua.SWIGTYPE_p_pjsip_tx_data;
import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pj_stun_nat_detect_result;
import org.pjsip.pjsua.pjsip_event;
import org.pjsip.pjsua.pjsip_redirect_op;
import org.pjsip.pjsua.pjsip_status_code;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_info;
import org.pjsip.pjsua.pjsua_med_tp_state_info;

import java.util.ArrayList;
import java.util.List;

/**
 * 各种状态的回调方法
 */
public class UAStateCallback extends Callback {
    private static final String TAG = "UAStateCallback";
    private static final int ON_CALL_STATE = 2;
    private static final int ON_MEDIA_STATE = 3;

    private UserAgentBroadcastReceiver mUserAgentBroadcastReceiver;
    private SipService mSipService = null;
    private WorkerHandler mWorkerHandler = new WorkerHandler();
    private PowerManager.WakeLock ongoingCallLock;
    private PowerManager.WakeLock eventLock;

    /**
     * 保存当前通话列表的集合
     */
    private SparseArray<SipCallSessionImpl> mCallsList = new SparseArray<SipCallSessionImpl>();

    private int mEventLockCount = 0;

    public UAStateCallback(UserAgentBroadcastReceiver userAgentBroadcastReceiver, SipService sipService) {
        this.mUserAgentBroadcastReceiver = userAgentBroadcastReceiver;
        this.mSipService = sipService;
    }

    /**
     * 呼叫状态标识
     */
    int callState = 0;

    private void lockCpu() {
        if (eventLock != null) {
            Log.d(TAG, "< LOCK CPU");
            eventLock.acquire();
            mEventLockCount++;
        }
    }

    private void unlockCpu() {
        if (eventLock != null && eventLock.isHeld()) {
            eventLock.release();
            mEventLockCount--;
            Log.d(TAG, "> UNLOCK CPU " + mEventLockCount);
        }
    }

    /**
     * 当呼叫的状态发生变化时执行回调
     *
     * @param call_id 呼叫索引
     * @param e       导致呼叫改变的状态
     */
    @Override
    public void on_call_state(int call_id, pjsip_event e) {
        pjsua.css_on_call_state(call_id, e);
        lockCpu();
        try {
            final SipCallSession callInfo = updateCallInfoFromStack(call_id, e);

            if (isMultiCall(call_id)) {
                pjsua.call_hangup(call_id, SipCallSession.StatusCode.BUSY_HERE, null, null);
                unlockCpu();
                return;
            }

            mWorkerHandler.sendMessage(mWorkerHandler.obtainMessage(ON_CALL_STATE, callInfo));
        } catch (SameThreadException e1) {
            e1.printStackTrace();
        } finally {
            unlockCpu();
        }
    }

    /**
     * 来电回调<br/>
     * 当程序监听到新来电信息时会执行此方法。<br/>
     * <li/>向服务器发送180振铃请求
     * <li/>直接发送200 ok实现自动应答功能
     * <li/>判断当前用户是否已有一个来电呼叫
     *
     * @param acc_id  发起呼叫的账户ID
     * @param call_id 目标呼叫ID
     * @param rdata   INVITE请求
     */
    @Override
    public void on_incoming_call(int acc_id, int call_id, SWIGTYPE_p_pjsip_rx_data rdata) {
        super.on_incoming_call(acc_id, call_id, rdata);
        lockCpu();

        if (isMultiCall(call_id)) {
            pjsua.call_hangup(call_id, SipCallSession.StatusCode.BUSY_HERE, null, null);
            unlockCpu();
            return;
        }

        try {
            mUserAgentBroadcastReceiver.mCallState = true;
            SipCallSessionImpl callInfo = updateCallInfoFromStack(call_id, null);
            callInfo.setIncoming(true);
            // 是否开启自动应答
            if (mUserAgentBroadcastReceiver.mPreferencesWrapper.getPreferenceBooleanValue(SipConfigManager.SUPPORT_MULTIPLE_CALLS)) {
                UserAgentBroadcastReceiver.getUserAgent().callAnswer(call_id, SipCallSession.StatusCode.OK, true);
            }
            // 正常呼叫，发送180振铃
            else {
                mUserAgentBroadcastReceiver.mMediaManager.startRing("");
                UserAgentBroadcastReceiver.getUserAgent().callAnswer(call_id, SipCallSession.StatusCode.RINGING, true);
                CallState accCallState = new CallState(0, callInfo.getCallId(), callInfo.getCallState(), callInfo.mediaHasVideo(), callInfo.getLastStatusCode(), false);
                mSipService.intentVideo(mSipService, accCallState);
            }
        } catch (SameThreadException e) {
            e.printStackTrace();
        } finally {
            unlockCpu();
        }
    }

    /**
     * 不允许多用户呼叫
     *
     * @param call_id
     * @return
     */
    public boolean isMultiCall(int call_id) {
        SipCallSessionImpl[] calls = getCalls();
        if (calls != null) {
            for (SipCallSessionImpl existingCall : calls) {
                if (!existingCall.isAfterEnded() && existingCall.getCallId() != call_id) {
                    Log.e(TAG, "不同时支持两个电话 !!!");
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void on_call_tsx_state(int call_id, SWIGTYPE_p_pjsip_transaction tsx, pjsip_event e) {
        super.on_call_tsx_state(call_id, tsx, e);
        LogUtils.d(TAG, "on_call_tsx_state");
    }

    @Override
    public void on_call_media_state(int call_id) {
        pjsua.css_on_call_media_state(call_id);
        lockCpu();
        if (mUserAgentBroadcastReceiver.mMediaManager != null) {
            mUserAgentBroadcastReceiver.mMediaManager.stopRing();
        }
        try {
            final SipCallSession callInfo = updateCallInfoFromStack(call_id, null); // 更新通话信息
            boolean connectToOtherCalls = false;
            int callConfSlot = callInfo.getConfPort();
            int mediaStatus = callInfo.getMediaStatus();
            if (mediaStatus == SipCallSession.MediaState.ACTIVE || mediaStatus == SipCallSession.MediaState.REMOTE_HOLD) {
                connectToOtherCalls = true;
                pjsua.conf_connect(callConfSlot, 0);
                pjsua.conf_connect(0, callConfSlot);
            }
        } catch (SameThreadException e) {
            e.printStackTrace();
        } finally {
            unlockCpu();
        }
        LogUtils.e(TAG, "on_call_media_state");
    }

    @Override
    public void on_call_sdp_created(int call_id, SWIGTYPE_p_pjmedia_sdp_session sdp, pj_pool_t pool, SWIGTYPE_p_pjmedia_sdp_session rem_sdp) {
        super.on_call_sdp_created(call_id, sdp, pool, rem_sdp);
        LogUtils.d(TAG, "on_call_sdp_created");
    }

    @Override
    public void on_stream_created(int call_id, SWIGTYPE_p_pjmedia_stream strm, long stream_idx, SWIGTYPE_p_p_pjmedia_port p_port) {
        super.on_stream_created(call_id, strm, stream_idx, p_port);
        LogUtils.d(TAG, "on_stream_created");
    }

    @Override
    public void on_stream_destroyed(int call_id, SWIGTYPE_p_pjmedia_stream strm, long stream_idx) {
        super.on_stream_destroyed(call_id, strm, stream_idx);
        LogUtils.d(TAG, "on_stream_destroyed");
    }

    @Override
    public void on_dtmf_digit(int call_id, int digit) {
        super.on_dtmf_digit(call_id, digit);
        LogUtils.d(TAG, "on_dtmf_digit");
    }

    @Override
    public void on_call_transfer_request(int call_id, pj_str_t dst, SWIGTYPE_p_pjsip_status_code code) {
        super.on_call_transfer_request(call_id, dst, code);
        LogUtils.d(TAG, "on_call_transfer_request");
    }

    @Override
    public void on_call_transfer_status(int call_id, int st_code, pj_str_t st_text, int final_, SWIGTYPE_p_int p_cont) {
        super.on_call_transfer_status(call_id, st_code, st_text, final_, p_cont);
        LogUtils.d(TAG, "on_call_transfer_status");
    }

    @Override
    public void on_call_replace_request(int call_id, SWIGTYPE_p_pjsip_rx_data rdata, SWIGTYPE_p_int st_code, pj_str_t st_text) {
        super.on_call_replace_request(call_id, rdata, st_code, st_text);
        LogUtils.d(TAG, "on_call_replace_request");
    }

    @Override
    public void on_call_replaced(int old_call_id, int new_call_id) {
        super.on_call_replaced(old_call_id, new_call_id);
        LogUtils.d(TAG, "on_call_replaced");
    }

    /**
     * 注册回调
     *
     * @param acc_id
     */
    @Override
    public void on_reg_state(int acc_id) {
        super.on_reg_state(acc_id);
        lockCpu();
        LogUtils.d(TAG, "on_reg_state acc_id: " + acc_id);
        int success = pjsuaConstants.PJ_FALSE;
        pjsua_acc_info pjAccountInfo;
        pjAccountInfo = new pjsua_acc_info();
        success = pjsua.acc_get_info(acc_id, pjAccountInfo);
        if (success == pjsuaConstants.PJ_SUCCESS && pjAccountInfo != null) {
            int stateCode = -1;
            String stateMessage = "";
            int swigValue = pjAccountInfo.getStatus().swigValue();
            if (200 == swigValue) {
                stateCode = 200;
                stateMessage = "登陆成功";
            } else {
                stateCode = swigValue;
                stateMessage = "注册失败：" + pjStrToString(pjAccountInfo.getStatus_text()) + " " + pjAccountInfo.getExpires();
            }
            AccountNotification.onUpdateNotification(mSipService, UserAgentBroadcastReceiver.pjStrToString(pjAccountInfo.getAcc_uri()), stateCode, stateMessage, AccountNotification.REGISTER_NOTIFICATION);
        } else {
            AccountNotification.onUpdateNotification(mSipService, UserAgentBroadcastReceiver.pjStrToString(pjAccountInfo.getAcc_uri()), 489, "注册发生错误", AccountNotification.REGISTER_NOTIFICATION);
        }

        unlockCpu();
    }

    @Override
    public void on_buddy_state(int buddy_id) {
        super.on_buddy_state(buddy_id);
        LogUtils.d(TAG, "on_buddy_state");
    }

    /**
     * 短信监听回调
     *
     * @param call_id
     * @param from
     * @param to
     * @param contact
     * @param mime_type
     * @param body
     */
    @Override
    public void on_pager(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type, pj_str_t body) {
        LogUtils.d(TAG, "on_pager -- ");
        super.on_pager(call_id, from, to, contact, mime_type, body);

        lockCpu();
        long date = System.currentTimeMillis();
        String fromStr = UserAgentBroadcastReceiver.pjStrToString(from); // "100" <sip:100@192.168.1.35>
        String canonicFromStr = SipUri.getCanonicalSipContact(fromStr); // sip:100@192.168.1.35
        String contactStr = UserAgentBroadcastReceiver.pjStrToString(contact); //
        String toStr = UserAgentBroadcastReceiver.pjStrToString(to); // "102" <sip:102@192.168.1.35>
        String bodyStr = UserAgentBroadcastReceiver.pjStrToString(body); // message body
        String mimeStr = UserAgentBroadcastReceiver.pjStrToString(mime_type); // text/plain
        AccountNotification.onUpdateNotification(mSipService, UserAgentBroadcastReceiver.pjStrToString(to), 200, bodyStr, AccountNotification.MESSAGE_NOTIFICATION);
        unlockCpu();
    }

    @Override
    public void on_pager2(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type, pj_str_t body, SWIGTYPE_p_pjsip_rx_data rdata) {
        super.on_pager2(call_id, from, to, contact, mime_type, body, rdata);
        LogUtils.d(TAG, "on_pager2");
    }

    @Override
    public void on_pager_status(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason) {
        super.on_pager_status(call_id, to, body, status, reason);
        LogUtils.d(TAG, "on_pager_status");
    }

    @Override
    public void on_pager_status2(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason, SWIGTYPE_p_pjsip_tx_data tdata, SWIGTYPE_p_pjsip_rx_data rdata) {
        super.on_pager_status2(call_id, to, body, status, reason, tdata, rdata);
        LogUtils.d(TAG, "on_pager_status2");
    }

    @Override
    public void on_typing(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, int is_typing) {
        super.on_typing(call_id, from, to, contact, is_typing);
        LogUtils.d(TAG, "on_typing");
    }

    @Override
    public void on_nat_detect(pj_stun_nat_detect_result res) {
        super.on_nat_detect(res);
        LogUtils.d(TAG, "on_nat_detect");
    }

    @Override
    public pjsip_redirect_op on_call_redirected(int call_id, pj_str_t target) {
        LogUtils.d(TAG, "on_call_redirected");
        return super.on_call_redirected(call_id, target);
    }

    @Override
    public void on_mwi_info(int acc_id, pj_str_t mime_type, pj_str_t body) {
        super.on_mwi_info(acc_id, mime_type, body);
        LogUtils.d(TAG, "on_mwi_info");
    }

    @Override
    public void on_call_media_transport_state(int call_id, pjsua_med_tp_state_info info) {
        super.on_call_media_transport_state(call_id, info);
        LogUtils.d(TAG, "on_call_media_transport_state");
    }

    @Override
    public int on_validate_audio_clock_rate(int clock_rate) {
        LogUtils.d(TAG, "on_validate_audio_clock_rate");
        return super.on_validate_audio_clock_rate(clock_rate);
    }

    @Override
    public void on_setup_audio(int before_init) {
        super.on_setup_audio(before_init);
        LogUtils.d(TAG, "on_setup_audio");
    }

    @Override
    public void on_teardown_audio() {
        super.on_teardown_audio();
        LogUtils.d(TAG, "on_teardown_audio");
    }

    @Override
    public int on_set_micro_source() {
        LogUtils.d(TAG, "on_set_micro_source");
        return super.on_set_micro_source();
    }

    /**
     * 获得可呼叫的会话列表
     *
     * @return
     */
    public SipCallSessionImpl[] getCalls() {
        if (mCallsList != null) {
            List<SipCallSessionImpl> calls = new ArrayList<SipCallSessionImpl>();
            synchronized (mCallsList) {
                for (int i = 0; i < mCallsList.size(); i++) {
                    SipCallSessionImpl callInfo = getCallInfo(i);
                    if (callInfo != null) {
                        calls.add(callInfo);
                    }
                }
            }
            return calls.toArray(new SipCallSessionImpl[calls.size()]);
        }
        return new SipCallSessionImpl[0];
    }

    /**
     * 更新通话信息中的通话记录状态
     *
     * @param callId 指定的通话ID
     */
    public void updateRecordingStatus(int callId, boolean canRecord, boolean isRecording) {
        SipCallSessionImpl callInfo = getCallInfo(callId);
        callInfo.setCanRecord(canRecord);
        callInfo.setIsRecording(isRecording);
        synchronized (mCallsList) {
            // Re-add it just to be sure
            mCallsList.put(callId, callInfo);
        }
//        onBroadcastCallState(callInfo);
    }

    /**
     * 查找指定的会话
     *
     * @param callId
     * @return
     */
    public SipCallSessionImpl getCallInfo(Integer callId) {
        SipCallSessionImpl callInfo;
        synchronized (mCallsList) {
            callInfo = mCallsList.get(callId, null);
        }
        return callInfo;
    }

    /**
     * 通过调用原PJSIP协议栈，更新电话通话信息
     *
     * @param callId 更新这个callId
     * @param e      e 更新要求
     * @return 内置的会话信息，保存在缓冲中
     */
    private SipCallSessionImpl updateCallInfoFromStack(Integer callId, pjsip_event e) throws SameThreadException {
        SipCallSessionImpl callInfo;
        synchronized (mCallsList) {
            callInfo = mCallsList.get(callId);
            if (callInfo == null) {
                callInfo = new SipCallSessionImpl();
                callInfo.setCallId(callId);
            }
        }

        try {
            PjSipCalls.updateSessionFromPj(callInfo, e, mSipService);
            synchronized (mCallsList) {
                mCallsList.put(callId, callInfo);
            }
        } catch (SameThreadException e1) {
            e1.printStackTrace();
        }
        return callInfo;
    }

    public static String pjStrToString(pj_str_t pjStr) {
        try {
            if (pjStr != null) {
                // If there's utf-8 ptr length is possibly lower than slen
                int len = pjStr.getSlen();
                if (len > 0 && pjStr.getPtr() != null) {
                    // Be robust to smaller length detected
                    if (pjStr.getPtr().length() < len) {
                        len = pjStr.getPtr().length();
                    }
                    if (len > 0) {
                        return pjStr.getPtr().substring(0, len);
                    }
                }
            }
        } catch (StringIndexOutOfBoundsException e) {
            LogUtils.d(TAG, "Impossible to retrieve string from pjsip: " + e);
        }
        return "";
    }

    public static long getAccountIdForPjsipId(Context ctxt, int pjId) {
        long accId = SipProfile.INVALID_ID;
        Cursor c = ctxt.getContentResolver().query(SipProfile.ACCOUNT_STATUS_URI, null, null, null, null);
        if (c != null) {
            try {
                c.moveToFirst();
                do {
                    int pjsuaId = c.getInt(c.getColumnIndex(SipProfileState.PJSUA_ID));
                    LogUtils.d(TAG, "Found pjsua " + pjsuaId + " searching " + pjId);
                    if (pjsuaId == pjId) {
                        accId = c.getInt(c.getColumnIndex(SipProfileState.ACCOUNT_ID));
                        break;
                    }
                } while (c.moveToNext());
            } catch (Exception e) {
                LogUtils.d(TAG, "Error on looping over sip profiles: " + e);
            } finally {
                c.close();
            }
        }
        return accId;
    }

    private class WorkerHandler extends Handler {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case ON_CALL_STATE: {
                    SipCallSessionImpl callInfo = (SipCallSessionImpl) msg.obj;
                    final int callState = callInfo.getCallState();
                    switch (callState) {
                        case SipCallSession.InvState.INCOMING:
                            LogUtils.e(TAG, "INCOMING");
                            break;
                        case SipCallSession.InvState.CALLING: { // 发送邀请之后
                            LogUtils.e(TAG, "CALLING");
                            CallState accCallState = new CallState(0, callInfo.getCallId(), callInfo.getCallState(), callInfo.mediaHasVideo(), callInfo.getLastStatusCode(), false);
                            mSipService.intentVideo(mSipService, accCallState);
                        }

                        break;
                        case SipCallSession.InvState.EARLY:
                            LogUtils.e(TAG, "EARLY");
                            break;
                        case SipCallSession.InvState.CONNECTING: // 接收2xx
                            LogUtils.e(TAG, "CONNECTING");
                            break;
                        case SipCallSession.InvState.CONFIRMED: { // 接收ack
                            LogUtils.e(TAG, "CONFIRMED");
                            CallState accCallState = new CallState(0, callInfo.getCallId(), callInfo.getCallState(), callInfo.mediaHasVideo(), callInfo.getLastStatusCode(), false);
                            AccountNotification.onUpdateNotification(mSipService, callInfo.getCallId() + "", callState, "通话中...", AccountNotification.CALL_NOTIFICATION, accCallState);
                            mSipService.intentVideo(mSipService, accCallState);
                        }
                        break;
                        case SipCallSession.InvState.DISCONNECTED: { // 会议结束
                            LogUtils.e(TAG, "DISCONNECTED");
                            if (mUserAgentBroadcastReceiver.mMediaManager != null) {
                                mUserAgentBroadcastReceiver.mMediaManager.stopRing();
                            }

                            mUserAgentBroadcastReceiver.mCallState = false;

                            AccountNotification.onUpdateNotification(mSipService, callInfo.getCallId() + "", callState, "通话关闭...", AccountNotification.CALL_NOTIFICATION, true);
                            CallState accCallState = new CallState(0, callInfo.getCallId(), callInfo.getCallState(), callInfo.mediaHasVideo(), callInfo.getLastStatusCode(), false);
                            mSipService.intentVideo(mSipService, accCallState);
                            callInfo.applyDisconnect();
                        }
                        break;
                        default:
                            break;
                    }
                    break;
                }
                case ON_MEDIA_STATE: {
                    break;
                }
            }
        }
    }
}