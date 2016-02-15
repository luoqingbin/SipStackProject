package com.crte.sipstackhome.ui.message;

import android.app.Activity;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.models.ShortMessage;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.utils.UIUtils;

import java.util.ArrayList;

public class MessageRecordActivity extends BaseActivity {
    private static final String TAG = "MessageRecordActivity";

    private Toolbar mToolbar;
    private RecyclerView mRecyclerView;
    private MessageAdapter mMessageAdapter;
    private MyContentObserver mMyContentObserver;

    private ArrayList<ShortMessage> beanArrayList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message_record);

        mToolbar = getActionBarToolbar(false);
        mToolbar.setTitle("短信记录");
        mMessageAdapter = new MessageAdapter(this);
        mRecyclerView = getLinearLayoutRecyclerView(LinearLayoutManager.VERTICAL);
        ArrayList<ShortMessage> shortMessages = ShortMessage.getManagedCursor(this);
        beanArrayList.addAll(shortMessages);
        mRecyclerView.setAdapter(mMessageAdapter);

        mMyContentObserver = new MyContentObserver(this, myHandler);
        getContentResolver().registerContentObserver(SipProfile.CONTACT_SHORT_MESSAGE_URI, true, mMyContentObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_message_record, menu);
        final MenuItem item = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("搜索");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 提交时执行
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // 当字符串变化时执行
                beanArrayList.clear();
                beanArrayList.addAll(ShortMessage.getqueryDatas(MessageRecordActivity.this, newText));
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity mActivity;
        private LayoutInflater mLayoutInflater;

        public MessageAdapter(Activity activity) {
            this.mActivity = activity;
            this.mLayoutInflater = activity.getLayoutInflater();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new MessageViewHolder(mLayoutInflater.inflate(R.layout.item_message_record, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
            ((MessageViewHolder) holder).mMessageRecord.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(MessageRecordActivity.this, MessageActivity.class);
//                    intent.putExtra(CallInfoActivity.ACTIVITY_THEME_COLOR, beanArrayList.get(position).color);
//                    startActivity(intent);
                }
            });
            ((MessageViewHolder) holder).mUsername.setText(beanArrayList.get(position).fromUsername);
            ((MessageViewHolder) holder).mMessage.setText(beanArrayList.get(position).body);
            ((MessageViewHolder) holder).mTime.setText(beanArrayList.get(position).date + "");
            UIUtils.setImageViewColorFilter(mActivity, ((MessageViewHolder) holder).mHeader, beanArrayList.get(position).color);
        }

        @Override
        public int getItemCount() {
            return beanArrayList.size();
        }

        class MessageViewHolder extends RecyclerView.ViewHolder {
            public LinearLayout mMessageRecord;
            public ImageView mHeader;
            public TextView mUsername;
            public TextView mMessage;
            public TextView mTime;

            public MessageViewHolder(View itemView) {
                super(itemView);
                mMessageRecord = (LinearLayout) itemView.findViewById(R.id.message_record);
                mHeader = (ImageView) itemView.findViewById(R.id.header);
                mUsername = (TextView) itemView.findViewById(R.id.username);
                mMessage = (TextView) itemView.findViewById(R.id.message);
                mTime = (TextView) itemView.findViewById(R.id.time);
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
                    mMessageAdapter.notifyDataSetChanged();
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
