package com.crte.sipstackhome.ui.message;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.db.DatabaseHelper;
import com.crte.sipstackhome.utils.TimeUtils;


public class ShortMessageAdapter extends CursorAdapter {
	private final int cFieldID;
	private final int cFieldUsername;
	private final int cFieldMessage;
	private final int cFieldTime;
	private final int cFieldFlag;

	private Activity mActivity;
	private LayoutInflater mLayoutInflater;
	private int mUserId;

	private final static String[] DATA_ALL = new String[] { DatabaseHelper.FIELD_ID, DatabaseHelper.FIELD_USERNAME,
			DatabaseHelper.FIELD_MESSAGE, DatabaseHelper.FIELD_TIME, DatabaseHelper.FIELD_FLAG };
	
	public ShortMessageAdapter(Activity activity, int userId) {
		super(activity, getManagedCursor(activity, userId), true);
		this.mActivity = activity;
		mLayoutInflater = activity.getLayoutInflater();
		Cursor cursor = getCursor();

		cFieldID = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_ID);
		cFieldUsername = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_USERNAME);
		cFieldMessage = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_MESSAGE);
		cFieldTime = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_TIME);
		cFieldFlag = cursor.getColumnIndexOrThrow(DatabaseHelper.FIELD_FLAG);
		
	}

	/** 查询指定用户的记录 */
	private static Cursor getManagedCursor(Activity activity, int userId) {
		Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_SHORT_MESSAGE_URI,
				DATA_ALL, 
				null,
				null,
				DatabaseContentProvider.DEFAULT_SORT_ORDER);
		return cursor;
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View view = mLayoutInflater.inflate(R.layout.item_message, parent, false);
		ViewHolder viewHolder = new ViewHolder();
		viewHolder.mLeftBackground = (LinearLayout) view.findViewById(R.id.left_background);
		viewHolder.mLeft = (RelativeLayout) view.findViewById(R.id.left);
		viewHolder.mLeftHeader = (ImageView) view.findViewById(R.id.left_header);
		viewHolder.mLeftMessage = (TextView) view.findViewById(R.id.left_message);
		viewHolder.mLeftTime = (TextView) view.findViewById(R.id.left_time);
		viewHolder.mRightBackground = (LinearLayout) view.findViewById(R.id.right_background);
		viewHolder.mRight = (RelativeLayout) view.findViewById(R.id.right);
		viewHolder.mRightHeader = (ImageView) view.findViewById(R.id.right_header);
		viewHolder.mRightMessage = (TextView) view.findViewById(R.id.right_message);
		viewHolder.mRightTime = (TextView) view.findViewById(R.id.right_time);
		view.setTag(viewHolder);
		return view;
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder viewHolder = (ViewHolder) view.getTag();
		// viewHolder.header
		if ("1".equals(cursor.getString(cFieldFlag))) { // left
			viewHolder.mLeft.setVisibility(View.VISIBLE);
			viewHolder.mRight.setVisibility(View.GONE);
			viewHolder.mLeftMessage.setText(cursor.getString(cFieldMessage));
			viewHolder.mLeftHeader.setColorFilter(context.getResources().getColor(R.color.blue_500));
			viewHolder.mLeftTime.setText(TimeUtils.getTimeLen(Long.parseLong(cursor.getString(cFieldTime))));
			Drawable drawable = context.getResources().getDrawable(R.drawable.ic_launcher);
			drawable.setColorFilter(context.getResources().getColor(R.color.blue_50), PorterDuff.Mode.MULTIPLY);
			viewHolder.mLeftBackground.setBackgroundDrawable(drawable);
		} else { // right
			viewHolder.mLeft.setVisibility(View.GONE);
			viewHolder.mRight.setVisibility(View.VISIBLE);
			viewHolder.mRightMessage.setText(cursor.getString(cFieldMessage));
			viewHolder.mRightTime.setText(TimeUtils.getTimeLen(Long.parseLong(cursor.getString(cFieldTime))));
			viewHolder.mRightHeader.setColorFilter(context.getResources().getColor(R.color.blue_600));
			Drawable drawable = context.getResources().getDrawable(R.drawable.ic_launcher);
			drawable.setColorFilter(context.getResources().getColor(R.color.blue_300), PorterDuff.Mode.MULTIPLY);
			viewHolder.mRightBackground.setBackgroundDrawable(drawable);
		}
	}
	
	@Override
	public int getViewTypeCount() {
		return super.getViewTypeCount();
	}

	@Override
	public int getItemViewType(int position) {
		return 2;
	}

	private static class ViewHolder {
		LinearLayout mLeftBackground;
		LinearLayout mRightBackground;
		RelativeLayout mRight;
		RelativeLayout mLeft;
		ImageView mLeftHeader;
		TextView mLeftMessage;
		TextView mLeftTime;
		ImageView mRightHeader;
		TextView mRightMessage;
		TextView mRightTime;
	}
	
}
