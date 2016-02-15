package com.crte.sipstackhome.models;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.impl.SipInfoState;

import java.util.ArrayList;
import java.util.Random;

/**
 * 主键
 */
public class ShortMessage extends BaseBean {
    public static final String[] FULL_PROJECTION = new String[]{
            ShortMessage.FIELD_ID,
            ShortMessage.FIELD_TO_USERNAME,
            ShortMessage.FIELD_TO_SIP_URI,
            ShortMessage.FIELD_FROM_USERNAME,
            ShortMessage.FIELD_FROM_SIP_URI,
            ShortMessage.FIELD_DATE,
            ShortMessage.FIELD_MESSAGE_TYPE,
            ShortMessage.FIELD_MIME_TYPE,
            ShortMessage.FIELD_READ,
            ShortMessage.FIELD_BODY,
            ShortMessage.FIELD_PID
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
     * 消息类型
     */
    public static final String FIELD_MESSAGE_TYPE = "message_type";
    /**
     * MINI/TYPE
     */
    public static final String FIELD_MIME_TYPE = "mime_type";
    /**
     * 消息是否已读
     */
    public static final String FIELD_READ = "read";
    /**
     * 消息内容
     */
    public static final String FIELD_BODY = "body";

    public int _id;
    public String toUsername;
    public String toSipUri;
    public String fromUsername;
    public String fromSipUri;
    public long date;
    public int messageType;
    public String mimeType;
    public int read;
    public String body;

    /**
     * 插入一条数据
     */
    public static void insertDatas(ContentResolver contentResolver, ShortMessage data) {
        ContentValues contentValues = getContentValuesDatas(data);
        contentResolver.insert(SipProfile.CONTACT_SHORT_MESSAGE_URI, contentValues);
    }

    /**
     * 查询所有信息
     *
     * @param contentResolver
     * @return
     */
    public static Cursor queryAllDatas(ContentResolver contentResolver) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_SHORT_MESSAGE_URI, null, FIELD_ID + " != -1", null, null);
        return cursor;
    }

    public static ArrayList<ShortMessage> getqueryDatas(Activity activity, String username) {
        Cursor cursor = queryDatas(activity.getContentResolver(), username);
        return getArrayListDatas(activity, cursor);
    }

    public static Cursor queryDatas(ContentResolver contentResolver, String username) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_SHORT_MESSAGE_URI, null, ShortMessage.FIELD_FROM_USERNAME + " LIKE ?", new String[]{"%" + username + "%"}, "_id DESC");
        return cursor;
    }

    public static ArrayList<ShortMessage> getShortMessageBean(Cursor cursor) {
        if (cursor == null) {
            return null;
        }

        ArrayList<ShortMessage> shortMessageArrayList = new ArrayList<>();

        try {
            while (cursor.moveToNext()) {
                ShortMessage shortMessage = new ShortMessage();
                shortMessage._id = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_ID));
                shortMessage.toUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_USERNAME));
                shortMessage.toSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_TO_SIP_URI));
                shortMessage.fromUsername = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_USERNAME));
                shortMessage.fromSipUri = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_FROM_SIP_URI));
                shortMessage.date = cursor.getLong(cursor.getColumnIndexOrThrow(FIELD_DATE));
                shortMessage.messageType = cursor.getShort(cursor.getColumnIndexOrThrow(FIELD_MESSAGE_TYPE));
                shortMessage.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_MIME_TYPE));
                shortMessage.read = cursor.getInt(cursor.getColumnIndexOrThrow(FIELD_READ));
                shortMessage.body = cursor.getString(cursor.getColumnIndexOrThrow(FIELD_BODY));
                shortMessageArrayList.add(shortMessage);
            }
        } finally {
            cursor.close();
        }
        return shortMessageArrayList;
    }

    public static ArrayList<ShortMessage> getManagedCursor(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_SHORT_MESSAGE_URI, ShortMessage.FULL_PROJECTION, ShortMessage.FIELD_FROM_USERNAME + " != -1) " + "GROUP BY (" + ShortMessage.FIELD_FROM_USERNAME, null, DatabaseContentProvider.DEFAULT_SORT_ORDER_DESC);
        return getArrayListDatas(activity, cursor);
    }

    public static ArrayList<ShortMessage> getArrayListDatas(Activity activity, Cursor cursor) {
        ArrayList<ShortMessage> shortMessages = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                ShortMessage shortMessage = new ShortMessage();
                shortMessage.stateFlag = SipInfoState.VIEW_MESSAGE;
                shortMessage.pid = cursor.getInt(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_PID));
                shortMessage.stateDescription = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_USERNAME));

                shortMessage.toSipUri = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_TO_SIP_URI));
                shortMessage.toUsername = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_TO_USERNAME));
                shortMessage.fromSipUri = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_SIP_URI));
                shortMessage.fromUsername = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_USERNAME));
                shortMessage.date = cursor.getInt(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_DATE));
                shortMessage.mimeType = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_MIME_TYPE));
                shortMessage.read = cursor.getInt(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_READ));
                shortMessage.body = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_BODY));

                // 添加颜色
                ArrayList<Contacts> contactPersonBeen = Contacts.getqueryDatas(activity, Contacts.FIELD_ID, shortMessage.pid);
                Contacts contacts = contactPersonBeen.get(0);
                shortMessage.color = contacts.color;
                shortMessages.add(shortMessage);
            }
        } catch (Exception e) {
            cursor.close();
        }
        return shortMessages;
    }

    /**
     * 获得ContentValues
     */
    public static ContentValues getContentValuesDatas(ShortMessage datas) {
        ContentValues contentValues = new ContentValues();

        contentValues.put(FIELD_TO_USERNAME, datas.toUsername);
        contentValues.put(FIELD_TO_SIP_URI, datas.toSipUri);
        contentValues.put(FIELD_FROM_USERNAME, datas.fromUsername);
        contentValues.put(FIELD_FROM_SIP_URI, datas.fromSipUri);
        contentValues.put(FIELD_DATE, datas.date);
        contentValues.put(FIELD_MESSAGE_TYPE, datas.messageType);
        contentValues.put(FIELD_MIME_TYPE, datas.mimeType);
        contentValues.put(FIELD_READ, datas.read);
        contentValues.put(FIELD_BODY, datas.body);
        contentValues.put(FIELD_PID, datas.pid);
        return contentValues;
    }

    /*
     * 添加测试联系人
     */
    public static void addTestData(Activity activity) {
        Random random = new Random();

        for (int i = 1; i < 44; i++) {
            ArrayList<Contacts> contactPersonBeen = Contacts.getqueryDatas(activity, Contacts.FIELD_PID, i);
            Contacts contacts = contactPersonBeen.get(0);
            ShortMessage shortMessage = new ShortMessage();
            shortMessage.toSipUri = "<sip:to_username:sip_address>";
            shortMessage.toUsername = "用户昵称 - ";
            shortMessage.fromSipUri = "<sip:from_username:sip_address>";
            shortMessage.fromUsername = "目标昵称 - " + contacts.username;
            shortMessage.date = System.currentTimeMillis();
            shortMessage.mimeType = "text/plant";
            shortMessage.read = 1;
            shortMessage.body = message[random.nextInt(message.length)];
            shortMessage.pid = contacts._id;
            insertDatas(activity.getContentResolver(), shortMessage);
        }
    }

    static String[] name = {"王震", "成龙", "13120201339", "陶喆", "阮今天", "哈林", "Torment", "Hello Agent", "李德华", "王菲", "中国移动通信", "10086", "那英", "Google开发团队", "网易",};
    static String[] message = {"NASA科学副总监、前宇航员约翰·格伦斯菲尔德(John Grunsfeld)介绍说",
            "左图：没有磁场的保护，火星大气层不断被太阳风剥离；右图：拥有全球性磁场的地球偏离太阳风高能带电粒子流，保护了地球大气。（腾讯太空配图）",
            "NASA科学副总监、前宇航员约翰·格伦斯菲尔德(John Grunsfeld)介绍说",
            "知晓什么原因导致行星表面从可以有微生物存在到变得不再拥有生命很重要，这也是美国宇航局载人火星计划正在解决的一个重要问题",
            "火星远古时期拥有丰富的液态水，今天火星表面遗留的众多河谷和只有水参与才能形成的各种矿物质都是当时大量液态水存在的证据。在30亿年前，火星的大气密度要比现在高的多，气候也温暖的多，当时的火星表面存在河流，湖泊，甚至液态水组成的海洋。",
            "来自美国宇航局戈达德太空飞行中心",
            "他称，预计整个巡航计划将总计需要10亿菲律宾比索(约合1.3亿元人民币)。(白云怡)",
            "OK",
            "日本巡航南海最新消息",
            "组图：直击中越边境扫雷行动 出动扫雷机器人",
            "组图：泰国小象洗澡时顽皮戏水萌翻众看客",
            "听说二年级的孩子胆大点，敢到教室外的走廊玩会儿，但是不能跑、跳，让老师抓到就要被警告，老师甚至告诉孩子们，抓到3次就可能被开除。",
            "感觉孩子蔫巴了不少。",
            "记者了解到，目前北京一些有口碑的小学，一个年级有8个班、10个班，每个班三四十人，一个学校有几千人。",
            "在学校不能跑和跳，不能到操场活动",
    };

    @Override
    public String toString() {
        return "ShortMessage{" +
                "_id=" + _id +
                ", toUsername='" + toUsername + '\'' +
                ", toSipUri='" + toSipUri + '\'' +
                ", fromUsername='" + fromUsername + '\'' +
                ", fromSipUri='" + fromSipUri + '\'' +
                ", date=" + date +
                ", messageType=" + messageType +
                ", mimeType='" + mimeType + '\'' +
                ", read=" + read +
                ", body='" + body + '\'' +
                '}';
    }
}
