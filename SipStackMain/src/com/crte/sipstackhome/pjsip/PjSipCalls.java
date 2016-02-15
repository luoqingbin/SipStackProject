/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p/>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p/>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.crte.sipstackhome.pjsip;

import android.content.Context;
import android.os.SystemClock;
import android.text.TextUtils;

import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.service.SipCallSessionImpl;
import com.crte.sipstackhome.utils.log.LogUtils;

import org.pjsip.pjsua.pj_time_val;
import org.pjsip.pjsua.pjmedia_dir;
import org.pjsip.pjsua.pjsip_event;
import org.pjsip.pjsua.pjsip_inv_state;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_call_info;
import org.pjsip.pjsua.zrtp_state_info;

/**
 * 单例类管理PJSIP电话。它可以将检索PJSIP电话信息和转换成可以在Android方面容易管理的对象
 */
public final class PjSipCalls {

    private PjSipCalls() {
    }

    private static final String TAG = "PjSipCalls";

    /**
     * 更新这个呼叫会议信息
     *
     * @param session 会话更新（输入/输出）,必须有正确的呼叫标识
     * @param e       PjSipService Sip 服务器检索PJSIP账户信息
     */
    public static void updateSessionFromPj(SipCallSessionImpl session, pjsip_event e, Context context) throws SameThreadException {
        pjsua_call_info pjInfo = new pjsua_call_info();
        int status = pjsua.call_get_info(session.getCallId(), pjInfo); // 获得指定联系人的通话状态

        if (status == pjsua.PJ_SUCCESS) {
            updateSession(session, pjInfo, context);

            // Update state here because we have pjsip_event here and can get q.850 state
            if (e != null) {
                int status_code = pjsua.get_event_status_code(e);
                if (status_code == 0) {
                    try {
                        status_code = pjInfo.getLast_status().swigValue();
                    } catch (IllegalArgumentException err) {
                        // The status code does not exist in enum ignore it
                    }
                }
                session.setLastStatusCode(status_code);
                LogUtils.d(TAG, "Last status code is " + status_code);
                // TODO - get comment from q.850 state as well
                String status_text = UserAgentBroadcastReceiver.pjStrToString(pjInfo.getLast_status_text());
                session.setLastStatusComment(status_text);

                int reason_code = pjsua.get_event_reason_code(e);
                if (reason_code != 0) {
                    session.setLastReasonCode(reason_code);
                }
            }

            // 关于安全信息
            session.setSignalisationSecure(pjsua.call_secure_sig_level(session.getCallId()));
            String secureInfo = UserAgentBroadcastReceiver.pjStrToString(pjsua.call_secure_media_info(session.getCallId()));
            session.setMediaSecureInfo(secureInfo);
            session.setMediaSecure(!TextUtils.isEmpty(secureInfo));
            zrtp_state_info zrtpInfo = pjsua.jzrtp_getInfoFromCall(session.getCallId());
            session.setZrtpSASVerified(zrtpInfo.getSas_verified() == pjsuaConstants.PJ_TRUE);
            session.setHasZrtp(zrtpInfo.getSecure() == pjsuaConstants.PJ_TRUE);

            // 有关视频信息
            int vidStreamIdx = pjsua.call_get_vid_stream_idx(session.getCallId()); // 返回默认的视频流索引
            if (vidStreamIdx >= 0) {
                // 当前会叫是否指定视频流
                int hasVid = pjsua.call_vid_stream_is_running(session.getCallId(), vidStreamIdx, pjmedia_dir.PJMEDIA_DIR_DECODING);
                session.setMediaHasVideo((hasVid == pjsuaConstants.PJ_TRUE));
            }
        } else {
            LogUtils.d(TAG, "不存在于堆栈中的呼叫信息 - 假设它已被断开");
            session.setCallState(pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED.swigValue());
        }
    }

    /**
     * 复制PJSUA呼叫信息到SipCallSession对象
     *
     * @param session    复制信息到会话（输出）
     * @param pjCallInfo 来自pjsua的呼叫信息
     * @param context    上下文
     */
    private static void updateSession(SipCallSessionImpl session, pjsua_call_info pjCallInfo, Context context) {
        session.setCallId(pjCallInfo.getId());
        session.setCallState(pjCallInfo.getState().swigValue());
        session.setMediaStatus(pjCallInfo.getMedia_status().swigValue());
        session.setRemoteContact(UserAgentBroadcastReceiver.pjStrToString(pjCallInfo.getRemote_info())); // 远程信息
        session.setConfPort(pjCallInfo.getConf_slot());

        int pjAccId = pjCallInfo.getAcc_id();
        session.setAccId(UserAgentBroadcastReceiver.getAccountIdForPjsipId(context, pjAccId));

        pj_time_val duration = pjCallInfo.getConnect_duration();
        session.setConnectStart(SystemClock.elapsedRealtime() - duration.getSec() * 1000 - duration.getMsec());
    }

    /**
     * 拿到PJSIP的呼叫信息
     *
     * @param callId pjsip call id
     * @return Serialized information about this call
     * @throws SameThreadException
     */
    public static String dumpCallInfo(int callId) throws SameThreadException {
        return UserAgentBroadcastReceiver.pjStrToString(pjsua.call_dump(callId, pjsua.PJ_TRUE, " "));
    }
}