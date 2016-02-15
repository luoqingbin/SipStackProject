package com.crte.sipstackhome.dao;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.models.Contacts;
import com.crte.sipstackhome.models.ShortMessage;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;

public class ShortMessageDao extends DaoImpl<ShortMessage> {
    private static ShortMessageDao mShortMessageDao;
//    private final static String[] DATA_ALL = new String[]{DatabaseHelper.FIELD_ID, DatabaseHelper.FIELD_USERNAME, DatabaseHelper.FIELD_MESSAGE, DatabaseHelper.FIELD_TIME, DatabaseHelper.FIELD_FLAG, DatabaseHelper.FIELD_PID};

    private ShortMessageDao() {
    }

    public static ShortMessageDao getInstance() {
        if (mShortMessageDao == null) {
            mShortMessageDao = new ShortMessageDao();
        }
        return mShortMessageDao;
    }

    @Override
    public void insertNewDatas(ContentResolver contentResolver, ShortMessage shortMessage) {
        ContentValues contentValues = getContentValuesDatas(shortMessage);
        contentResolver.insert(SipProfile.CONTACT_SHORT_MESSAGE_URI, contentValues);
    }

    @Override
    public void deleteDatas(ContentResolver contentResolver, int userId) {

    }

    @Override
    public void updateDatas(ContentResolver contentResolver, ShortMessage data) {

    }

    @Override
    public Cursor queryDatas(ContentResolver contentResolver, int userId) {
        return null;
    }

    public Cursor queryDatas(ContentResolver contentResolver, String username) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_SHORT_MESSAGE_URI, null, ShortMessage.FIELD_FROM_USERNAME + " LIKE ?", new String[]{"%" + username + "%"}, "_id DESC");
        return cursor;
    }

    @Override
    public void insertAllNewDatas(ContentResolver contentResolver, ArrayList<ShortMessage> dataList) {

    }

    @Override
    public void deleteAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    @Override
    public void updateAllDatas(ContentResolver contentResolver, ArrayList<ShortMessage> dataList) {

    }

    @Override
    public void queryAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    @Override
    public ContentValues getContentValuesDatas(ShortMessage shortMessage) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(ShortMessage.FIELD_TO_USERNAME, shortMessage.toUsername);
        contentValues.put(ShortMessage.FIELD_TO_SIP_URI, shortMessage.toSipUri);
        contentValues.put(ShortMessage.FIELD_FROM_USERNAME, shortMessage.fromUsername);
        contentValues.put(ShortMessage.FIELD_FROM_SIP_URI, shortMessage.fromSipUri);
        contentValues.put(ShortMessage.FIELD_DATE, shortMessage.date);
        contentValues.put(ShortMessage.FIELD_MESSAGE_TYPE, shortMessage.messageType);
        contentValues.put(ShortMessage.FIELD_MIME_TYPE, shortMessage.mimeType);
        contentValues.put(ShortMessage.FIELD_READ, shortMessage.read);
        contentValues.put(ShortMessage.FIELD_BODY, shortMessage.body);
        return contentValues;
    }

    public ArrayList<ShortMessage> getArrayListDatas(Activity activity, Cursor cursor) {
        ArrayList<ShortMessage> shortMessages = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                LogUtils.d("ShortMessage", "getArrayListDatas 执行了吗：" + cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_USERNAME)));
                ShortMessage shortMessage = new ShortMessage();
                shortMessage.stateFlag = SipInfoState.VIEW_MESSAGE;
                shortMessage.pid = cursor.getInt(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_PID));
                shortMessage.stateDescription = cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_USERNAME));
                // 测试 添加颜色选项
                ArrayList<Contacts> contactPersonBeen = Contacts.getqueryDatas(activity, Contacts.FIELD_ID, shortMessage.pid);
                Contacts contacts = contactPersonBeen.get(0);
                LogUtils.d("ShortMessage", "getArrayListDatas ---：" + cursor.getString(cursor.getColumnIndexOrThrow(ShortMessage.FIELD_FROM_USERNAME)));
                shortMessage.color = contacts.color;
                shortMessages.add(shortMessage);
            }
        } catch (Exception e) {
            cursor.close();
        }
        return shortMessages;
    }

    @Override
    public ShortMessage getPrototype(String data) {
        return null;
    }

    public ArrayList<ShortMessage> getManagedCursor(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_SHORT_MESSAGE_URI, ShortMessage.FULL_PROJECTION, ShortMessage.FIELD_FROM_USERNAME + " != -1) " + "GROUP BY (" + ShortMessage.FIELD_FROM_USERNAME, null, DatabaseContentProvider.DEFAULT_SORT_ORDER_DESC);
        return getArrayListDatas(activity, cursor);
    }

    public ArrayList<ShortMessage> getqueryDatas(Activity activity, String username) {
        Cursor cursor = queryDatas(activity.getContentResolver(), username);
        return getArrayListDatas(activity, cursor);
    }

    /**
     * 添加测试数据
     */
    public void addTestData(Activity activity) {
        // 测试数据，数量模拟联系人数量
        for (int i = 1; i < 44; i++) {
            ArrayList<Contacts> contactPersonBeen = ContactPersonDao.getInstance().getqueryDatas(activity, i);
            Contacts contacts = contactPersonBeen.get(0);
            ShortMessage shortMessage = new ShortMessage();
            shortMessage.pid = contacts._id;
            insertNewDatas(activity.getContentResolver(), shortMessage);
        }
    }

    String[] name = {"王震", "成龙", "13120201339", "陶喆", "阮今天", "哈林", "Torment", "Hello Agent", "李德华", "王菲", "中国移动通信", "10086", "那英", "Google开发团队", "网易",};
    String[] message = {"NASA科学副总监、前宇航员约翰·格伦斯菲尔德(John Grunsfeld)介绍说",
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
}
