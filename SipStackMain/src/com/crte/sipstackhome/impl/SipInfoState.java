package com.crte.sipstackhome.impl;

import android.app.Activity;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.db.DatabaseHelper;

public class SipInfoState {
    public static final int VIEW_TITLE = 1; // 标题
    public static final int VIEW_CONTENT = 2; // 基本内容
    public static final int VIEW_MESSAGE = 3; // 短信
    public static final int VIEW_PHONE = 4; // 通话记录
    public static final int VIEW_GROUP = 5; // 组信息
    public static final int VIEW_CALL = 6; // 拨号界面

    public static final String[] CONTACT_PERSON_FLAG = new String[]{DatabaseHelper.FIELD_ID,
            DatabaseHelper.FIELD_PHONE, DatabaseHelper.FIELD_HEADER,
            DatabaseHelper.FIELD_SIP_ADDRESS, DatabaseHelper.FIELD_USERNAME, DatabaseHelper.FIELD_SORT_LETTERS, DatabaseHelper.FIELD_FIRST
    };

    /**
     * 获得联系信息
     */
    public static Cursor getManagedCursor(Activity activity) {
        return activity.managedQuery(SipProfile.CONTACT_PERSON_CONTENT_URI, CONTACT_PERSON_FLAG, null, null, DatabaseContentProvider.DEFAULT_SORT_ORDER);
    }
}
