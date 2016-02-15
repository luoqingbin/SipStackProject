/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 *
 *  CSipSimple is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *  If you own a pjsip commercial license you can also redistribute it
 *  and/or modify it under the terms of the GNU Lesser General Public License
 *  as an android library.
 *
 *  CSipSimple is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.crte.sipstackhome.pjsip;

import android.content.Context;
import android.text.TextUtils;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.api.SipUri;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.utils.log.Log;
import com.crte.sipstackhome.utils.log.LogUtils;

import org.pjsip.pjsua.SWIGTYPE_p_pj_stun_auth_cred;
import org.pjsip.pjsua.csipsimple_acc_config;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjmedia_srtp_use;
import org.pjsip.pjsua.pjsip_auth_clt_pref;
import org.pjsip.pjsua.pjsip_cred_info;
import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;
import org.pjsip.pjsua.pjsua_acc_config;
import org.pjsip.pjsua.pjsua_ice_config;
import org.pjsip.pjsua.pjsua_ice_config_use;
import org.pjsip.pjsua.pjsua_ipv6_use;
import org.pjsip.pjsua.pjsua_stun_use;
import org.pjsip.pjsua.pjsua_transport_config;
import org.pjsip.pjsua.pjsua_turn_config;
import org.pjsip.pjsua.pjsua_turn_config_use;

import java.util.UUID;

public class PjSipAccount {
	private static final String TAG = "PjSipAccount";
	
	//private static final String THIS_FILE = "PjSipAcc";
	
	private String displayName;
	// For now everything is public, easiest to manage
	public String wizard;
	public boolean active;
	public pjsua_acc_config cfg;
	public csipsimple_acc_config css_cfg;
	public Long id;
	public Integer transport = 0;
	private int profile_vid_auto_show = -1;
	private int profile_vid_auto_transmit = -1;
    private int profile_enable_qos;
    private int profile_qos_dscp;
    private boolean profile_default_rtp_port = true;
	
	//private boolean hasZrtpValue = false;
	public PjSipAccount() {
		cfg = new pjsua_acc_config();
		pjsua.acc_config_default(cfg);
		
		css_cfg = new csipsimple_acc_config();
		pjsua.csipsimple_acc_config_default(css_cfg);
	}
	
