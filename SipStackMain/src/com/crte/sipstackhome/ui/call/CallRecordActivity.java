package com.crte.sipstackhome.ui.call;

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
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.customview.CircleImageView;
import com.crte.sipstackhome.customview.ExtendEmptyRecyclerView;
import com.crte.sipstackhome.dao.CallRecordDao;
import com.crte.sipstackhome.models.CallRecord;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.utils.UIUtils;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.util.ArrayList;

/**
 * 通话记录
 */
public class CallRecordActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ExtendEmptyRecyclerView mExtendEmptyRecyclerView;
    private CallRecordAdapter mCallRecordAdapter;
    private MyContentObserver mMyContentObserver;

    private ArrayList<CallRecord> callRecordLists = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_call_record);
        mToolbar = getActionBarToolbar(false);
        mToolbar.setTitle("通话记录");

        mCallRecordAdapter = new CallRecordAdapter(this);
        mExtendEmptyRecyclerView = (ExtendEmptyRecyclerView) getLinearLayoutRecyclerView(LinearLayoutManager.VERTICAL);
        mExtendEmptyRecyclerView.setEmptyView(findViewById(R.id.empty));
        mExtendEmptyRecyclerView.setAdapter(mCallRecordAdapter);

        mMyContentObserver = new MyContentObserver(this, myHandler);
        getContentResolver().registerContentObserver(SipProfile.CONTACT_CALL_RECORD, true, mMyContentObserver);

        // 获得用户信息
        ArrayList<CallRecord> callRecords = CallRecordDao.getInstance().getManagedCursor(this);
        callRecordLists.addAll(callRecords);
        mCallRecordAdapter.notifyDataSetChanged();
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
                callRecordLists.clear();
                callRecordLists.addAll(CallRecordDao.getInstance().getqueryDatas(CallRecordActivity.this, newText));
                mCallRecordAdapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class CallRecordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity mActivity;
        private LayoutInflater mLayoutInflater;

        public CallRecordAdapter(Activity activity) {
            this.mActivity = activity;
            this.mLayoutInflater = activity.getLayoutInflater();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new CallRecordViewHolder(mLayoutInflater.inflate(R.layout.item_call_record, parent, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
//            ((CallRecordViewHolder) holder).mUsername.setText(callRecordLists.get(position).username);
            ((CallRecordViewHolder) holder).mCallInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Intent intent = new Intent(CallRecordActivity.this, CallInfoActivity.class);
//                    intent.putExtra(CallInfoActivity.ACTIVITY_THEME_COLOR, callRecordLists.get(position).color);
//                    startActivity(intent);
                }
            });
            UIUtils.setImageViewColorFilter(mActivity, ((CallRecordViewHolder) holder).mHeader, callRecordLists.get(position).color);
        }

        @Override
        public int getItemCount() {
            return callRecordLists.size();
        }

        class CallRecordViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mCallInfo;
            private TextView mUsername;
            private CircleImageView mHeader;

            public CallRecordViewHolder(View itemView) {
                super(itemView);
                mUsername = (TextView) itemView.findViewById(R.id.username);
                mCallInfo = (LinearLayout) itemView.findViewById(R.id.call_info);
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
                    LogUtils.d("---这个观察者被执行了");
                    mCallRecordAdapter.notifyDataSetChanged();
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
