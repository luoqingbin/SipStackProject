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

package com.crte.sipstackhome.utils;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.crte.sipstackhome.api.SipCallSession;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.api.SipProfileState;
import com.crte.sipstackhome.utils.log.Log;

public class AccountListUtils {

	public static final class AccountStatusDisplay {
		public String statusLabel;
		/**
		 * 状态颜色
		 */
		public int statusColor;
		/**
		 * 复选框指示器
		 */
		public int checkBoxIndicator;
		/**
		 * 可用于电话
		 */
		public boolean availableForCalls;
	}

	private static final String THIS_FILE = "AccountListUtils";
	
	/**
	 * 设置账户的状态显示
	 * @param context
	 * @param accountId
	 * @return
	 */
	public static AccountStatusDisplay getAccountDisplay(Context context, long accountId) {
		AccountStatusDisplay accountDisplay = new AccountStatusDisplay();
		// 默认属性
//		accountDisplay.statusLabel = context.getString(R.string.acct_inactive); // 默认状态：未活动的
//		final Resources resources = context.getResources();
//		accountDisplay.statusColor = resources.getColor(R.color.account_inactive); 
//		accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
		accountDisplay.availableForCalls = false;
		
		SipProfileState accountInfo = null;
		if(accountId < 0) {
			return accountDisplay;
		}
		
		try {
			// 查询指定用户
            Cursor c = context.getContentResolver().query(ContentUris.withAppendedId(SipProfile.ACCOUNT_STATUS_ID_URI_BASE, accountId), null, null, null, null);
    		if (c != null) {
    			try {
    				if(c.getCount() > 0) {
    					c.moveToFirst();
    					accountInfo = new SipProfileState(c);
    				}
    			} catch (Exception e) {
    				Log.e(THIS_FILE, "Error on looping over sip profiles states", e);
    			} finally {
    				c.close();
    			}
    		}
		}catch(Exception e) {
		    Log.e(THIS_FILE, "Failed account id " + accountId);
		}
		
		// 根据状态显示不同用户信息
		if (accountInfo != null && accountInfo.isActive()) {
			if (accountInfo.getAddedStatus() >= SipManager.SUCCESS) {
				// 默认 -- 未注册
//				accountDisplay.statusLabel = context.getString(R.string.acct_unregistered);
//				accountDisplay.statusColor = resources.getColor(R.color.account_unregistered);
//				accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
				if( TextUtils.isEmpty( accountInfo.getRegUri()) ) {
					// 已经注册
//					accountDisplay.statusColor = resources.getColor(R.color.account_valid);
//					accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_on;
//					accountDisplay.statusLabel = context.getString(R.string.acct_registered);
					accountDisplay.availableForCalls = true;
				}else if (accountInfo.isAddedToStack()) {
					String pjStat = accountInfo.getStatusText();	// 仅用于错误状态信息
					int statusCode = accountInfo.getStatusCode();
					if (statusCode == SipCallSession.StatusCode.OK) {
						// Log.d(THIS_FILE,
						// "Now account "+account.display_name+" has expires "+accountInfo.getExpires());
						if (accountInfo.getExpires() > 0) {
							// 绿色 已经注册
//							accountDisplay.statusColor = resources.getColor(R.color.account_valid);
//							accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_on;
//							accountDisplay.statusLabel = context.getString(R.string.acct_registered);
							accountDisplay.availableForCalls = true;
						} else {
							// 黄色 未注册
//							accountDisplay.statusColor = resources.getColor(R.color.account_unregistered);
//							accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
//							accountDisplay.statusLabel = context.getString(R.string.acct_unregistered);
						}
					} else if(statusCode != -1 ){
						if (statusCode == SipCallSession.StatusCode.PROGRESS || statusCode == SipCallSession.StatusCode.TRYING) {
							// 黄色 正在注册...
//							accountDisplay.statusColor = resources.getColor(R.color.account_unregistered);
//							accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
//							accountDisplay.statusLabel = context.getString(R.string.acct_registering);
						} else {
							// TODO : 特殊信息403
							// 红色 : 注册时发生错误
//							accountDisplay.statusColor = resources.getColor(R.color.account_error);
//							accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_red;
//							accountDisplay.statusLabel = context.getString(R.string.acct_regerror) + " - " + pjStat;	// Why can't ' - ' be in resource?
						}
					}else {
						// 正在注册
						// Account is currently registering (added to pjsua but not replies yet from pjsua registration)
//						accountDisplay.statusColor = resources.getColor(R.color.account_inactive);
//						accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
//						accountDisplay.statusLabel = context.getString(R.string.acct_registering);
					}
				}
			} else {
				if(accountInfo.isAddedToStack()) {
					// 注册失败
//					accountDisplay.statusLabel = context.getString(R.string.acct_regfailed);
//					accountDisplay.statusColor = resources.getColor(R.color.account_error);
				}else {
					// 正在注册...
//					accountDisplay.statusColor = resources.getColor(R.color.account_inactive);
//					accountDisplay.checkBoxIndicator = R.drawable.ic_indicator_yellow;
//					accountDisplay.statusLabel = context.getString(R.string.acct_registering);
					
				}
			}
		}
		return accountDisplay;
	}
	
	
	
}
