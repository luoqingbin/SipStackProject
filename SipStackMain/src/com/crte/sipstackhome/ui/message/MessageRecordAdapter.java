package com.crte.sipstackhome.ui.message;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseHelper;
import com.crte.sipstackhome.models.ShortMessage;
import com.crte.sipstackhome.utils.TimeUtils;


public class MessageRecordAdapter extends CursorAdapter {
	private final int cFieldID;
	private final int cFieldUsername;
	private final int cFieldMessage;
	private final int cFieldTime;
	private final int cFieldFlag;

	private Activity mActivity;
	private LayoutInflater mLayoutInflater;
	private int mUserId;

//	private final static String[] DATA_ALL = new String[] { DatabaseHelper.FIELD_ID, DatabaseHelper.FIELD_USERNAME,
//			DatabaseHelper.FIELD_MESSAGE, DatabaseHelper.FIELD_TIME, DatabaseHelper.FIELD_FLAG };

	public MessageRecordAdapter(Activity activity) {
		super(activity, getManagedCursor(activity), true);
		this.mActivity = activity;
		mLayoutInflater = activity.getLayoutInflater();
		Cursor cursor = getCursor();

		cFieldID = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_ID);
		cFieldUsername = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME);
		cFieldMessage = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_MESSAGE);
		cFieldTime = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_TIME);
		cFieldFlag = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_FLAG);
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mLayoutInflater.inflate(R.layout.item_message_record, parent, false);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.header = (ImageView) view.findViewById(R.id.header);
		viewHolder.username = (TextView) view.findViewById(R.id.username);
		viewHolder.message = (TextView) view.findViewById(R.id.message);
		viewHolder.time = (TextView) view.findViewById(R.id.time);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		viewHolder.username.setText(cursor.getString(cFieldUsername));
		viewHolder.message.setText(cursor.getString(cFieldMessage));
		viewHolder.time.setText(TimeUtils.getTimeLen(Long.parseLong(cursor.getString(cFieldTime))));
	}

	private class ViewHolder {
		ImageView header;
		TextView username;
		TextView message;
		TextView time;
	}

	private static Cursor getManagedCursor(Activity activity) {
		Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_SHORT_MESSAGE_URI, ShortMessage.FULL_PROJECTION, "username != -1) " + "GROUP BY (username", null, "_id DESC");
		return cursor;
	}

}
