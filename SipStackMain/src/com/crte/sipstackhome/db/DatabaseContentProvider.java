/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.crte.sipstackhome.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.api.SipMessage;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.api.SipProfileState;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class DatabaseContentProvider extends ContentProvider {
    private static final String TAG = "DatabaseContentProvider";

    private DatabaseHelper mDatabaseHelper;
    private static final String UNKNOWN_URI_LOG = "Unknown URI ";

    private static final int ACCOUNTS = 1, ACCOUNTS_ID = 2;
    private static final int ACCOUNTS_STATUS = 3, ACCOUNTS_STATUS_ID = 4;
    private static final int CALLLOGS = 5, CALLLOGS_ID = 6;
    private static final int FILTERS = 7, FILTERS_ID = 8;
    private static final int MESSAGES = 9, MESSAGES_ID = 10;
    private static final int THREADS = 11, THREADS_ID = 12;
    public static final int MESSAGE = 13, MESSAGE_ID = 14;
    public static final int PERSON = 15, PERSON_ID = 16;
    public static final int RECORD = 17, RECORD_ID = 19;
    public static final int VIDEO = 20, VIDEO_ID = 21;

    // 排序方式 升序
    public static final String DEFAULT_SORT_ORDER = "_id ASC";
    public static final String DEFAULT_SORT_ORDER_DESC = "_id DESC";

    private static final UriMatcher URI_MATCHER;

    static {
        URI_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);

        URI_MATCHER.addURI(SipManager.AUTHORITY, SipProfile.ACCOUNTS_TABLE_NAME, ACCOUNTS);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipProfile.ACCOUNTS_TABLE_NAME + "/#", ACCOUNTS_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipProfile.ACCOUNTS_STATUS_TABLE_NAME, ACCOUNTS_STATUS);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipProfile.ACCOUNTS_STATUS_TABLE_NAME + "/#", ACCOUNTS_STATUS_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipManager.CALLLOGS_TABLE_NAME, CALLLOGS);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipManager.CALLLOGS_TABLE_NAME + "/#", CALLLOGS_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipManager.FILTERS_TABLE_NAME, FILTERS);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipManager.FILTERS_TABLE_NAME + "/#", FILTERS_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipMessage.MESSAGES_TABLE_NAME, MESSAGES);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipMessage.MESSAGES_TABLE_NAME + "/#", MESSAGES_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipMessage.THREAD_ALIAS, THREADS);
        URI_MATCHER.addURI(SipManager.AUTHORITY, SipMessage.THREAD_ALIAS + "/*", THREADS_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_SHORT_MESSAGE_NAME, MESSAGE);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_SHORT_MESSAGE_NAME + "/#", MESSAGE_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_CONTACT_PERSON, PERSON);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_CONTACT_PERSON + "/#", PERSON_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_CALL_RECORD, RECORD);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_CALL_RECORD + "/#", RECORD_ID);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_VIDEO_RECORD, VIDEO);
        URI_MATCHER.addURI(SipManager.AUTHORITY, DatabaseHelper.TABLE_VIDEO_RECORD + "/#", VIDEO_ID);
    }

    private final Map<Long, ContentValues> mProfilesStatus = new HashMap<Long, ContentValues>();

    public final static String[] ACCOUNT_FULL_PROJECTION = {
            SipProfile.FIELD_ID,
            // Application relative fields
            SipProfile.FIELD_ACTIVE, SipProfile.FIELD_WIZARD, SipProfile.FIELD_DISPLAY_NAME,
            // Custom datas
            SipProfile.FIELD_WIZARD_DATA,

            // Here comes pjsua_acc_config fields
            SipProfile.FIELD_PRIORITY, SipProfile.FIELD_ACC_ID, SipProfile.FIELD_REG_URI,
            SipProfile.FIELD_MWI_ENABLED, SipProfile.FIELD_PUBLISH_ENABLED, SipProfile.FIELD_REG_TIMEOUT, SipProfile.FIELD_KA_INTERVAL,
            SipProfile.FIELD_PIDF_TUPLE_ID,
            SipProfile.FIELD_FORCE_CONTACT, SipProfile.FIELD_ALLOW_CONTACT_REWRITE, SipProfile.FIELD_CONTACT_REWRITE_METHOD,
            SipProfile.FIELD_ALLOW_VIA_REWRITE, SipProfile.FIELD_ALLOW_SDP_NAT_REWRITE,
            SipProfile.FIELD_CONTACT_PARAMS, SipProfile.FIELD_CONTACT_URI_PARAMS,
            SipProfile.FIELD_TRANSPORT, SipProfile.FIELD_DEFAULT_URI_SCHEME, SipProfile.FIELD_USE_SRTP, SipProfile.FIELD_USE_ZRTP,
            SipProfile.FIELD_REG_DELAY_BEFORE_REFRESH,

            // RTP config
            SipProfile.FIELD_RTP_PORT, SipProfile.FIELD_RTP_PUBLIC_ADDR, SipProfile.FIELD_RTP_BOUND_ADDR,
            SipProfile.FIELD_RTP_ENABLE_QOS, SipProfile.FIELD_RTP_QOS_DSCP,

            // Proxy infos
            SipProfile.FIELD_PROXY, SipProfile.FIELD_REG_USE_PROXY,

            // And now cred_info since for now only one cred info can be managed
            // In future release a credential table should be created
            SipProfile.FIELD_REALM, SipProfile.FIELD_SCHEME, SipProfile.FIELD_USERNAME, SipProfile.FIELD_DATATYPE,
            SipProfile.FIELD_DATA,

            SipProfile.FIELD_AUTH_INITIAL_AUTH, SipProfile.FIELD_AUTH_ALGO,

            // CSipSimple specific
            SipProfile.FIELD_SIP_STACK, SipProfile.FIELD_VOICE_MAIL_NBR,
            SipProfile.FIELD_TRY_CLEAN_REGISTERS, SipProfile.FIELD_ANDROID_GROUP,

            // RFC 5626
            SipProfile.FIELD_USE_RFC5626, SipProfile.FIELD_RFC5626_INSTANCE_ID, SipProfile.FIELD_RFC5626_REG_ID,

            // Video
            SipProfile.FIELD_VID_IN_AUTO_SHOW, SipProfile.FIELD_VID_OUT_AUTO_TRANSMIT,

            // STUN, ICE, TURN
            SipProfile.FIELD_SIP_STUN_USE, SipProfile.FIELD_MEDIA_STUN_USE,
            SipProfile.FIELD_ICE_CFG_USE, SipProfile.FIELD_ICE_CFG_ENABLE,
            SipProfile.FIELD_TURN_CFG_USE, SipProfile.FIELD_TURN_CFG_ENABLE, SipProfile.FIELD_TURN_CFG_SERVER, SipProfile.FIELD_TURN_CFG_USER, SipProfile.FIELD_TURN_CFG_PASSWORD,

            SipProfile.FIELD_IPV6_MEDIA_USE,
    };

    @Override
    public String getType(Uri uri) {
        switch (URI_MATCHER.match(uri)) {
            case ACCOUNTS:
                return SipProfile.ACCOUNT_CONTENT_TYPE;
            case ACCOUNTS_ID:
                return SipProfile.ACCOUNT_CONTENT_ITEM_TYPE;
            case ACCOUNTS_STATUS:
                return SipProfile.ACCOUNT_STATUS_CONTENT_TYPE;
            case ACCOUNTS_STATUS_ID:
                return SipProfile.ACCOUNT_STATUS_CONTENT_ITEM_TYPE;
            case CALLLOGS:
                return SipManager.CALLLOG_CONTENT_TYPE;
            case CALLLOGS_ID:
                return SipManager.CALLLOG_CONTENT_ITEM_TYPE;
            case FILTERS:
                return SipManager.FILTER_CONTENT_TYPE;
            case FILTERS_ID:
                return SipManager.FILTER_CONTENT_ITEM_TYPE;
            case MESSAGES:
                return SipMessage.MESSAGE_CONTENT_TYPE;
            case MESSAGES_ID:
                return SipMessage.MESSAGE_CONTENT_ITEM_TYPE;
            case THREADS:
                return SipMessage.MESSAGE_CONTENT_TYPE;
            case THREADS_ID:
                return SipMessage.MESSAGE_CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException(UNKNOWN_URI_LOG + uri);
        }
    }

    @Override
    public boolean onCreate() {
        mDatabaseHelper = new DatabaseHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        LogUtils.i(TAG, "执行 query 操作");
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();

        int uriCode = URI_MATCHER.match(uri);
        Cursor c;
        long id;
        switch (uriCode) {
            case ACCOUNTS:
                sqLiteQueryBuilder.setTables(SipProfile.ACCOUNTS_TABLE_NAME);
                break;
            case ACCOUNTS_ID:
                sqLiteQueryBuilder.setTables(SipProfile.ACCOUNTS_TABLE_NAME);
                break;
            case MESSAGE:
                sqLiteQueryBuilder.setTables(DatabaseHelper.TABLE_SHORT_MESSAGE_NAME);
                break;
            case PERSON:
                sqLiteQueryBuilder.setTables(DatabaseHelper.TABLE_CONTACT_PERSON);
                break;
            case RECORD:
                sqLiteQueryBuilder.setTables(DatabaseHelper.TABLE_CALL_RECORD);
                break;
            case VIDEO:
                sqLiteQueryBuilder.setTables(DatabaseHelper.TABLE_VIDEO_RECORD);
                break;
            case VIDEO_ID:
                sqLiteQueryBuilder.setTables(DatabaseHelper.TABLE_VIDEO_RECORD);
                break;
            case ACCOUNTS_STATUS:
                synchronized (mProfilesStatus) {
                    ContentValues[] cvs = new ContentValues[mProfilesStatus.size()];
                    int i = 0;
                    for (ContentValues ps : mProfilesStatus.values()) {
                        cvs[i] = ps;
                        i++;
                    }
                    c = getCursor(cvs);
                }
                if (c != null) {
                    c.setNotificationUri(getContext().getContentResolver(), uri);
                }
                return c;
            case ACCOUNTS_STATUS_ID:
                id = ContentUris.parseId(uri);
                synchronized (mProfilesStatus) {
                    ContentValues cv = mProfilesStatus.get(id);
                    if (cv == null) {
                        return null;
                    }
                    c = getCursor(new ContentValues[]{cv});
                }
                c.setNotificationUri(getContext().getContentResolver(), uri);
                return c;
        }

        SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getReadableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(sqLiteDatabase, projection, selection, selectionArgs, null, null, sortOrder);

//        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        getContext().getContentResolver().notifyChange(uri, null);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues initialValues) {
        LogUtils.i(TAG, "执行 insert 操作");
        int uriCode = URI_MATCHER.match(uri);
        ContentValues values;

        if (initialValues != null) {
            values = new ContentValues(initialValues);
        } else {
            values = new ContentValues();
        }
        String table = null;

        switch (uriCode) {
            case ACCOUNTS:
                table = SipProfile.ACCOUNTS_TABLE_NAME;
                break;
            case ACCOUNTS_ID:
                table = SipProfile.ACCOUNTS_TABLE_NAME;
                break;
            case MESSAGE:
                table = DatabaseHelper.TABLE_SHORT_MESSAGE_NAME;
                break;
            case PERSON:
                table = DatabaseHelper.TABLE_CONTACT_PERSON;
                break;
            case RECORD:
                table = DatabaseHelper.TABLE_CALL_RECORD;
                break;
            case VIDEO:
                table = DatabaseHelper.TABLE_VIDEO_RECORD;
                break;
            case VIDEO_ID:
                table = DatabaseHelper.TABLE_VIDEO_RECORD;
                break;
            case ACCOUNTS_STATUS_ID:
                LogUtils.d(TAG, "ACCOUNTS_STATUS_ID 添加操作");
                // 创建临时配置文件，并将其添加到临时集合中
                long id = ContentUris.parseId(uri);
                synchronized (mProfilesStatus) {
                    SipProfileState ps = new SipProfileState(); // 创建一个配置文件
                    if (mProfilesStatus.containsKey(id)) { // 包含
                        ContentValues currentValues = mProfilesStatus.get(id);
                        ps.createFromContentValue(currentValues);
                    }
                    ps.createFromContentValue(initialValues); // 更新他
                    ContentValues cv = ps.getAsContentValue();
                    cv.put(SipProfileState.ACCOUNT_ID, id);
                    mProfilesStatus.put(id, cv);
                }
                getContext().getContentResolver().notifyChange(uri, null);
                LogUtils.e(TAG, "profilesStatus.size(): " + mProfilesStatus.size());
                return uri;
            default:
                break;
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.insert(table, null, values);
        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);

            // 通知用户账户发生变化
            if (uriCode == ACCOUNTS || uriCode == ACCOUNTS_ID) {
                broadcastAccountChange(rowId);
            }

            return noteUri;
        }
        return null;
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        LogUtils.i(TAG, "执行 delete 操作");
        SQLiteDatabase sqLiteDatabase = mDatabaseHelper.getWritableDatabase();
        switch (URI_MATCHER.match(uri)) {
            case ACCOUNTS:
                sqLiteDatabase.delete(SipProfile.ACCOUNTS_TABLE_NAME, where, whereArgs);
                break;
            case ACCOUNTS_ID:
                sqLiteDatabase.delete(SipProfile.ACCOUNTS_TABLE_NAME, where, whereArgs);
                break;
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        LogUtils.i(TAG, "执行 update 操作");
        int uriCode = URI_MATCHER.match(uri);
        ContentValues initialValues;
        if (values != null) {
            initialValues = new ContentValues(values);
        } else {
            initialValues = new ContentValues();
        }
        String table = null;

        switch (uriCode) {
            case ACCOUNTS:
                table = SipProfile.ACCOUNTS_TABLE_NAME;
                break;
            case ACCOUNTS_ID:
                table = SipProfile.ACCOUNTS_TABLE_NAME;
                break;
            default:
                break;
        }

        SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        long rowId = db.update(table, initialValues, where, whereArgs);

        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(uri, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);

            // 通知用户账户发生变化
            if (uriCode == ACCOUNTS || uriCode == ACCOUNTS_ID) {
                broadcastAccountChange(rowId);
            }
        }

        return 0;
    }

    /**
     * Build a {@link Cursor} with a single row that contains all values
     * provided through the given {@link ContentValues}.
     */
    private Cursor getCursor(ContentValues[] contentValues) {
        if (contentValues.length > 0) {
            final Set<Entry<String, Object>> valueSet = contentValues[0].valueSet();
            int colSize = valueSet.size();
            final String[] keys = new String[colSize];

            int i = 0;
            for (Entry<String, Object> entry : valueSet) {
                keys[i] = entry.getKey();
                i++;
            }

            final MatrixCursor cursor = new MatrixCursor(keys);
            for (ContentValues cv : contentValues) {
                final Object[] values = new Object[colSize];
                i = 0;
                for (Entry<String, Object> entry : cv.valueSet()) {
                    values[i] = entry.getValue();
                    i++;
                }
                cursor.addRow(values);
            }
            return cursor;
        }
        return null;
    }

    /**
     * 通知用户账户发生变化
     *
     * @param accountId
     */
    private void broadcastAccountChange(long accountId) {
        Intent publishIntent = new Intent(SipManager.ACTION_SIP_ACCOUNT_CHANGED);
        publishIntent.putExtra(SipProfile.FIELD_ID, accountId);
        getContext().sendBroadcast(publishIntent);
    }
}
