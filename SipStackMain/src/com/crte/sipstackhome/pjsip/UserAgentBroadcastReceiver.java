package com.crte.sipstackhome.pjsip;

import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.widget.Toast;

import com.crte.sipstackhome.api.MediaState;
import com.crte.sipstackhome.api.SipCallSession;
import com.crte.sipstackhome.api.SipConfigManager;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.api.SipProfileState;
import com.crte.sipstackhome.api.SipUri;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.service.MediaManager;
import com.crte.sipstackhome.service.SipService;
import com.crte.sipstackhome.ui.login.AccountNotification;
import com.crte.sipstackhome.ui.login.LoginActivity;
import com.crte.sipstackhome.ui.preferences.PreferencesWrapper;
import com.crte.sipstackhome.utils.ExtraPlugins;
import com.crte.sipstackhome.utils.log.Log;
import com.crte.sipstackhome.utils.log.LogUtils;

import org.pjsip.pjsua.csipsimple_config;
import org.pjsip.pjsua.dynamic_factory;
import org.pjsip.pjsua.pj_pool_t;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsip_transport_type_e;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_info;
import org.pjsip.pjsua.pjsua_call_setting;
import org.pjsip.pjsua.pjsua_config;
import org.pjsip.pjsua.pjsua_logging_config;
import org.pjsip.pjsua.pjsua_media_config;
import org.pjsip.pjsua.pjsua_msg_data;
import org.pjsip.pjsua.pjsua_transport_config;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 用户代理
 * Created by wangz on 2015/12/26.
 */