	/**
	 * 初始化SipProfile信息
	 * @param profile 使用的profile文件
	 */
	public PjSipAccount(SipProfile profile) {
	    this();
	    
		if(profile.id != SipProfile.INVALID_ID) {
			id = profile.id;
		}
		
		displayName = profile.display_name;
		wizard = profile.wizard;
		transport = profile.transport;
		active = profile.active;
		transport = profile.transport;
		
		cfg.setPriority(profile.priority); // 设置账户优先级
		if(profile.acc_id != null) {
			cfg.setId(pjsua.pj_str_copy(profile.acc_id)); // 账户URI
		}
		if(profile.reg_uri != null) {
			cfg.setReg_uri(pjsua.pj_str_copy(profile.reg_uri)); // 注册URI
		}
		if(profile.publish_enabled != -1) {
			cfg.setPublish_enabled(profile.publish_enabled);
		}
		if(profile.reg_timeout != -1) {
			cfg.setReg_timeout(profile.reg_timeout);
		}
		if(profile.reg_delay_before_refresh != -1) {
			cfg.setReg_delay_before_refresh(profile.reg_delay_before_refresh);
		}
		if(profile.ka_interval != -1) {
			cfg.setKa_interval(profile.ka_interval);
		}
		if(profile.pidf_tuple_id != null) {
			cfg.setPidf_tuple_id(pjsua.pj_str_copy(profile.pidf_tuple_id));
		}
		if(profile.force_contact != null) {
			cfg.setForce_contact(pjsua.pj_str_copy(profile.force_contact));
		}
		
		cfg.setAllow_contact_rewrite(profile.allow_contact_rewrite ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
		cfg.setContact_rewrite_method(profile.contact_rewrite_method);
        cfg.setAllow_via_rewrite(profile.allow_via_rewrite ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
        cfg.setAllow_sdp_nat_rewrite(profile.allow_sdp_nat_rewrite ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
		
		if(profile.use_srtp != -1) {
			cfg.setUse_srtp(pjmedia_srtp_use.swigToEnum(profile.use_srtp));
			cfg.setSrtp_secure_signaling(0);
		}
		
		css_cfg.setUse_zrtp(profile.use_zrtp);
		
		if(profile.proxies != null) {
			Log.d("PjSipAccount", "Create proxy " + profile.proxies.length);
			cfg.setProxy_cnt(profile.proxies.length);
			pj_str_t[] proxies = cfg.getProxy();
			int i = 0;
			for(String proxy : profile.proxies) {
				Log.d("PjSipAccount", "Add proxy "+proxy);
				proxies[i] = pjsua.pj_str_copy(proxy);
				i += 1;
			}
			cfg.setProxy(proxies);
		}else {
			cfg.setProxy_cnt(0);
		}
		cfg.setReg_use_proxy(profile.reg_use_proxy);

		if(profile.username != null || profile.data != null) {
			cfg.setCred_count(1);
			pjsip_cred_info cred_info = cfg.getCred_info();
			
			if(profile.realm != null) {
				cred_info.setRealm(pjsua.pj_str_copy(profile.realm));
			}
			if(profile.username != null) {
				cred_info.setUsername(pjsua.pj_str_copy(profile.username));
			}
			if(profile.datatype != -1) {
				cred_info.setData_type(profile.datatype);
			}
			if(profile.data != null) {
				cred_info.setData(pjsua.pj_str_copy(profile.data));
			}
		}else {
			cfg.setCred_count(0);
		}
		
		// Auth prefs
		{
		     pjsip_auth_clt_pref authPref = cfg.getAuth_pref();
    		 authPref.setInitial_auth(profile.initial_auth ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
    		 if(!TextUtils.isEmpty(profile.auth_algo)) {
    		     authPref.setAlgorithm(pjsua.pj_str_copy(profile.auth_algo));
    		 }
    		 cfg.setAuth_pref(authPref);
		}
		
        cfg.setMwi_enabled(profile.mwi_enabled ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
        cfg.setIpv6_media_use(profile.ipv6_media_use == 1 ? pjsua_ipv6_use.PJSUA_IPV6_ENABLED
                : pjsua_ipv6_use.PJSUA_IPV6_DISABLED);
		
		// RFC5626
		cfg.setUse_rfc5626(profile.use_rfc5626? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
		if(!TextUtils.isEmpty(profile.rfc5626_instance_id)) {
		    cfg.setRfc5626_instance_id(pjsua.pj_str_copy(profile.rfc5626_instance_id));
		}
		if(!TextUtils.isEmpty(profile.rfc5626_reg_id)) {
            cfg.setRfc5626_reg_id(pjsua.pj_str_copy(profile.rfc5626_reg_id));
        }
		
		// Video
		profile_vid_auto_show = profile.vid_in_auto_show;
		profile_vid_auto_transmit = profile.vid_out_auto_transmit;
		
		// Rtp cfg
		pjsua_transport_config rtpCfg = cfg.getRtp_cfg();
		if(profile.rtp_port >= 0) {
		    rtpCfg.setPort(profile.rtp_port);
		    profile_default_rtp_port = false;
		}
		if(!TextUtils.isEmpty(profile.rtp_public_addr)) {
		    rtpCfg.setPublic_addr(pjsua.pj_str_copy(profile.rtp_public_addr));
		}
        if(!TextUtils.isEmpty(profile.rtp_bound_addr)) {
            rtpCfg.setBound_addr(pjsua.pj_str_copy(profile.rtp_bound_addr));
        }
        
        profile_enable_qos = profile.rtp_enable_qos;
        profile_qos_dscp = profile.rtp_qos_dscp;
        
        cfg.setSip_stun_use(profile.sip_stun_use == 0 ? pjsua_stun_use.PJSUA_STUN_USE_DISABLED : pjsua_stun_use.PJSUA_STUN_USE_DEFAULT);
        cfg.setMedia_stun_use(profile.media_stun_use == 0 ? pjsua_stun_use.PJSUA_STUN_USE_DISABLED : pjsua_stun_use.PJSUA_STUN_USE_DEFAULT);
        if(profile.ice_cfg_use == 1) {
            cfg.setIce_cfg_use(pjsua_ice_config_use.PJSUA_ICE_CONFIG_USE_CUSTOM);
            pjsua_ice_config iceCfg = cfg.getIce_cfg();
            iceCfg.setEnable_ice( (profile.ice_cfg_enable == 1 )? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
        }else {
            cfg.setIce_cfg_use(pjsua_ice_config_use.PJSUA_ICE_CONFIG_USE_DEFAULT);
        }
        if(profile.turn_cfg_use == 1) {
            cfg.setTurn_cfg_use(pjsua_turn_config_use.PJSUA_TURN_CONFIG_USE_CUSTOM);
            pjsua_turn_config turnCfg = cfg.getTurn_cfg();
            SWIGTYPE_p_pj_stun_auth_cred creds = turnCfg.getTurn_auth_cred();
            turnCfg.setEnable_turn( (profile.turn_cfg_enable == 1) ? pjsuaConstants.PJ_TRUE : pjsuaConstants.PJ_FALSE);
            turnCfg.setTurn_server( pjsua.pj_str_copy(profile.turn_cfg_server) );
            pjsua.set_turn_credentials(pjsua.pj_str_copy(profile.turn_cfg_user), pjsua.pj_str_copy(profile.turn_cfg_password), pjsua.pj_str_copy("*"), creds);
            // Normally this step is useless as manipulating a pointer in C memory at this point, but in case this changes reassign
            turnCfg.setTurn_auth_cred(creds);
        }else {
            cfg.setTurn_cfg_use(pjsua_turn_config_use.PJSUA_TURN_CONFIG_USE_DEFAULT);
        }

		// Video
		cfg.setVid_in_auto_show(pjsuaConstants.PJ_TRUE);
		cfg.setVid_out_auto_transmit(pjsuaConstants.PJ_TRUE);
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	@Override
	public boolean equals(Object o) {
		if(o != null && o.getClass() == PjSipAccount.class) {
			PjSipAccount oAccount = (PjSipAccount) o;
			return oAccount.id == id;
		}
		return super.equals(o);
	}

	/**
	 * 构建用户基本信息
	 */
	public static SipProfile buildAccount(SipProfile account, String username, String password, String service) {
		LogUtils.i(TAG, "构建用户...");
		account.display_name = username;

		String[] serverParts = service.split(":");
		// 拼接：<sip:100@192.168.1.12>
		account.acc_id = "<sip:" + SipUri.encodeUser(username) + "@" + serverParts[0].trim() + ">";

		// 注册URI：sip:192.168.1.12
		String regUri = "sip:" + service;
		account.reg_uri = regUri;
		account.proxies = new String[]{regUri};

		account.realm = "*";
		account.username = username;
		account.data = password;
		account.scheme = SipProfile.CRED_SCHEME_DIGEST;
		account.datatype = SipProfile.CRED_DATA_PLAIN_PASSWD;
		// 默认传输协议 UDP
		account.transport = SipProfile.TRANSPORT_UDP;
		return account;
	}

	public static void applyNewAccountDefault(SipProfile account) {
		if (account.use_rfc5626) {
			if (TextUtils.isEmpty(account.rfc5626_instance_id)) {
				String autoInstanceId = (UUID.randomUUID()).toString();
				account.rfc5626_instance_id = "<urn:uuid:" + autoInstanceId + ">";
			}
		}
	}

	public static SipProfile getAccount(Context context, long accountId) {
		return SipProfile.getProfileFromDbId(context, accountId, DatabaseContentProvider.ACCOUNT_FULL_PROJECTION);
	}
}
