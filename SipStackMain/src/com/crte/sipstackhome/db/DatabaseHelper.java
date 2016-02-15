package com.crte.sipstackhome.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.models.BaseBean;
import com.crte.sipstackhome.models.CallRecord;
import com.crte.sipstackhome.models.Contacts;
import com.crte.sipstackhome.models.ShortMessage;

/**
 * 数据库
 * Created by Torment on 2015/12/21.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_SHORT_MESSAGE_NAME = "short_message";
    public static final String TABLE_CONTACT_PERSON = "contact_person";
    public static final String TABLE_CALL_RECORD = "call_record";
    public static final String TABLE_VIDEO_RECORD = "video_record";

    public static final String FIELD_ID = "_id";
    public static final String FIELD_PID = "pid";
    public static final String FIELD_USER_ID = "userId";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_MESSAGE = "message";
    public static final String FIELD_TIME = "date";
    public static final String FIELD_SIP_ADDRESS = "sipAddress";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_HEADER = "header";
    public static final String FIELD_FLAG = "flag";
    public static final String FIELD_SORT_LETTERS = "sortLetters";
    public static final String FIELD_FIRST = "isFister";
    public static final String FIELD_CALL_STATE = "callState";
    public static final String FIELD_CALL_ADDRESS = "callAddress";
    public static final String FIELD_COLOR = "color";

    /**
     * 账户信息
     */
    private static final String TABLE_ACCOUNT_CREATE = "CREATE TABLE IF NOT EXISTS "
            + SipProfile.ACCOUNTS_TABLE_NAME
            + " ("
            // 主键
            + SipProfile.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"

            // 账户活动状态
            + SipProfile.FIELD_ACTIVE + " INTEGER,"
            // 向导标记
            + SipProfile.FIELD_WIZARD + " TEXT,"
            // 显示的账户名称
            + SipProfile.FIELD_DISPLAY_NAME + " TEXT,"

            // 账户优先级
            + SipProfile.FIELD_PRIORITY + " INTEGER,"
            // 账户ID
            + SipProfile.FIELD_ACC_ID + " TEXT NOT NULL,"
            // 注册URI
            + SipProfile.FIELD_REG_URI + " TEXT,"
            // ??
            + SipProfile.FIELD_MWI_ENABLED + " BOOLEAN,"
            // 是否将信息保存到SIP服务器上
            + SipProfile.FIELD_PUBLISH_ENABLED + " INTEGER,"
            // 注册超时时间
            + SipProfile.FIELD_REG_TIMEOUT + " INTEGER,"
            // 传输间隔时间
            + SipProfile.FIELD_KA_INTERVAL + " INTEGER,"
            // ??
            + SipProfile.FIELD_PIDF_TUPLE_ID + " TEXT,"
            // 可选的URI关联账户，建议为空
            + SipProfile.FIELD_FORCE_CONTACT + " TEXT,"
            + SipProfile.FIELD_ALLOW_CONTACT_REWRITE + " INTEGER,"
            + SipProfile.FIELD_CONTACT_REWRITE_METHOD + " INTEGER,"
            + SipProfile.FIELD_CONTACT_PARAMS + " TEXT,"
            + SipProfile.FIELD_CONTACT_URI_PARAMS + " TEXT,"
            + SipProfile.FIELD_TRANSPORT + " INTEGER,"
            + SipProfile.FIELD_DEFAULT_URI_SCHEME + " TEXT,"
            + SipProfile.FIELD_USE_SRTP + " INTEGER,"
            + SipProfile.FIELD_USE_ZRTP + " INTEGER,"

            // Proxy infos
            + SipProfile.FIELD_PROXY + " TEXT,"
            + SipProfile.FIELD_REG_USE_PROXY + " INTEGER,"

            // And now cred_info since for now only one cred info can be managed
            // In future release a credential table should be created
            + SipProfile.FIELD_REALM + " TEXT,"
            + SipProfile.FIELD_SCHEME + " TEXT,"
            + SipProfile.FIELD_USERNAME + " TEXT,"
            + SipProfile.FIELD_DATATYPE + " INTEGER,"
            + SipProfile.FIELD_DATA + " TEXT,"
            + SipProfile.FIELD_AUTH_INITIAL_AUTH + " INTEGER,"
            + SipProfile.FIELD_AUTH_ALGO + " TEXT,"

            + SipProfile.FIELD_SIP_STACK + " INTEGER,"
            + SipProfile.FIELD_VOICE_MAIL_NBR + " TEXT,"
            + SipProfile.FIELD_REG_DELAY_BEFORE_REFRESH + " INTEGER,"

            + SipProfile.FIELD_TRY_CLEAN_REGISTERS + " INTEGER,"

            + SipProfile.FIELD_USE_RFC5626 + " INTEGER DEFAULT 1,"
            + SipProfile.FIELD_RFC5626_INSTANCE_ID + " TEXT,"
            + SipProfile.FIELD_RFC5626_REG_ID + " TEXT,"

            + SipProfile.FIELD_VID_IN_AUTO_SHOW + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_VID_OUT_AUTO_TRANSMIT + " INTEGER DEFAULT -1,"

            + SipProfile.FIELD_RTP_PORT + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_ENABLE_QOS + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_QOS_DSCP + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_RTP_BOUND_ADDR + " TEXT,"
            + SipProfile.FIELD_RTP_PUBLIC_ADDR + " TEXT,"
            + SipProfile.FIELD_ANDROID_GROUP + " TEXT,"
            + SipProfile.FIELD_ALLOW_VIA_REWRITE + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_ALLOW_SDP_NAT_REWRITE + " INTEGER  DEFAULT 0,"
            + SipProfile.FIELD_SIP_STUN_USE + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_MEDIA_STUN_USE + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_ICE_CFG_USE + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_ICE_CFG_ENABLE + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_TURN_CFG_USE + " INTEGER DEFAULT -1,"
            + SipProfile.FIELD_TURN_CFG_ENABLE + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_TURN_CFG_SERVER + " TEXT,"
            + SipProfile.FIELD_TURN_CFG_USER + " TEXT,"
            + SipProfile.FIELD_TURN_CFG_PASSWORD + " TEXT,"
            + SipProfile.FIELD_IPV6_MEDIA_USE + " INTEGER DEFAULT 0,"
            + SipProfile.FIELD_WIZARD_DATA + " TEXT"
            + ");";

    /**
     * 通话记录
     */
    public final static String CREATE_TABLE_CALL_RECORD = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CALL_RECORD
            + " ("
            + CallRecord.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CallRecord.FIELD_TO_USERNAME + " TEXT,"
            + CallRecord.FIELD_TO_SIP_URI + " TEXT,"
            + CallRecord.FIELD_FROM_USERNAME + " TEXT,"
            + CallRecord.FIELD_FROM_SIP_URI + " TEXT,"
            + CallRecord.FIELD_DATE + " INTEGER,"
            + CallRecord.FIELD_CALL_TYPE + " INTEGER,"
            + CallRecord.FIELD_TYPE + " INTEGER,"
            + CallRecord.FIELD_RECORD_PATH + " TEXT,"
            + BaseBean.FIELD_PID + " INTEGER KEY REFERENCES " + TABLE_CONTACT_PERSON + "(" + Contacts.FIELD_ID + ") ON DELETE CASCADE"
            + ");";

    /**
     * 语音记录
     */
    public final static String CREATE_TABLE_VIDEO_RECORD = "CREATE TABLE IF NOT EXISTS "
            + TABLE_VIDEO_RECORD
            + " ("
            + CallRecord.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + CallRecord.FIELD_TO_USERNAME + " TEXT,"
            + CallRecord.FIELD_TO_SIP_URI + " TEXT,"
            + CallRecord.FIELD_FROM_USERNAME + " TEXT,"
            + CallRecord.FIELD_FROM_SIP_URI + " TEXT,"
            + CallRecord.FIELD_DATE + " INTEGER,"
            + CallRecord.FIELD_CALL_TYPE + " INTEGER"
            + ");";

    public static final String CREATE_TABLE_MESSAGE = "CREATE TABLE IF NOT EXISTS "
            + TABLE_SHORT_MESSAGE_NAME
            + " ("
            + ShortMessage.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ShortMessage.FIELD_TO_USERNAME + " TEXT,"
            + ShortMessage.FIELD_TO_SIP_URI + " TEXT,"
            + ShortMessage.FIELD_FROM_USERNAME + " TEXT,"
            + ShortMessage.FIELD_FROM_SIP_URI + " TEXT,"
            + ShortMessage.FIELD_DATE + " INTEGER,"
            + ShortMessage.FIELD_MESSAGE_TYPE + " INTEGER,"
            + ShortMessage.FIELD_MIME_TYPE + " TEXT,"
            + ShortMessage.FIELD_READ + " INTEGER,"
            + ShortMessage.FIELD_BODY + " TEXT,"
            + BaseBean.FIELD_PID + " INTEGER KEY REFERENCES " + TABLE_CONTACT_PERSON + "(" + Contacts.FIELD_ID + ") ON DELETE CASCADE"
            + " );";

    /**
     * 联系人<br/>
     * 主表
     */
    public static final String CREATE_TABLE_CONTACT_PERSON = "CREATE TABLE IF NOT EXISTS "
            + TABLE_CONTACT_PERSON + "("
            + Contacts.FIELD_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + Contacts.FIELD_USERNAME + " TEXT, "
            + Contacts.FIELD_PHONE + " TEXT, "
            + Contacts.FIELD_HEADER + " TEXT, "
            + Contacts.FIELD_SIP_ADDRESS + " TEXT, "
            + BaseBean.FIELD_SORT_LETTERS + " TEXT, "
            + BaseBean.FIELD_IS_FISTER + " INTEGER, "
            + BaseBean.FIELD_COLOR + " INTEGER)";

    public DatabaseHelper(Context context) {
        super(context, SipManager.AUTHORITY, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_ACCOUNT_CREATE);
        db.execSQL(CREATE_TABLE_MESSAGE);
        db.execSQL(CREATE_TABLE_VIDEO_RECORD);
        db.execSQL(CREATE_TABLE_CONTACT_PERSON);
        db.execSQL(CREATE_TABLE_CALL_RECORD);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}