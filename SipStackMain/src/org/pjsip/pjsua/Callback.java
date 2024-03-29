/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (http://www.swig.org).
 * Version 2.0.12
 *
 * Do not make changes to this file unless you know what you are doing--modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua;

/**
 * 这个类描述应用程序回调来自PJSUA的各种事件通知，所有的回调都是可选的，但也有一些程序需要实现的重要回调（如on_incoming_call）<br/>
 * URL: http://www.pjsip.org/pjsip/docs/html/structpjsua__callback.htm#ad1e853098b7200d625bee109e0044b38
 *
 * @author Administrator
 */
public class Callback {
    private long swigCPtr;
    protected boolean swigCMemOwn;

    protected Callback(long cPtr, boolean cMemoryOwn) {
        swigCMemOwn = cMemoryOwn;
        swigCPtr = cPtr;
    }

    protected static long getCPtr(Callback obj) {
        return (obj == null) ? 0 : obj.swigCPtr;
    }

    protected void finalize() {
        delete();
    }

    public synchronized void delete() {
        if (swigCPtr != 0) {
            if (swigCMemOwn) {
                swigCMemOwn = false;
                pjsuaJNI.delete_Callback(swigCPtr);
            }
            swigCPtr = 0;
        }
    }

    protected void swigDirectorDisconnect() {
        swigCMemOwn = false;
        delete();
    }

    public void swigReleaseOwnership() {
        swigCMemOwn = false;
        pjsuaJNI.Callback_change_ownership(this, swigCPtr, false);
    }

    public void swigTakeOwnership() {
        swigCMemOwn = true;
        pjsuaJNI.Callback_change_ownership(this, swigCPtr, true);
    }

    /**
     * 通知应用程序，呼叫的状态发生了变化<br/>
     * 可以通过pjsua_call_get_info()获得通话详情状态 <br/>
     *
     * @param call_id 呼叫索引
     * @param e       导致呼叫改变的状态
     */
    public void on_call_state(int call_id, pjsip_event e) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_state(swigCPtr, this, call_id, pjsip_event.getCPtr(e), e);
        else
            pjsuaJNI.Callback_on_call_stateSwigExplicitCallback(swigCPtr, this, call_id, pjsip_event.getCPtr(e), e);
    }

    /**
     * 通知应用程序 <br/>
     * 监听语音、视频来电呼叫
     *
     * @param acc_id  发起呼叫的账户ID
     * @param call_id 目标呼叫ID
     * @param rdata   INVITE请求
     */
    public void on_incoming_call(int acc_id, int call_id, SWIGTYPE_p_pjsip_rx_data rdata) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_incoming_call(swigCPtr, this, acc_id, call_id, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
        else
            pjsuaJNI.Callback_on_incoming_callSwigExplicitCallback(swigCPtr, this, acc_id, call_id, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
    }

    /**
     * 一般通知回调，在事物发生改变时调用<br/>
     * 应用程序可以实现这个回调，监控输出请求回调，或者处理回答传入的请求与最终响应
     *
     * @param call_id 呼叫识别ID
     * @param tsx     改变状态的事物
     * @param e       引起状态变化的事物事件
     */
    public void on_call_tsx_state(int call_id, SWIGTYPE_p_pjsip_transaction tsx, pjsip_event e) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_tsx_state(swigCPtr, this, call_id, SWIGTYPE_p_pjsip_transaction.getCPtr(tsx), pjsip_event.getCPtr(e), e);
        else
            pjsuaJNI.Callback_on_call_tsx_stateSwigExplicitCallback(swigCPtr, this, call_id, SWIGTYPE_p_pjsip_transaction.getCPtr(tsx), pjsip_event.getCPtr(e), e);
    }

    /**
     * 通知应用程序，通话媒体状态发生了变化。正确的应用程序需要实现这个回调。<br/>
     * 如：接通电话的媒体声音设备。
     *
     * @param call_id 呼叫索引
     */
    public void on_call_media_state(int call_id) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_media_state(swigCPtr, this, call_id);
        else pjsuaJNI.Callback_on_call_media_stateSwigExplicitCallback(swigCPtr, this, call_id);
    }

    /**
     * 通知应用程序刚刚创建一个SDP（创建新的或使用之前的SDP提供应答），应用程序可以实现这个回调修改SDP，例如修改
     * 编码器优先级或添加自定义或专有SDP属性
     *
     * @param call_id
     * @param sdp
     * @param pool
     * @param rem_sdp
     */
    public void on_call_sdp_created(int call_id, SWIGTYPE_p_pjmedia_sdp_session sdp, pj_pool_t pool, SWIGTYPE_p_pjmedia_sdp_session rem_sdp) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_sdp_created(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_sdp_session.getCPtr(sdp), pj_pool_t.getCPtr(pool), pool, SWIGTYPE_p_pjmedia_sdp_session.getCPtr(rem_sdp));
        else
            pjsuaJNI.Callback_on_call_sdp_createdSwigExplicitCallback(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_sdp_session.getCPtr(sdp), pj_pool_t.getCPtr(pool), pool, SWIGTYPE_p_pjmedia_sdp_session.getCPtr(rem_sdp));
    }

    public void on_stream_created(int call_id, SWIGTYPE_p_pjmedia_stream strm, long stream_idx, SWIGTYPE_p_p_pjmedia_port p_port) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_stream_created(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_stream.getCPtr(strm), stream_idx, SWIGTYPE_p_p_pjmedia_port.getCPtr(p_port));
        else
            pjsuaJNI.Callback_on_stream_createdSwigExplicitCallback(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_stream.getCPtr(strm), stream_idx, SWIGTYPE_p_p_pjmedia_port.getCPtr(p_port));
    }

    public void on_stream_destroyed(int call_id, SWIGTYPE_p_pjmedia_stream strm, long stream_idx) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_stream_destroyed(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_stream.getCPtr(strm), stream_idx);
        else
            pjsuaJNI.Callback_on_stream_destroyedSwigExplicitCallback(swigCPtr, this, call_id, SWIGTYPE_p_pjmedia_stream.getCPtr(strm), stream_idx);
    }

    public void on_dtmf_digit(int call_id, int digit) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_dtmf_digit(swigCPtr, this, call_id, digit);
        else pjsuaJNI.Callback_on_dtmf_digitSwigExplicitCallback(swigCPtr, this, call_id, digit);
    }

    public void on_call_transfer_request(int call_id, pj_str_t dst, SWIGTYPE_p_pjsip_status_code code) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_transfer_request(swigCPtr, this, call_id, pj_str_t.getCPtr(dst), dst, SWIGTYPE_p_pjsip_status_code.getCPtr(code));
        else
            pjsuaJNI.Callback_on_call_transfer_requestSwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(dst), dst, SWIGTYPE_p_pjsip_status_code.getCPtr(code));
    }

    public void on_call_transfer_status(int call_id, int st_code, pj_str_t st_text, int final_, SWIGTYPE_p_int p_cont) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_transfer_status(swigCPtr, this, call_id, st_code, pj_str_t.getCPtr(st_text), st_text, final_, SWIGTYPE_p_int.getCPtr(p_cont));
        else
            pjsuaJNI.Callback_on_call_transfer_statusSwigExplicitCallback(swigCPtr, this, call_id, st_code, pj_str_t.getCPtr(st_text), st_text, final_, SWIGTYPE_p_int.getCPtr(p_cont));
    }

    public void on_call_replace_request(int call_id, SWIGTYPE_p_pjsip_rx_data rdata, SWIGTYPE_p_int st_code, pj_str_t st_text) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_replace_request(swigCPtr, this, call_id, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata), SWIGTYPE_p_int.getCPtr(st_code), pj_str_t.getCPtr(st_text), st_text);
        else
            pjsuaJNI.Callback_on_call_replace_requestSwigExplicitCallback(swigCPtr, this, call_id, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata), SWIGTYPE_p_int.getCPtr(st_code), pj_str_t.getCPtr(st_text), st_text);
    }

    public void on_call_replaced(int old_call_id, int new_call_id) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_replaced(swigCPtr, this, old_call_id, new_call_id);
        else
            pjsuaJNI.Callback_on_call_replacedSwigExplicitCallback(swigCPtr, this, old_call_id, new_call_id);
    }

    public void on_reg_state(int acc_id) {
        if (getClass() == Callback.class) pjsuaJNI.Callback_on_reg_state(swigCPtr, this, acc_id);
        else pjsuaJNI.Callback_on_reg_stateSwigExplicitCallback(swigCPtr, this, acc_id);
    }

    public void on_buddy_state(int buddy_id) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_buddy_state(swigCPtr, this, buddy_id);
        else pjsuaJNI.Callback_on_buddy_stateSwigExplicitCallback(swigCPtr, this, buddy_id);
    }

    public void on_pager(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type, pj_str_t body) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_pager(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body);
        else
            pjsuaJNI.Callback_on_pagerSwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body);
    }

    public void on_pager2(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, pj_str_t mime_type, pj_str_t body, SWIGTYPE_p_pjsip_rx_data rdata) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_pager2(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
        else
            pjsuaJNI.Callback_on_pager2SwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body, SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
    }

    public void on_pager_status(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_pager_status(swigCPtr, this, call_id, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(body), body, status.swigValue(), pj_str_t.getCPtr(reason), reason);
        else
            pjsuaJNI.Callback_on_pager_statusSwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(body), body, status.swigValue(), pj_str_t.getCPtr(reason), reason);
    }

    public void on_pager_status2(int call_id, pj_str_t to, pj_str_t body, pjsip_status_code status, pj_str_t reason, SWIGTYPE_p_pjsip_tx_data tdata, SWIGTYPE_p_pjsip_rx_data rdata) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_pager_status2(swigCPtr, this, call_id, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(body), body, status.swigValue(), pj_str_t.getCPtr(reason), reason, SWIGTYPE_p_pjsip_tx_data.getCPtr(tdata), SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
        else
            pjsuaJNI.Callback_on_pager_status2SwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(body), body, status.swigValue(), pj_str_t.getCPtr(reason), reason, SWIGTYPE_p_pjsip_tx_data.getCPtr(tdata), SWIGTYPE_p_pjsip_rx_data.getCPtr(rdata));
    }

    public void on_typing(int call_id, pj_str_t from, pj_str_t to, pj_str_t contact, int is_typing) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_typing(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, is_typing);
        else
            pjsuaJNI.Callback_on_typingSwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(from), from, pj_str_t.getCPtr(to), to, pj_str_t.getCPtr(contact), contact, is_typing);
    }

    public void on_nat_detect(pj_stun_nat_detect_result res) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_nat_detect(swigCPtr, this, pj_stun_nat_detect_result.getCPtr(res), res);
        else
            pjsuaJNI.Callback_on_nat_detectSwigExplicitCallback(swigCPtr, this, pj_stun_nat_detect_result.getCPtr(res), res);
    }

    public pjsip_redirect_op on_call_redirected(int call_id, pj_str_t target) {
        return pjsip_redirect_op.swigToEnum((getClass() == Callback.class) ? pjsuaJNI.Callback_on_call_redirected(swigCPtr, this, call_id, pj_str_t.getCPtr(target), target) : pjsuaJNI.Callback_on_call_redirectedSwigExplicitCallback(swigCPtr, this, call_id, pj_str_t.getCPtr(target), target));
    }

    public void on_mwi_info(int acc_id, pj_str_t mime_type, pj_str_t body) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_mwi_info(swigCPtr, this, acc_id, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body);
        else
            pjsuaJNI.Callback_on_mwi_infoSwigExplicitCallback(swigCPtr, this, acc_id, pj_str_t.getCPtr(mime_type), mime_type, pj_str_t.getCPtr(body), body);
    }

    public void on_call_media_transport_state(int call_id, pjsua_med_tp_state_info info) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_call_media_transport_state(swigCPtr, this, call_id, pjsua_med_tp_state_info.getCPtr(info), info);
        else
            pjsuaJNI.Callback_on_call_media_transport_stateSwigExplicitCallback(swigCPtr, this, call_id, pjsua_med_tp_state_info.getCPtr(info), info);
    }

    public int on_validate_audio_clock_rate(int clock_rate) {
        return (getClass() == Callback.class) ? pjsuaJNI.Callback_on_validate_audio_clock_rate(swigCPtr, this, clock_rate) : pjsuaJNI.Callback_on_validate_audio_clock_rateSwigExplicitCallback(swigCPtr, this, clock_rate);
    }

    public void on_setup_audio(int before_init) {
        if (getClass() == Callback.class)
            pjsuaJNI.Callback_on_setup_audio(swigCPtr, this, before_init);
        else pjsuaJNI.Callback_on_setup_audioSwigExplicitCallback(swigCPtr, this, before_init);
    }

    public void on_teardown_audio() {
        if (getClass() == Callback.class) pjsuaJNI.Callback_on_teardown_audio(swigCPtr, this);
        else pjsuaJNI.Callback_on_teardown_audioSwigExplicitCallback(swigCPtr, this);
    }

    public int on_set_micro_source() {
        return (getClass() == Callback.class) ? pjsuaJNI.Callback_on_set_micro_source(swigCPtr, this) : pjsuaJNI.Callback_on_set_micro_sourceSwigExplicitCallback(swigCPtr, this);
    }

    public int timer_schedule(int entry, int entryId, int time) {
        return (getClass() == Callback.class) ? pjsuaJNI.Callback_timer_schedule(swigCPtr, this, entry, entryId, time) : pjsuaJNI.Callback_timer_scheduleSwigExplicitCallback(swigCPtr, this, entry, entryId, time);
    }

    public int timer_cancel(int entry, int entryId) {
        return (getClass() == Callback.class) ? pjsuaJNI.Callback_timer_cancel(swigCPtr, this, entry, entryId) : pjsuaJNI.Callback_timer_cancelSwigExplicitCallback(swigCPtr, this, entry, entryId);
    }

    public Callback() {
        this(pjsuaJNI.new_Callback(), true);
        pjsuaJNI.Callback_director_connect(this, swigCPtr, swigCMemOwn, false);
    }

}

