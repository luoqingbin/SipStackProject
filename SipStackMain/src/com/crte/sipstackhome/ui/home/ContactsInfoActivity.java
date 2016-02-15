package com.crte.sipstackhome.ui.home;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.customview.CircleImageView;
import com.crte.sipstackhome.dao.BaseDao;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.models.BaseBean;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.ui.message.MessageActivity;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;

/**
 * 联系人详情界面
 */
public class ContactsInfoActivity extends BaseActivity {
    public static final String ACTIVITY_USER_COLOR = "userColor"; // 颜色标记

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private LinearLayoutManager mLinearLayoutManager;
    private FloatingActionButton mFloatingActionButton;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;
    private MyContentObserver mMyContentObserver;
    private ContactsInfoAdapter mContactsInfoAdapter;

    private ArrayList<BaseBean> mBeanArrayList;

    int color;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        int mColor = getIntent().getIntExtra(ACTIVITY_USER_COLOR, -1);

        mToolbar = getActionBarToolbar(true);

        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.toolbar_layout);
        mCollapsingToolbarLayout.setTitle("用户信息");

        color = getResources().getColor(BaseActivity.HEADER_COLOR[mColor]);
        mCollapsingToolbarLayout.setBackgroundColor(color);
        mCollapsingToolbarLayout.setContentScrimColor(color);
        mCollapsingToolbarLayout.setStatusBarScrimColor(color);

        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mLinearLayoutManager = new LinearLayoutManager(this);
        mLinearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLinearLayoutManager);

        mMyContentObserver = new MyContentObserver(this, myHandler);
        getContentResolver().registerContentObserver(SipProfile.CONTACT_SHORT_MESSAGE_URI, true, mMyContentObserver);

        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        mFloatingActionButton.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startIntent(MessageActivity.class, false);
            }
        });

        initData();
        mContactsInfoAdapter = new ContactsInfoAdapter(this);
        mRecyclerView.setAdapter(mContactsInfoAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_call_record, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        return super.onOptionsItemSelected(item);
    }

    public void initData() {
        if (mBeanArrayList != null) {
            mBeanArrayList.clear();
        } else {
            mBeanArrayList = new ArrayList<>();
        }
        mBeanArrayList.add(BaseDao.getBaseBeanLayout(BaseDao.VIEW_TITLE, "详细信息"));
        mBeanArrayList.add(BaseDao.getBaseBeanLayout(BaseDao.VIEW_CONTENT, "内容"));
        Cursor cursor = getMessageManagedCursor(this);
        if (cursor.getCount() != 0) {
            BaseBean title2 = new BaseBean();
            title2.stateFlag = SipInfoState.VIEW_TITLE;
            title2.stateDescription = "通话记录";
            mBeanArrayList.add(title2); // 标题
            while (cursor.moveToNext()) {
                BaseBean message = new BaseBean();
                message.stateFlag = SipInfoState.VIEW_PHONE;
                mBeanArrayList.add(message);
            }
        }
        Cursor cursor2 = getMessageManagedCursor(this);
        if (cursor.getCount() != 0) {
            BaseBean title2 = new BaseBean();
            title2.stateFlag = SipInfoState.VIEW_TITLE;
            title2.stateDescription = "语音记录";
            mBeanArrayList.add(title2); // 标题1
            while (cursor2.moveToNext()) {
                BaseBean message = new BaseBean();
                message.stateFlag = SipInfoState.VIEW_MESSAGE;
                mBeanArrayList.add(message);
                break;
            }
        }
    }

    // 查询用户的最后三条通话记录
