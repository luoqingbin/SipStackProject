package com.crte.sipstackhome.dao;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseHelper;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.models.Contacts;

import java.util.ArrayList;

/**
 * 联系人<br/>
 * 负责数据库的查询操作
 */
public class ContactPersonDao extends DaoImpl<Contacts> {
    private static ContactPersonDao mContactPersonDao;

    private ContactPersonDao() {
    }

    public static ContactPersonDao getInstance() {
        if (mContactPersonDao == null) {
            mContactPersonDao = new ContactPersonDao();
        }
        return mContactPersonDao;
    }

    @Override
    public void insertNewDatas(ContentResolver contentResolver, Contacts contacts) {
        ContentValues contentValues = getContentValuesDatas(contacts);
        contentResolver.insert(SipProfile.CONTACT_PERSON_CONTENT_URI, contentValues);
    }

    @Override
    public void deleteDatas(ContentResolver contentResolver, int userId) {

    }

    @Override
    public void updateDatas(ContentResolver contentResolver, Contacts data) {

    }

    @Override
    public Cursor queryDatas(ContentResolver contentResolver, int userId) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_PERSON_CONTENT_URI, null, DatabaseHelper.FIELD_ID + " = " + userId, null, null);
        return cursor;
    }

    @Override
    public void insertAllNewDatas(ContentResolver contentResolver, ArrayList<Contacts> dataList) {

    }

    @Override
    public void deleteAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    @Override
    public void updateAllDatas(ContentResolver contentResolver, ArrayList<Contacts> dataList) {

    }

    @Override
    public void queryAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    /**
     * 模糊查询<br/>
     * 联系人名字匹配
     * */
    public Cursor queryDatas(ContentResolver contentResolver, String username) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_PERSON_CONTENT_URI, null, DatabaseHelper.FIELD_USERNAME + " LIKE ?", new String[]{"%" + username + "%"}, "_id DESC");
        return cursor;
    }

    public ArrayList<Contacts> getqueryDatas(Activity activity, String username) {
        Cursor cursor = queryDatas(activity.getContentResolver(), username);
        return getArrayListDatas(cursor);
    }

    public ArrayList<Contacts> getqueryDatas(Activity activity, int id) {
        Cursor cursor = queryDatas(activity.getContentResolver(), id);
        return getArrayListDatas(cursor);
    }


    @Override
    public ContentValues getContentValuesDatas(Contacts contacts) {
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

    @Override
    public Contacts getPrototype(String data) {
        Contacts contacts = new Contacts();
        contacts.username = data;
        return contacts;
    }

    public ArrayList<Contacts> getArrayListDatas(Cursor cursor) {
        ArrayList<Contacts> contactses = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                Contacts contacts = new Contacts();
                contacts.stateFlag = SipInfoState.VIEW_PHONE;
                contacts.stateDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME));
                contacts.username = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME));
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
}
