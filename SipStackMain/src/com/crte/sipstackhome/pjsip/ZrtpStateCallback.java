package com.crte.sipstackhome.pjsip;

import com.crte.sipstackhome.utils.log.LogUtils;

import org.pjsip.pjsua.ZrtpCallback;
import org.pjsip.pjsua.pj_str_t;

/**
 * Created by Administrator on 2015/12/22.
 */
public class ZrtpStateCallback extends ZrtpCallback {
    private static final String TAG = "ZrtpStateCallback";

    @Override
    public void on_zrtp_show_sas(int call_id, pj_str_t sas, int verified) {
        super.on_zrtp_show_sas(call_id, sas, verified);
        LogUtils.d(TAG, "on_zrtp_show_sas");
    }

    @Override
    public void on_zrtp_update_transport(int call_id) {
        super.on_zrtp_update_transport(call_id);
        LogUtils.d(TAG, "on_zrtp_update_transport");
    }
}