public class UserAgentBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "UserAgentBroadcastReceiver";

    private static UserAgentBroadcastReceiver userAgentBroadcastReceiver;
    private static ArrayList<String> codecs = new ArrayList<>();
    private static ArrayList<String> video_codecs = new ArrayList<>();

    private static boolean codecs_initialized = false;

    private SparseArray<List<IRecorderHandler>> mCallRecorders = new SparseArray<List<IRecorderHandler>>();

    public PreferencesWrapper mPreferencesWrapper;
    public SipService mSipService;
    public MediaManager mMediaManager;

    private UAStateCallback mUaStateCallback;
    private ZrtpStateCallback mZrtpStateCallback;

    /**
     * Sip 协议栈启动状态
     */
    private boolean mCreate;
    /**
     * 当前电话呼叫状态
     */
    public boolean mCallState;

    public UserAgentBroadcastReceiver(SipService sipService) {
        this.mSipService = sipService;
        mPreferencesWrapper = mSipService.getmPreferencesWrapper();
        userAgentBroadcastReceiver = this;
    }

    public static UserAgentBroadcastReceiver getUserAgent() {
        return userAgentBroadcastReceiver;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        switch (action) {
            case SipManager.ACTION_SIP_ACCOUNT_CHANGED: {
                LogUtils.i(TAG, "账户状态发生变化");
                final long accountId = intent.getLongExtra(SipProfile.FIELD_ID, SipProfile.INVALID_ID);
                retrievalAccountReg(mSipService, accountId);
            }
            break;
            case SipManager.ACTION_SIP_STACK_CHANGE:
                LogUtils.i(TAG, "SIP协议栈状态发生变化");
                mSipService.RestartRunnableAndRegister();
                break;
            case SipManager.ACTION_CALL_MAKE:
                break;

            case SipManager.ACTION_FINISH:
                mSipService.SafeCloseRunnable();
                break;

            case ConnectivityManager.CONNECTIVITY_ACTION: {
                retrievalAccountReg(mSipService, LoginActivity.ACCONT_ID);
            }
            break;

            case Intent.ACTION_BOOT_COMPLETED: // 开机启动
                break;
            case Intent.ACTION_HEADSET_PLUG: // 插拔耳机
                break;
            case Intent.ACTION_SCREEN_ON: // 点亮屏幕
                break;
            case Intent.ACTION_USER_PRESENT: // 屏幕锁
                retrievalAccountReg(mSipService, LoginActivity.ACCONT_ID);
                break;
            case Intent.ACTION_SCREEN_OFF: // 关闭屏幕
                break;
            case WifiManager.WIFI_STATE_CHANGED_ACTION: // WiFi状态发生变化
                judgeCurrentNetworkState();
                break;
            case WifiManager.SCAN_RESULTS_AVAILABLE_ACTION: // 扫描WiFi后执行
                break;
        }
    }

    public void retrievalAccountReg(Context context, long accountId) {
        if (context == null) {
            context = mSipService;
        }

        if (accountId != SipProfile.INVALID_ID) {
            final SipProfile account = PjSipAccount.getAccount(context, accountId);
            LogUtils.d(TAG, "account:" + account.getSipUserName());
            LogUtils.d(TAG, "account.reg_uri:" + account.reg_uri);

            if (account != null) {
                try {
                    setAccountRegistration(account); // 根据账号的活动状态，进行注册或注销
                } catch (SameThreadException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 开启录音
     *
     * @param callId
     * @param way
     * @throws SameThreadException
     */
    public void startRecording(int callId, int way) throws SameThreadException {
        try {
            File recFolder = mPreferencesWrapper.getRecordsFolder(mSipService);
            IRecorderHandler recoder = new SimpleWavRecorderHandler(getCallInfo(callId), recFolder, way);
            List<IRecorderHandler> recordersList = mCallRecorders.get(callId, new ArrayList<IRecorderHandler>());
            recordersList.add(recoder);
            mCallRecorders.put(callId, recordersList);
            recoder.startRecording();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止录音
     *
     * @param callId
     * @throws SameThreadException
     */
    public void stopRecording(int callId) throws SameThreadException {
        if (!mCreate) {
            return;
        }

        List<IRecorderHandler> recoders = mCallRecorders.get(callId, null);
        if (recoders != null) {
            for (IRecorderHandler recoder : recoders) {
                recoder.stopRecording();
                // 广播到其他应用程序的一个新的记录已经完成
//                SipCallSession callInfo = getPublicCallInfo(callId);
//                Intent it = new Intent(SipManager.ACTION_SIP_CALL_RECORDED);
//                it.putExtra(SipManager.EXTRA_CALL_INFO, callInfo);
//                recoder.fillBroadcastWithInfo(it);
//                service.sendBroadcast(it, SipManager.PERMISSION_USE_SIP);
            }
            // In first case we drop everything
            mCallRecorders.delete(callId);
        }
    }

    /**
     * 获得公共呼叫信息
     *
     * @param callId
     * @return
     */
    public SipCallSession getPublicCallInfo(int callId) {
        SipCallSession internalCallSession = getCallInfo(callId);
        if (internalCallSession == null) {
            return null;
        }
        return new SipCallSession(internalCallSession);
    }

    /**
     * 获得呼叫信息
     *
     * @param callId
     * @return
     */
    public SipCallSession getCallInfo(int callId) {
        if (mCreate && mUaStateCallback != null) {
            SipCallSession callInfo = mUaStateCallback.getCallInfo(callId);
            return callInfo;
        }
        return null;
    }

    /**
     * 设置账号的状态<br/>
     * 目前只支持注册账户
     */
    public boolean setAccountRegistration(SipProfile sipProfile) throws SameThreadException {
        int status;
        if (!mCreate || sipProfile == null) {
            Log.e(TAG, "PJSIP SIP 协议栈没有启动");
            return false;
        }
        if (sipProfile.id == SipProfile.INVALID_ID) {
            Log.w(TAG, "这是一个无效用户");
            return false;
        }
//        SipProfileState currentAccountStatus = getProfileState(sipProfile);

        com.crte.sipstackhome.pjsip.PjSipAccount account = new com.crte.sipstackhome.pjsip.PjSipAccount(sipProfile);
        // account.applyExtraParams(mSipService); // 添加扩展参数

        account.cfg.setRegister_on_acc_add(pjsuaConstants.PJ_TRUE);
        int[] accId = new int[1];
        accId[0] = account.id.intValue();
        status = pjsua.acc_add(account.cfg, pjsuaConstants.PJ_TRUE, accId);

        onUpdateNotification(mSipService, account.getDisplayName(), SipCallSession.StatusCode.UNAUTHORIZED, "正在注册...", AccountNotification.REGISTER_NOTIFICATION);

        // 添加配置文件
        if (status == pjsuaConstants.PJ_SUCCESS) {
            SipProfileState profileState = new SipProfileState(sipProfile);
            profileState.setPjsuaId(accId[0]);
            profileState.setAddedStatus(status);
            mSipService.getContentResolver().insert(ContentUris.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, account.id), profileState.getAsContentValue());
            pjsua.acc_set_online_status(accId[0], 1);
        }

        return status == 0;
    }

    /**
     * 从内容提供商的后台同步PJSIP协议栈
     *
     * @throws SameThreadException
     */
    public void updateProfileStateFromService(int pjsuaId) throws SameThreadException {
        if (!mCreate) {
            return;
        }
        long accId = getAccountIdForPjsipId(mSipService, pjsuaId);
        LogUtils.d(TAG, "从服务器更新 " + pjsuaId + " 到数据库  " + accId);
        if (accId != SipProfile.INVALID_ID) {
            int success = pjsuaConstants.PJ_FALSE;
            pjsua_acc_info pjAccountInfo;
            pjAccountInfo = new pjsua_acc_info();
            success = pjsua.acc_get_info(pjsuaId, pjAccountInfo); // 获得账户信息状态
            if (success == pjsuaConstants.PJ_SUCCESS && pjAccountInfo != null) {
                ContentValues cv = new ContentValues();
                try {
                    cv.put(SipProfileState.STATUS_CODE, pjAccountInfo.getStatus().swigValue());
                } catch (IllegalArgumentException e) {
                    cv.put(SipProfileState.STATUS_CODE, SipCallSession.StatusCode.INTERNAL_SERVER_ERROR);
                }

                cv.put(SipProfileState.STATUS_TEXT, pjStrToString(pjAccountInfo.getStatus_text()));
                cv.put(SipProfileState.EXPIRES, pjAccountInfo.getExpires());

                mSipService.getContentResolver().update(ContentUris.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, accId), cv, null, null);

                LogUtils.d("SIP UA Receiver", "pjAccountInfo.getStatus().swigValue(): " + pjAccountInfo.getStatus().swigValue());
                LogUtils.d("SIP UA Receiver", "pjAccountInfo.getStatus().getStatus_text(): " + pjStrToString(pjAccountInfo.getStatus_text()));
                LogUtils.d("SIP UA Receiver", "pjAccountInfo.getStatus().getExpires(): " + pjAccountInfo.getExpires());

                LogUtils.d(TAG, "Profile state UP : " + cv);
            }
        } else {
            LogUtils.d(TAG, "Trying to update not added account " + pjsuaId);
        }
    }

    /**
     * 发送短信信息<br/>
     * 将数据信息保存至数据库中
     */
    public ToCall sendMessage(String callee, String message, long accountId) throws SameThreadException {
        if (!mCreate) {
            LogUtils.d(TAG, "SIP 协议栈没有启动");
            return null;
        }

        ToCall toCall = sanitizeSipUri(callee, accountId);
        if (toCall != null) {
            pj_str_t uri = pjsua.pj_str_copy(toCall.getCallee());
            pj_str_t text = pjsua.pj_str_copy(message);

            byte[] userData = new byte[1];
            int status = pjsua.im_send(toCall.getPjsipAccountId(), uri, null, text, null, userData);

            if (status == pjsuaConstants.PJ_SUCCESS) {
//                ArrayList<Contacts> contactses = Contacts.getqueryDatas(mSipService, Contacts.FIELD_PHONE, callee);
//                ShortMessage shortMessage = new ShortMessage();
//
//                if (contactses.size() > 0) {
//                    Contacts contacts = contactses.get(0);
//                    shortMessage.pid = contacts._id;
//                    shortMessage.fromUsername = contacts.phone;
//                    shortMessage.fromSipUri = contacts.phone;
//                } else {
//                    shortMessage.fromUsername = accountId + "";
//                    shortMessage.fromSipUri = accountId + "";
//                }
//
//                shortMessage.toUsername = "me";
//                shortMessage.toSipUri = "<sip:me>";
//                shortMessage.body = message;
//                shortMessage.date = System.currentTimeMillis();
//                shortMessage.mimeType = "text/plant";
//                shortMessage.read = 1;
//                ShortMessage.insertDatas(mSipService.getContentResolver(), shortMessage);
                return toCall;
            }
        }
        return null;
    }

    /**
     * 发起呼叫
     */
    public void makeCallOrVideo(String callee, int accountId, Bundle b, boolean useVideo) throws SameThreadException {
        LogUtils.d(TAG, "MakeCallOrVideo ...");
        if (!mCreate) {
            LogUtils.d(TAG, "SIP 协议栈没有启动");
            return;
        }

        if (!mCallState) {
            final ToCall toCall = sanitizeSipUri(callee, accountId);
            if (toCall != null) {
                pj_str_t uri = pjsua.pj_str_copy(toCall.getCallee());

                byte[] userData = new byte[1];
                int[] callId = new int[1];
                pjsua_call_setting cs = new pjsua_call_setting();
                pjsua_msg_data msgData = new pjsua_msg_data();
                int pjsuaAccId = toCall.getPjsipAccountId();

                pjsua.call_setting_default(cs);

                cs.setAud_cnt(1);
                cs.setVid_cnt(useVideo ? 1 : 0);
                cs.setFlag(0);

                pj_pool_t pool = pjsua.pool_create("call_tmp", 512, 512); // 创建内存池，使用完成后必须释放
                pjsua.msg_data_init(msgData);
                pjsua.csipsimple_init_acc_msg_data(pool, pjsuaAccId, msgData);

                pjsua.call_make_call(pjsuaAccId, uri, cs, userData, msgData, callId);
                LogUtils.d(TAG, "开始呼叫");
                pjsua.pj_pool_release(pool);
            } else {
                LogUtils.d(TAG, "呼叫失败");
            }
            mCallState = true;
        } else {
            Toast.makeText(mSipService, "已有一个呼叫正在进行中...", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 接听电话<br/>
     * 可以发送不同的数据
     */
    public int callAnswer(int callId, int code, boolean useVideo) {
        LogUtils.i(TAG, "callId:" + callId + " code:" + code + " useVideo:" + useVideo);
        pjsua_call_setting cs = new pjsua_call_setting();
        pjsua.call_setting_default(cs);
        cs.setAud_cnt(1);
        cs.setVid_cnt(useVideo ? 1 : 0);
        cs.setFlag(0);
        return pjsua.call_answer2(callId, cs, code, null, null);
    }

    /**
     * 挂断电话
     */
    public int callHangup(int callId, int code) throws SameThreadException {
        stopRecording(callId); // 停止录音
        int state = pjsua.call_hangup(callId, code, null, null);
        mCallState = false;
        return state;
    }

    @Nullable
    private ToCall sanitizeSipUri(String callee, long accountId) throws SameThreadException {
        int pjsipAccountId = (int) SipProfile.INVALID_ID;

        SipProfile account = new SipProfile();
        account.id = accountId;
        SipProfileState profileState = getProfileState(account);
        long finalAccountId = accountId;

        // 如果这是一个无效账户
        if (accountId == SipProfile.INVALID_ID || !profileState.isAddedToStack()) {
            int defaultPjsipAccount = pjsua.acc_get_default();

            boolean valid = false;
            account = getAccountForPjsipId(defaultPjsipAccount);
            if (account != null) {
                profileState = getProfileState(account);
                valid = profileState.isAddedToStack();
            }
            if (!valid) {
                Cursor c = mSipService.getContentResolver().query(SipProfile.ACCOUNT_STATUS_URI, null, null, null, null);
                if (c != null) {
                    try {
                        if (c.getCount() > 0) {
                            c.moveToFirst();
                            do {
                                SipProfileState ps = new SipProfileState(c);
                                if (ps.isValidForCall()) {
                                    finalAccountId = ps.getAccountId();
                                    pjsipAccountId = ps.getPjsuaId();
                                    break;
                                }
                            } while (c.moveToNext());
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "SipProfile状态发生错误", e);
                    } finally {
                        c.close();
                    }
                }
            } else {
                // 使用默认配置
                finalAccountId = profileState.getAccountId();
                pjsipAccountId = profileState.getPjsuaId();
            }
        } else {
            pjsipAccountId = profileState.getPjsuaId();
        }

        if (pjsipAccountId == SipProfile.INVALID_ID) {
            LogUtils.e(TAG, "这不是一个有效的SIP账户");
            return null;
        }

        account = PjSipAccount.getAccount(mSipService, (int) finalAccountId);
        SipUri.ParsedSipContactInfos finalCallee = account.formatCalleeNumber(callee);
        String digitsToAdd = null;
        if (!TextUtils.isEmpty(finalCallee.userName) && (finalCallee.userName.contains(",") || finalCallee.userName.contains(";"))) {
            int commaIndex = finalCallee.userName.indexOf(",");
            int semiColumnIndex = finalCallee.userName.indexOf(";");
            if (semiColumnIndex > 0 && semiColumnIndex < commaIndex) {
                commaIndex = semiColumnIndex;
            }
            digitsToAdd = finalCallee.userName.substring(commaIndex);
            finalCallee.userName = finalCallee.userName.substring(0, commaIndex);
        }

        Log.d(TAG, "will call " + finalCallee);

        if (pjsua.verify_sip_url(finalCallee.toString(false)) == 0) {
            // In worse worse case, find back the account id for uri.. but
            // probably useless case
            if (pjsipAccountId == SipProfile.INVALID_ID) {
                pjsipAccountId = pjsua.acc_find_for_outgoing(pjsua.pj_str_copy(finalCallee.toString(false)));
            }
            return new ToCall(pjsipAccountId, finalCallee.toString(true), digitsToAdd);
        }
        return null;
    }

    /**
     * 获得PjsipId的账户
     *
     * @param pjId
     * @return
     */
    public SipProfile getAccountForPjsipId(int pjId) {
        long accId = getAccountIdForPjsipId(mSipService, pjId);
        if (accId == SipProfile.INVALID_ID) {
            return null;
        } else {
            return PjSipAccount.getAccount(mSipService, accId);
        }
    }

    /**
     * 获得PjsipId的账号ID
     *
     * @param ctxt
     * @param pjId
     * @return
     */
    public static long getAccountIdForPjsipId(Context ctxt, int pjId) {
        long accId = SipProfile.INVALID_ID;

        Cursor c = ctxt.getContentResolver().query(SipProfile.ACCOUNT_STATUS_URI, null, null, null, null);
        if (c != null) {
            try {
                c.moveToFirst();
                do {
                    int pjsuaId = c.getInt(c.getColumnIndex(SipProfileState.PJSUA_ID));
                    Log.d(TAG, "Found pjsua " + pjsuaId + " searching " + pjId);
                    if (pjsuaId == pjId) {
                        accId = c.getInt(c.getColumnIndex(SipProfileState.ACCOUNT_ID));
                        break;
                    }
                } while (c.moveToNext());
            } catch (Exception e) {
                Log.e(TAG, "Error on looping over sip profiles", e);
            } finally {
                c.close();
            }
        }
        return accId;
    }

    public static final class ToCall {
        private Integer pjsipAccountId;
        private String callee;
        private String dtmf;

        public ToCall(Integer acc, String uri) {
            pjsipAccountId = acc;
            callee = uri;
        }

        public ToCall(Integer acc, String uri, String dtmfChars) {
            pjsipAccountId = acc;
            callee = uri;
            dtmf = dtmfChars;
        }

        /**
         * @return the pjsipAccountId
         */
        public Integer getPjsipAccountId() {
            return pjsipAccountId;
        }

        /**
         * @return the callee
         */
        public String getCallee() {
            return callee;
        }

        /**
         * @return the dtmf sequence to automatically dial for this call
         */
        public String getDtmf() {
            return dtmf;
        }
    }

    /**
     * 开启SIP协议栈
     */
    public void startSipStack() {
        int pj_status_t;
        pj_status_t = pjsua.create();

        if(pj_status_t != pjsua.PJ_SUCCESS) {
            LogUtils.e(TAG, "初始化PJSUA失败！");
            return ;
        }

        // 通用配置
        pjsua_config cfg = new pjsua_config(); // PJSUA 配置文件
        pjsua_logging_config logCfg = new pjsua_logging_config(); // PJSUA 日志文件
        pjsua_media_config mediaCfg = new pjsua_media_config(); // PJSUA 多媒体文件

        /* 特殊：CSipSimple配置文件 */
        csipsimple_config cssCfg = new csipsimple_config();

        /**
         * 设置应用程序回调
         * 接收来电信息、媒体状态的回调函数
         */
        if (mUaStateCallback == null) {
            mUaStateCallback = new UAStateCallback(this, mSipService);
        }

        /**
         * ZRTP 是使用SRTP在两个端点间协商共享秘密和秘钥交换的机制
         * ZRTP 运用随机加密，这个意味着RTP流的安全问题不用担心
         */
        if (mZrtpStateCallback == null) {
            mZrtpStateCallback = new ZrtpStateCallback();
        }

        if (mMediaManager == null) {
            mMediaManager = new MediaManager(this);
        }

        pjsua.setCallbackObject(mUaStateCallback);
        pjsua.setZrtpCallbackObject(mZrtpStateCallback);

        /*
         * 设置CSipSimple默认配置文件
         * 路径：jni/csipsimple-wrapper/src/pjsua_jni_addons.c
         */
        pjsua.csipsimple_config_default(cssCfg);
        cssCfg.setUse_compact_form_headers(pjsua.PJ_FALSE); // 使用紧凑版的SIP
        cssCfg.setUse_compact_form_sdp(pjsua.PJ_FALSE);
        // cssCfg.setUse_no_update(pjsua.PJ_FALSE); // 避免使用UPDATE

        /* PJSIP：设置默认配置 */
        pjsua.config_default(cfg);
        cfg.setCb(pjsuaConstants.WRAPPER_CALLBACK_STRUCT);
        cfg.setThread_cnt(2);
        mediaCfg.setHas_ioqueue(pjsuaConstants.PJ_TRUE);

        /* PJSIP：默认日志配置 */
        pjsua.logging_config_default(logCfg);
        logCfg.setMsg_logging(pjsuaConstants.PJ_TRUE);

        int tsx_to = -1;
        if (tsx_to > 0) {
            cssCfg.setTsx_t1_timeout(tsx_to);
        }
        if (tsx_to > 0) {
            cssCfg.setTsx_t2_timeout(tsx_to);
        }
        if (tsx_to > 0) {
            cssCfg.setTsx_t4_timeout(tsx_to);
        }
        if (tsx_to > 0) {
            cssCfg.setTsx_td_timeout(tsx_to);
        }

        /* 媒体配置 */
        Map<String, ExtraPlugins.DynCodecInfos> availableCodecs = ExtraPlugins.getDynCodecPlugins(mSipService, SipManager.ACTION_GET_EXTRA_CODECS);
        dynamic_factory[] cssCodecs = cssCfg.getExtra_aud_codecs();

        int i = 0;
        for (Map.Entry<String, ExtraPlugins.DynCodecInfos> availableCodec : availableCodecs.entrySet()) {
            ExtraPlugins.DynCodecInfos dyn = availableCodec.getValue();
            if (!TextUtils.isEmpty(dyn.libraryPath)) {
                cssCodecs[i].setShared_lib_path(pjsua.pj_str_copy(dyn.libraryPath));
                cssCodecs[i++].setInit_factory_name(pjsua.pj_str_copy(dyn.factoryInitFunction));
            }
        }
        cssCfg.setExtra_aud_codecs_cnt(i);

        dynamic_factory audImp = cssCfg.getAudio_implementation();
        audImp.setInit_factory_name(pjsua.pj_str_copy("pjmedia_opensl_factory"));
        File openslLib = NativeLibManager.getBundledStackLibFile(mSipService, "libpj_opensl_dev.so");
        audImp.setShared_lib_path(pjsua.pj_str_copy(openslLib.getAbsolutePath()));
        cssCfg.setAudio_implementation(audImp);
        LogUtils.d(TAG, "Use OpenSL-ES implementation");

        // 视频实现
        Map<String, ExtraPlugins.DynCodecInfos> videoPlugins = ExtraPlugins.getDynCodecPlugins(mSipService, SipManager.ACTION_GET_VIDEO_PLUGIN);
        if (videoPlugins.size() > 0) {
            ExtraPlugins.DynCodecInfos videoPlugin = videoPlugins.values().iterator().next();
            pj_str_t pjVideoFile = pjsua.pj_str_copy(videoPlugin.libraryPath);
            // 渲染
            {
                dynamic_factory vidImpl = cssCfg.getVideo_render_implementation();
                vidImpl.setInit_factory_name(pjsua.pj_str_copy("pjmedia_webrtc_vid_render_factory"));
                vidImpl.setShared_lib_path(pjVideoFile);
            }
            // 捕获
            {
                dynamic_factory vidImpl = cssCfg.getVideo_capture_implementation();
                vidImpl.setInit_factory_name(pjsua.pj_str_copy("pjmedia_webrtc_vid_capture_factory"));
                vidImpl.setShared_lib_path(pjVideoFile);
            }

            // 视频编码
            availableCodecs = ExtraPlugins.getDynCodecPlugins(mSipService, SipManager.ACTION_GET_EXTRA_VIDEO_CODECS);
            cssCodecs = cssCfg.getExtra_vid_codecs();
            dynamic_factory[] cssCodecsDestroy = cssCfg.getExtra_vid_codecs_destroy();
            i = 0;
            for (Map.Entry<String, ExtraPlugins.DynCodecInfos> availableCodec : availableCodecs.entrySet()) {
                ExtraPlugins.DynCodecInfos dyn = availableCodec.getValue();
                LogUtils.d(TAG, "dyn.libraryPath: " + dyn.libraryPath);
                if (!TextUtils.isEmpty(dyn.libraryPath)) {
                    // 创建
                    cssCodecs[i].setShared_lib_path(pjsua.pj_str_copy(dyn.libraryPath));
                    cssCodecs[i].setInit_factory_name(pjsua.pj_str_copy(dyn.factoryInitFunction));
                    LogUtils.e(TAG, "setShared_lib_path:" + dyn.libraryPath);
                    LogUtils.e(TAG, "setInit_factory_name:" + dyn.factoryInitFunction);
                    // 销毁
                    cssCodecsDestroy[i].setShared_lib_path(pjsua.pj_str_copy(dyn.libraryPath));
                    cssCodecsDestroy[i].setInit_factory_name(pjsua.pj_str_copy(dyn.factoryDeinitFunction));
                    LogUtils.e(TAG, "setShared_lib_path:" + dyn.libraryPath);
                    LogUtils.e(TAG, "setInit_factory_name:" + dyn.factoryDeinitFunction);
                }
                i++;
            }
            cssCfg.setExtra_vid_codecs_cnt(i);

            // 转换器
            dynamic_factory convertImpl = cssCfg.getVid_converter();
            convertImpl.setShared_lib_path(pjVideoFile);
            convertImpl.setInit_factory_name(pjsua.pj_str_copy("pjmedia_libswscale_converter_init"));
        }

        /* PJSIP：默认媒体配置 */
        pjsua.media_config_default(mediaCfg);
        mediaCfg.setChannel_count(1);
        mediaCfg.setSnd_auto_close_time(1);
        mediaCfg.setEc_tail_len(200);
        mediaCfg.setEc_options(3);
        mediaCfg.setNo_vad(1);
        mediaCfg.setQuality(4);
        mediaCfg.setClock_rate(16000);
        mediaCfg.setAudio_frame_ptime(20);
        mediaCfg.setThread_cnt(2);
        mediaCfg.setHas_ioqueue(1);
        mediaCfg.setEnable_ice(0);

        /* PJSIP：初始化 */
        pj_status_t = pjsua.csipsimple_init(cfg, logCfg, mediaCfg, cssCfg, mSipService);
        LogUtils.d(TAG, "init: " + pj_status_t);

        // 设置传输协议
        pjsua_transport_config transport_config = new pjsua_transport_config();
        int[] tId = new int[1];
        tId[0] = 0;
        pjsua.transport_config_default(transport_config);
        transport_config.setPort(0);
        pj_status_t = pjsua.transport_create(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, transport_config, tId);
        LogUtils.d(TAG, "transport_config: " + pj_status_t);
        int[] p_acc_id = new int[1];
        pjsua.acc_add_local(tId[0], pjsua.PJ_FALSE, p_acc_id);

        // 开启
        pj_status_t = pjsua.start();
        LogUtils.d(TAG, "start: " + pj_status_t);

        mCreate = true;
        LogUtils.d(TAG, "mCreate:" + mCreate);

        try {
            initCodecs();
            setCodecsPriorities();
        } catch (SameThreadException e) {
            e.printStackTrace();
        }
    }

    /**
     * 停止SIP协议栈
     */
    public boolean stopSipStack() {
        mCreate = false;
        pjsua.csipsimple_destroy(0); // 销毁
        return true;
    }

    private void initCodecs() throws SameThreadException {
        synchronized (codecs) {
            if (!codecs_initialized) {
                int nbrCodecs, i;
                nbrCodecs = pjsua.codecs_get_nbr();
                for (i = 0; i < nbrCodecs; i++) {
                    String codecId = pjStrToString(pjsua.codecs_get_id(i));
                    codecs.add(codecId);
                }
                nbrCodecs = pjsua.codecs_vid_get_nbr();
                for (i = 0; i < nbrCodecs; i++) {
                    String codecId = pjStrToString(pjsua.codecs_vid_get_id(i));
                    video_codecs.add(codecId);
                }
                codecs_initialized = true;
            }
        }
    }

    /**
     * 设置优先编码在PJSIP协议栈层基于偏好的商店
     *
     * @throws SameThreadException
     */
    private void setCodecsPriorities() throws SameThreadException {
        ConnectivityManager cm = ((ConnectivityManager) mSipService.getSystemService(Context.CONNECTIVITY_SERVICE));

        synchronized (codecs) {
            if (codecs_initialized) {
                NetworkInfo ni = cm.getActiveNetworkInfo();
                if (ni != null) {
                    synchronized (codecs) {

                        // 设置音频编解码器
                        for (String codec : codecs) {
                            short aPrio = 0;
                            if (codec.equals("PCMA/8000/1")) {
                                aPrio = 60;
                            } else if (codec.equals("PCMU/8000/1")) {
                                aPrio = 70;
                            } else if (codec.equals("speex/8000/1")) {
                                aPrio = 60;
                            }

                            LogUtils.e("UserAg", "codec:" + codec);
                            pj_str_t codecStr = pjsua.pj_str_copy(codec);
                            if (aPrio >= 0) {
                                pjsua.codec_set_priority(codecStr, aPrio);
                            }

                            String codecKey = SipConfigManager.getCodecKey(codec, SipConfigManager.FRAMES_PER_PACKET_SUFFIX);
                            Integer frmPerPacket = SipConfigManager.getPreferenceIntegerValue(mSipService, codecKey);
                            if (frmPerPacket != null && frmPerPacket > 0) {
                                pjsua.codec_set_frames_per_packet(codecStr, frmPerPacket);
                            }
                        }

                        // 设置视频编解码器
                        for (String codec : video_codecs) {
                            short aPrio = 0;
                            if (codec.equals("H263-1998/96")) {
                                aPrio = 1;
                            } else if (codec.equals("H264/97")) {
                                aPrio = 0;
                            }
                            LogUtils.e("UserAg", "codec:" + codec);

                            if (aPrio >= 0) {
                                pjsua.vid_codec_set_priority(pjsua.pj_str_copy(codec), aPrio);
                            }
                        }
                    }
                }
            }
        }
    }

    // 麦克风静音
    private boolean userWantMicrophoneMute = false;
    // 默认情况下，我们假设用户需要蓝牙。如果蓝牙不可用连接永远不会完成，那么UI将不会显示蓝牙功能已启动。
    private boolean userWantBluetooth = false;
    // 使用扬声器
    private boolean userWantSpeaker = false;

    public MediaState getMediaState() {
        MediaState mediaState = new MediaState();

        // Micro
        mediaState.isMicrophoneMute = userWantMicrophoneMute;
        mediaState.canMicrophoneMute = true; /*&& !mediaState.isBluetoothScoOn*/ //Compatibility.isCompatible(5);

        // Speaker
        mediaState.isSpeakerphoneOn = userWantSpeaker;
        mediaState.canSpeakerphoneOn = true && !mediaState.isBluetoothScoOn; //Compatibility.isCompatible(5);

        mediaState.isBluetoothScoOn = false;
        mediaState.canBluetoothSco = false;
        return mediaState;
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
            Log.e(TAG, "Impossible to retrieve string from pjsip ", e);
        }
        return "";
    }

    /**
     * 加载SO
     */
    public void loadLibrary() {
        System.loadLibrary("stlport_shared");
        System.loadLibrary("pjsipjni");
    }

    /**
     * 判断当前的网络状态
     */
    public void judgeCurrentNetworkState() {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) mSipService.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = mConnectivityManager.getActiveNetworkInfo();
        if (netInfo != null && netInfo.isAvailable()) {
            if (netInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                Toast.makeText(mSipService, "网络状态发送改变: WiFi网络", Toast.LENGTH_SHORT).show();
            } else if (netInfo.getType() == ConnectivityManager.TYPE_ETHERNET) {
                Toast.makeText(mSipService, "网络状态发送改变: 以太网络", Toast.LENGTH_SHORT).show();
            } else if (netInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                Toast.makeText(mSipService, "网络状态发送改变: 3g/4g网络", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(mSipService, "网络状态发送改变:网络断开", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获得账户的临时配置文件配置文件的状态
     */
    public SipProfileState getProfileState(SipProfile account) {
        if (!mCreate || account == null) {
            LogUtils.d(TAG, "is null mCreate:" + mCreate + " - account:" + account);
            return null;
        }
        if (account.id == SipProfile.INVALID_ID) {
            LogUtils.d(TAG, "无效账户");
            return null;
        }
        SipProfileState accountInfo = new SipProfileState(account);
        Cursor c = mSipService.getContentResolver().query(ContentUris.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, account.id), null, null, null, null);
        if (c != null) {
            try {
                if (c.getCount() > 0) {
                    c.moveToFirst();
                    accountInfo.createFromDb(c);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error on looping over sip profiles states", e);
            } finally {
                c.close();
            }
        }
        return accountInfo;
    }

    public void confAdjustTxLevel(int port, float value) throws SameThreadException {
        LogUtils.d(TAG, "tx value:" + value);
//        if (mCreate && mUaStateCallback != null) {
            // 0, 1.0
            pjsua.conf_adjust_tx_level(port, value);
//        }
    }

    public void confAdjustRxLevel(int port, float value) throws SameThreadException {
        LogUtils.d(TAG, "rx value:" + value);
//        if (mCreate && mUaStateCallback != null) {
            pjsua.conf_adjust_rx_level(port, value);
//        }
    }

    public void onUpdateNotification(SipService sipService, String accId, int stateCode, String stateMessage, int notificationType) {
        AccountNotification.onUpdateNotification(sipService, accId, stateCode, stateMessage, notificationType);
    }

    /**
     * 提供视频渲染
     */
    public static void setVideoAndroidRenderer(int callId, SurfaceView window) {
        pjsua.vid_set_android_renderer(callId, window);
    }

    /**
     * 提供视频捕捉表面观（一个绑定到相机）。
     */
    public static void setVideoAndroidCapturer(SurfaceView window) {
        pjsua.vid_set_android_capturer(window);
    }

    public void setNoSnd() throws SameThreadException {
        if (!mCreate) {
            return;
        }
        pjsua.set_no_snd_dev();
    }

    public void setSnd() throws SameThreadException {
        if (!mCreate) {
            return;
        }
        pjsua.set_snd_dev(0, 0);
    }
}
