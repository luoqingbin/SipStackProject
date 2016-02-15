package com.crte.sipstackhome.models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;

import java.util.ArrayList;

/**
 * 通话记录
 * * <li>ID
 * <li>自己昵称
 * <li>自己SIP账号
 * <li>目标昵称
 * <li>目标SIP账号
 * <li>呼叫时间
 * <li>呼叫类型<来电、接听、未接听...>
 * <li>通话类型
 */
public class CallRecord extends BaseBean {
    public static final String[] FULL_PROJECTION = new String[]{
            CallRecord.FIELD_ID,
            CallRecord.FIELD_TO_USERNAME,
            CallRecord.FIELD_TO_SIP_URI,
            CallRecord.FIELD_FROM_USERNAME,
            CallRecord.FIELD_FROM_SIP_URI,
            CallRecord.FIELD_DATE,
            CallRecord.FIELD_CALL_TYPE,
            CallRecord.FIELD_TYPE,
            CallRecord.FIELD_RECORD_PATH
    };

    /**
     * 主键
     */
    public static final String FIELD_ID = "_id";
    /**
     * 用户昵称
     */
    public static final String FIELD_TO_USERNAME = "to_username";
    /**
     * 用户URI
     */
    public static final String FIELD_TO_SIP_URI = "to_sip_uri";
    /**
     * 目标昵称
     */
    public static final String FIELD_FROM_USERNAME = "from_username";
    /**
     * 目标URI
     */
    public static final String FIELD_FROM_SIP_URI = "from_sip_uri";
    /**
     * 时间
     */
    public static final String FIELD_DATE = "date";
    /**
     * 呼叫类型
     */
    public static final String FIELD_CALL_TYPE = "call_type";
    /**
     * 通话类型
     */
    public static final String FIELD_TYPE = "type";
    /**
     * 文件保存地址
     */
    public static final String FIELD_RECORD_PATH = "record_path";

    public int _id;
    public String toUsername;
    public String toSipUri;
    public String fromUsername;
    public String fromSipUri;
    public long date;
    public int callType;
    public int type;
    public String recordPath;

    /**
     * 插入一条数据
     */
    public static void insertDatas(ContentResolver contentResolver, CallRecord data) {
        ContentValues contentValues = getContentValuesDatas(data);
        contentResolver.insert(SipProfile.CONTACT_CALL_RECORD, contentValues);
    }

    /**
     * 查询所有信息
     *
     * @param contentResolver
     * @return
     */
    public static Cursor queryAllDatas(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_CALL_RECORD, null, FIELD_ID + " != -1", null, null);
        return cursor;
    }

    public static ArrayList<CallRecord> getCallRecordBean(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        ArrayList<CallRecord> callRecordArrayList = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                CallRecord callRecord = new CallRecord();
                callRecord._id = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID));
                callRecord.toUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_USERNAME));
                callRecord.toSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_SIP_URI));
                callRecord.fromUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_USERNAME));
                callRecord.fromSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_SIP_URI));
                callRecord.date = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_DATE));
                callRecord.callType = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_CALL_TYPE));
                callRecord.type = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_TYPE));
                callRecord.recordPath = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_RECORD_PATH));
                callRecordArrayList.add(callRecord);
            }
        } finally {
            cursor.close();
        }
        return callRecordArrayList;
    }

    public static ContentValues getContentValuesDatas(CallRecord datas) {
        if (datas == null) {
            return null;
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(FIELD_TO_USERNAME, datas.toUsername);
        contentValues.put(FIELD_TO_SIP_URI, datas.toSipUri);
        contentValues.put(FIELD_FROM_USERNAME, datas.fromUsername);
        contentValues.put(FIELD_FROM_SIP_URI, datas.fromSipUri);
        contentValues.put(FIELD_DATE, datas.date);
        contentValues.put(FIELD_CALL_TYPE, datas.callType);
        contentValues.put(FIELD_TYPE, datas.type);
        contentValues.put(FIELD_RECORD_PATH, datas.recordPath);
        return contentValues;
    }

    @Override
    public String toString() {
        return "CallRecord{" +
                "_id=" + _id +
                ", toUsername='" + toUsername + '\'' +
                ", toSipUri='" + toSipUri + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", fromSipUri='" + fromSipUri + '\'' +
                ", date=" + date +
                ", callType=" + callType +
                ", type=" + type +
                '}';
    }
}