//    private final static String[] DATA_ALL = new String[]{DataBaseHelper.FIELD_ID, DataBaseHelper.FIELD_USERNAME,
//            DataBaseHelper.FIELD_MESSAGE, DataBaseHelper.FIELD_TIME, DataBaseHelper.FIELD_FLAG};

    private static Cursor getMessageManagedCursor(Activity activity) {
        Cursor cursor = activity.getContentResolver().query(SipProfile.CONTACT_SHORT_MESSAGE_URI,
                null,
                null,
                null,
                DatabaseContentProvider.DEFAULT_SORT_ORDER + " LIMIT 3");
        return cursor;
    }
    // 查询用户的最后三条短信记录

    class ContactsInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity activity;

        public ContactsInfoAdapter(Activity activity) {
            super();
            this.activity = activity;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case SipInfoState.VIEW_TITLE:
                    return new TitleViewHolder(activity.getLayoutInflater().inflate(R.layout.item_title, parent, false));
                case SipInfoState.VIEW_CONTENT:
                    return new ContentViewHolder(activity.getLayoutInflater().inflate(R.layout.item_base_info, parent, false));
                case SipInfoState.VIEW_PHONE:
                    return new ContactViewHolder(activity.getLayoutInflater().inflate(R.layout.item_user_info_phone, parent, false));
                case SipInfoState.VIEW_MESSAGE:
                    return new MessageViewHolder(activity.getLayoutInflater().inflate(R.layout.item_user_info_phone, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof TitleViewHolder) {
                ((TitleViewHolder) holder).mTitle.setText(mBeanArrayList.get(position).stateDescription);
                ((TitleViewHolder) holder).mTitle.setTextColor(color);
            } else if (holder instanceof ContentViewHolder) {
                ((ContentViewHolder) holder).mLiSendPhone.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ContactsInfoActivity.this, "点击呼叫语音", Toast.LENGTH_SHORT).show();
                    }
                });
                ((ContentViewHolder) holder).mLiSendVideo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(ContactsInfoActivity.this, "点击呼叫视频", Toast.LENGTH_SHORT).show();
                    }
                });

                ((ContentViewHolder) holder).mSendPhone.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                ((ContentViewHolder) holder).mSendVideo.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            } else if (holder instanceof ContactViewHolder) {
                ((ContactViewHolder) holder).mHeader.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                ((ContactViewHolder) holder).mHeader.setImageResource(R.drawable.ic_person_grey600);
            } else if (holder instanceof MessageViewHolder) {
                ((MessageViewHolder) holder).mHeader.setColorFilter(color, PorterDuff.Mode.MULTIPLY);
                ((MessageViewHolder) holder).mHeader.setImageResource(R.drawable.ic_person_grey600);
            }

        }

        @Override
        public int getItemViewType(int position) {
            switch (mBeanArrayList.get(position).stateFlag) {
                case SipInfoState.VIEW_TITLE:
                    return SipInfoState.VIEW_TITLE;

                case SipInfoState.VIEW_CONTENT:
                    return SipInfoState.VIEW_CONTENT;

                case SipInfoState.VIEW_PHONE:
                    return SipInfoState.VIEW_PHONE;

                case SipInfoState.VIEW_MESSAGE:
                    return SipInfoState.VIEW_MESSAGE;
            }
            return SipInfoState.VIEW_TITLE;
        }

        @Override
        public int getItemCount() {
            return mBeanArrayList.size();
        }

        // 标题布局
        class TitleViewHolder extends RecyclerView.ViewHolder {
            private TextView mTitle;

            public TitleViewHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.title);
            }
        }

        // 信息布局
        class ContentViewHolder extends RecyclerView.ViewHolder {
            private TextView mUsername;
            private ImageView mSendVideo;
            private ImageView mSendPhone;
            private LinearLayout mLiSendPhone;
            private LinearLayout mLiSendVideo;

            public ContentViewHolder(View itemView) {
                super(itemView);
                mUsername = (TextView) itemView.findViewById(R.id.username);
                mSendVideo = (ImageView) itemView.findViewById(R.id.send_video);
                mSendPhone = (ImageView) itemView.findViewById(R.id.send_phone);
                mLiSendPhone = (LinearLayout) itemView.findViewById(R.id.li_send_phone);
                mLiSendVideo = (LinearLayout) itemView.findViewById(R.id.li_send_video);
            }
        }

        // 短信布局
        class MessageViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mSendMessage;
            private CircleImageView mHeader;

            public MessageViewHolder(View itemView) {
                super(itemView);
                mSendMessage = (LinearLayout) itemView.findViewById(R.id.send_message);
                mHeader = (CircleImageView) itemView.findViewById(R.id.header);

            }
        }

        // 通话记录布局
        class ContactViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mSendPhone;
            private CircleImageView mHeader;

            public ContactViewHolder(View itemView) {
                super(itemView);
                mSendPhone = (LinearLayout) itemView.findViewById(R.id.send_phone);
                mHeader = (CircleImageView) itemView.findViewById(R.id.header);
            }
        }
    }

    class MyContentObserver extends ContentObserver {
        private Context mContext;
        private Handler mHandler;

        public MyContentObserver(Context context, Handler handler) {
            super(handler);
            this.mContext = context;
            this.mHandler = handler;
        }

        @Override
        public void onChange(boolean selfChange) {
            mHandler.sendEmptyMessage(0);
        }
    }

    private Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    initData();
                    LogUtils.d("这个观察者被执行了");
                    mContactsInfoAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        getContentResolver().unregisterContentObserver(mMyContentObserver);
    }
}
