package com.crte.sipstackhome.models;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;

import java.util.ArrayList;

/**
 * Created by wangz on 2016/1/2.
 */
public class VideoBean {
    public static final String[] FULL_PROJECTION = new String[]{
            VideoBean.FIELD_ID,
            VideoBean.FIELD_TO_USERNAME,
            VideoBean.FIELD_TO_SIP_URI,
            VideoBean.FIELD_FROM_USERNAME,
            VideoBean.FIELD_FROM_SIP_URI,
            VideoBean.FIELD_DATE,
            VideoBean.FIELD_CALL_TYPE
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

    public int _id;
    public String toUsername;
    public String toSipUri;
    public String fromUsername;
    public String fromSipUri;
    public long date;
    public int callType;

    /**
     * 插入一条数据
     */
    public static void insertDatas(ContentResolver contentResolver, VideoBean data) {
        ContentValues contentValues = getContentValuesDatas(data);
        contentResolver.insert(SipProfile.CONTACT_VIDEO_RECORD, contentValues);
    }

    /**
     * 查询所有信息
     * @param contentResolver
     * @return
     */
    public static Cursor queryAllDatas(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_VIDEO_RECORD, null, FIELD_ID + " != -1", null, null);
        return cursor;
    }

    public static ArrayList<VideoBean> getVideoBean(Cursor cursor) {
        if(cursor == null) {
            return null;
        }

        ArrayList<VideoBean> videoBeanArrayList = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                VideoBean videoBean = new VideoBean();
                videoBean._id = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID));
                videoBean.toUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_USERNAME));
                videoBean.toSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_SIP_URI));
                videoBean.fromUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_USERNAME));
                videoBean.fromSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_SIP_URI));
                videoBean.date = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_DATE));
                videoBean.callType = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_CALL_TYPE));
                videoBeanArrayList.add(videoBean);
            }
        } finally {
            cursor.close();
        }
        return videoBeanArrayList;
    }

    /**
     * 获得ContentValues
     */
    public static ContentValues getContentValuesDatas(VideoBean datas) {
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
        return contentValues;
    }

    @Override
    public String toString() {
        return "VideoBean{" +
                "_id=" + _id +
                ", toUsername='" + toUsername + '\'' +
                ", toSipUri='" + toSipUri + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", fromSipUri='" + fromSipUri + '\'' +
                ", date=" + date +
                ", callType=" + callType +
                '}';
    }
}
