package com.crte.sipstackhome.dao;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;

import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.db.DatabaseHelper;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.models.CallRecord;
import com.crte.sipstackhome.models.Contacts;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/6 0006.
 */
public class CallRecordDao extends DaoImpl<CallRecord> {
    private static CallRecordDao mCallRecordDao;
    private final static String[] DATA_ALL = new String[]{DatabaseHelper.FIELD_ID, DatabaseHelper.FIELD_USER_ID, DatabaseHelper.FIELD_USERNAME, DatabaseHelper.FIELD_CALL_STATE, DatabaseHelper.FIELD_CALL_ADDRESS, DatabaseHelper.FIELD_TIME, DatabaseHelper.FIELD_PID};

    private CallRecordDao() {
    }

    public static CallRecordDao getInstance() {
        if (mCallRecordDao == null) {
            mCallRecordDao = new CallRecordDao();
        }
        return mCallRecordDao;
    }

    @Override
    public void insertNewDatas(ContentResolver contentResolver, CallRecord data) {
        ContentValues contentValues = getContentValuesDatas(data);
        contentResolver.insert(SipProfile.CONTACT_CALL_RECORD, contentValues);
    }

    @Override
    public void deleteDatas(ContentResolver contentResolver, int userId) {

    }

    @Override
    public void updateDatas(ContentResolver contentResolver, CallRecord data) {

    }

    @Override
    public Cursor queryDatas(ContentResolver contentResolver, int userId) {
        return null;
    }

    public Cursor queryDatas(ContentResolver contentResolver, String username) {
        Cursor cursor = contentResolver.query(SipProfile.CONTACT_CALL_RECORD, null, DatabaseHelper.FIELD_USERNAME + " LIKE ?", new String[]{"%" + username + "%"}, "_id DESC");
        return cursor;
    }

    @Override
    public void insertAllNewDatas(ContentResolver contentResolver, ArrayList<CallRecord> dataList) {

    }

    @Override
    public void deleteAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    @Override
    public void updateAllDatas(ContentResolver contentResolver, ArrayList<CallRecord> dataList) {

    }

    @Override
    public void queryAllDatas(ContentResolver contentResolver, ArrayList<Integer> userIdList) {

    }

    @Override
    public ContentValues getContentValuesDatas(CallRecord datas) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DatabaseHelper.FIELD_TIME, datas.date);
        contentValues.put(DatabaseHelper.FIELD_SORT_LETTERS, datas.sortLetters);
        contentValues.put(DatabaseHelper.FIELD_FIRST, datas.isFister);
        contentValues.put(DatabaseHelper.FIELD_PID, datas.pid);
        return contentValues;
    }

    @Override
    public CallRecord getPrototype(String data) {
        return null;
    }

    public ArrayList<CallRecord> getArrayListDatas(Activity activity, Cursor cursor) {
        ArrayList<CallRecord> callRecords = new ArrayList<>();
        try {
            while (cursor.moveToNext()) {
                CallRecord callRecord = new CallRecord();
                callRecord.stateFlag = SipInfoState.VIEW_PHONE;
                callRecord.stateDescription = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME));
                callRecord.pid = cursor.getInt(cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_PID));
                // 测试 添加颜色选项
                ArrayList<Contacts> contactPersonBeen = ContactPersonDao.getInstance().getqueryDatas(activity, callRecord.pid);
                Contacts contacts = contactPersonBeen.get(0);
                callRecord.color = contacts.color;
                callRecords.add(callRecord);
            }
        } catch (Exception e) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return callRecords;
    }

    public ArrayList<CallRecord> getqueryDatas(Activity activity, String username) {
        Cursor cursor = queryDatas(activity.getContentResolver(), username);
        return getArrayListDatas(activity, cursor);
    }

    public ArrayList<CallRecord> getManagedCursor(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_CALL_RECORD, DATA_ALL, null, null, DatabaseContentProvider.DEFAULT_SORT_ORDER);
        return getArrayListDatas(activity, cursor);
    }

    /**
     * 添加测试数据
     */
    public void addTestData(Activity activity) {
    }
}
