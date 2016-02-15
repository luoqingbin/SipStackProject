package com.crte.sipstackhome.models;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.customview.SideBar;
import com.crte.sipstackhome.db.DatabaseHelper;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.utils.CharacterParser;
import com.crte.sipstackhome.utils.PinyinComparator;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Contacts extends BaseBean {
    private static CharacterParser mCharacterParser;
    private static PinyinComparator mPinyinComparator;

    /**
     * name_raw
     * contact_id
     * photo_id
     * photo_file_id
     * custom_ringtone
     * send_to_voicemail 发送到语音信箱
     * times_contacted 联系次数
     * last_time_contacted 最后联系时间
     */

    public static final String[] FULL_PROJECTION = new String[]{
            Contacts.FIELD_ID,
            Contacts.FIELD_USERNAME,
            Contacts.FIELD_PHONE,
            Contacts.FIELD_HEADER,
            Contacts.FIELD_SIP_ADDRESS,
            BaseBean.FIELD_SORT_LETTERS,
            BaseBean.FIELD_IS_FISTER,
            BaseBean.FIELD_COLOR
    };

    public static final String FIELD_ID = "_id";
    public static final String FIELD_USERNAME = "username";
    public static final String FIELD_PHONE = "phone";
    public static final String FIELD_HEADER = "header";
    public static final String FIELD_SIP_ADDRESS = "sip_address";

    public int _id;
    public String username;
    public String phone;
    public String header;
    public String sipAddress;

    /**
     * 添加一条数据
     *
     * @param contentResolver
     * @param contacts
     */
    public static void insertDatas(ContentResolver contentResolver, Contacts contacts) {
        ContentValues contentValues = getContentValuesDatas(contacts);
        contentResolver.insert(SipProfile.CONTACT_PERSON_CONTENT_URI, contentValues);
    }

    /**
     * 查询指定数据
     */
    public static Cursor queryDatas(ContentResolver contentResolver, String field, int fieldId) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_PERSON_CONTENT_URI, null, field + " = " + fieldId, null, null);
        return cursor;
    }

    public static Cursor queryDatas(ContentResolver contentResolver, String field, String fieldId) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_PERSON_CONTENT_URI, null, field + " = " + fieldId, null, null);
        return cursor;
    }

    public static ContentValues getContentValuesDatas(Contacts contacts) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(Contacts.FIELD_USERNAME, contacts.username);
        contentValues.put(Contacts.FIELD_PHONE, contacts.phone);
        contentValues.put(Contacts.FIELD_HEADER, contacts.header);
        contentValues.put(Contacts.FIELD_SIP_ADDRESS, contacts.sipAddress);
        contentValues.put(Contacts.FIELD_SORT_LETTERS, contacts.sortLetters);
        contentValues.put(Contacts.FIELD_IS_FISTER, contacts.isFister);
        contentValues.put(Contacts.FIELD_COLOR, contacts.color);
        return contentValues;
    }

    public static ArrayList<Contacts> getqueryDatas(Activity activity, String field, int fieldId) {
        Cursor cursor = queryDatas(activity.getContentResolver(), field, fieldId);
        return getArrayListDatas(cursor);
    }

    public static ArrayList<Contacts> getqueryDatas(Context context, String field, String fieldId) {
        Cursor cursor = queryDatas(context.getContentResolver(), field, fieldId);
        return getArrayListDatas(cursor);
    }

    /**
     * 获得详细信息
     *
     * @param cursor
     * @return
     */
    public static ArrayList<Contacts> getArrayListDatas(Cursor cursor) {
        ArrayList<Contacts> contactses = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Contacts contacts = new Contacts();
                contacts.stateFlag = SipInfoState.VIEW_PHONE;
                contacts.stateDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME));
                contacts.username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME));
                contacts.phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_PHONE));
                contacts.header = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_HEADER));
                contacts._id = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_ID));
                contacts.color = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_COLOR));
                contactses.add(contacts);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contactses;
    }

    public static void setFastIndex(Activity activity) {
        mCharacterParser = CharacterParser.getInstance();
        mPinyinComparator = new PinyinComparator();
        List<Contacts> tList = filledData(activity.getResources().getStringArray(R.array.date)); // 测试数据
        // 排序
        Collections.sort(tList, mPinyinComparator);

        // 设置标记
        for (int i = 0; i < SideBar.b.length; i++) {
            char first = SideBar.b[i].toUpperCase().charAt(0);
            for (int j = 0; j < tList.size(); j++) {
                char firstChar = tList.get(j).sortLetters.toUpperCase().charAt(0);
                if (first == firstChar) {
                    tList.get(j).isFister = 1;
                    break;
                }
            }
        }

        for (int i = 0; i < tList.size(); i++) {
            insertDatas(activity.getContentResolver(), tList.get(i)); // 目前是一条一条添加，需优化
        }
    }

    private static List<Contacts> filledData(String[] date) {
        List<Contacts> mSortList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < date.length; i++) {
            Contacts t = getPrototype(date[i], (100 + i) + "");
            String pinyin = mCharacterParser.getSelling(date[i]);
            String sortString = pinyin.substring(0, 1).toUpperCase();

            if (sortString.matches("[A-Z]")) {
                t.sortLetters = sortString.toUpperCase();
            } else {
                t.sortLetters = "#";
            }
            // 添加颜色测试
            t.color = random.nextInt(BaseActivity.HEADER_COLOR.length);
            mSortList.add(t);
        }
        return mSortList;
    }

    public static Contacts getPrototype(String data, String phone) {
        Contacts contacts = new Contacts();
        contacts.username = data;
        contacts.phone = phone;
        return contacts;
    }
}
