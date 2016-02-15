package com.crte.sipstackhome.ui.dialup;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.customview.ExtendEmptyRecyclerView;
import com.crte.sipstackhome.dao.BaseDao;
import com.crte.sipstackhome.dao.ContactPersonDao;
import com.crte.sipstackhome.impl.SipInfoState;
import com.crte.sipstackhome.models.BaseBean;
import com.crte.sipstackhome.models.Contacts;
import com.crte.sipstackhome.ui.BaseActivity;

import java.util.ArrayList;

/**
 * 拨号界面
 * Created by Administrator on 2015/11/4 0004.
 */
public class DialupActivity extends BaseActivity {
    private Toolbar mToolbar;
    private ExtendEmptyRecyclerView mExtendEmptyRecyclerView;
    private DialUpAdapter mDialUpAdapter;

    private ArrayList<BaseBean> mBeanArrayList = new ArrayList<>();
    private int mStateFlag = 0;
    private String mPhoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialup);

        mToolbar = getActionBarToolbar(true);
        mToolbar.setTitle("拨号");
        mExtendEmptyRecyclerView = (ExtendEmptyRecyclerView) getLinearLayoutRecyclerView(LinearLayoutManager.VERTICAL);
        mExtendEmptyRecyclerView.setEmptyView(findViewById(R.id.empty));
        mDialUpAdapter = new DialUpAdapter(this);
        mExtendEmptyRecyclerView.setAdapter(mDialUpAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_dialup, menu);
        final MenuItem item = menu.findItem(R.id.action_call);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setQueryHint("请输入呼叫号码");
        searchView.setInputType(InputType.TYPE_CLASS_NUMBER); // 弹出键盘类型
        searchView.setIconified(false);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { // 提交时执行
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) { // 当字符串变化时执行
                if (newText == null || "".equals(newText)) {
                    mBeanArrayList.clear();
                    mDialUpAdapter.notifyDataSetChanged();
                    return false;
                }
                mPhoneNumber = newText;
                initData(newText);
                mDialUpAdapter.notifyDataSetChanged();
                return false;
            }
        });
        return true;
    }

    /**
     * 初始化数据
     */
    public void initData(String username) {
        mBeanArrayList.clear();
        ContactPersonDao contactPersonDao = ContactPersonDao.getInstance();

        ArrayList<Contacts> contactList = contactPersonDao.getqueryDatas(this, username);
        if (contactList.size() == 0) {
            mStateFlag++;
        } else {
            mBeanArrayList.add(BaseDao.getBaseBeanLayout(BaseDao.VIEW_TITLE, "联系人"));
            mBeanArrayList.addAll(contactList);
        }

        ArrayList<Contacts> groupList = contactPersonDao.getqueryDatas(this, username);
        if (groupList.size() == 0) {
            mStateFlag++;
        } else {
            mBeanArrayList.add(BaseDao.getBaseBeanLayout(BaseDao.VIEW_TITLE, "组"));
            mBeanArrayList.addAll(groupList);
        }

        if (mStateFlag != 2) {
            return;
        }
        // 根据标记，查看当前的数据是不是空，如果空添加拨号界面
        mBeanArrayList.add(BaseDao.getBaseBeanLayout(BaseDao.VIEW_CALL, "拨号"));
        mStateFlag = 0;
    }

    /**
     * 拨号适配器
     */
    class DialUpAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private Activity mActivity;
        private LayoutInflater mLayoutInflater;

        public DialUpAdapter(Activity activity) {
            this.mActivity = activity;
            this.mLayoutInflater = mActivity.getLayoutInflater();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case SipInfoState.VIEW_TITLE:
                    return new TitleViewHolder(mLayoutInflater.inflate(R.layout.item_title, parent, false));
                case SipInfoState.VIEW_PHONE:
                    return new ContactViewHolder(mLayoutInflater.inflate(R.layout.item_contact_person, parent, false));
                case SipInfoState.VIEW_GROUP:
                    return new GroupViewHolder(mLayoutInflater.inflate(R.layout.item_contact_person, parent, false));
                case SipInfoState.VIEW_CALL:
                    return new CallViewHolder(mLayoutInflater.inflate(R.layout.item_call_message, parent, false));
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            if (holder instanceof TitleViewHolder) {
//                ((TitleViewHolder) holder).mTitle.setText(mBeanArrayList.get(position).stateDescription);
//            } else if (holder instanceof ContactViewHolder) {
//                ((ContactViewHolder) holder).mContactInfo.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startIntent(ContactsInfoActivity.class, false);
//                    }
//                });
//            } else if (holder instanceof GroupViewHolder) {
//                ((GroupViewHolder) holder).mContactInfo.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startIntent(GroupInfoActivity.class, false);
//                    }
//                });
//            } else if (holder instanceof CallViewHolder) {
//                ((CallViewHolder) holder).mSendPhone.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        startCallMenu(mPhoneNumber);
//                    }
//                });
//                ((CallViewHolder) holder).mSendMessage.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        // 发送消息未实现
//                    }
//                });
//            }
        }

        @Override
        public int getItemViewType(int position) {
            switch (mBeanArrayList.get(position).stateFlag) {
                case SipInfoState.VIEW_TITLE:
                    return SipInfoState.VIEW_TITLE;
                case SipInfoState.VIEW_PHONE:
                    return SipInfoState.VIEW_PHONE;
                case SipInfoState.VIEW_GROUP:
                    return SipInfoState.VIEW_GROUP;
                case SipInfoState.VIEW_CALL:
                    return SipInfoState.VIEW_CALL;
            }
            return -1;
        }

        @Override
        public int getItemCount() {
            return mBeanArrayList.size();
        }

        class TitleViewHolder extends RecyclerView.ViewHolder {
            private TextView mTitle;

            public TitleViewHolder(View itemView) {
                super(itemView);
                mTitle = (TextView) itemView.findViewById(R.id.title);
            }
        }

        class ContactViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mContactInfo;

            public ContactViewHolder(View itemView) {
                super(itemView);
                mContactInfo = (LinearLayout) itemView.findViewById(R.id.contact_info);
            }
        }

        class GroupViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mContactInfo;

            public GroupViewHolder(View itemView) {
                super(itemView);
                mContactInfo = (LinearLayout) itemView.findViewById(R.id.contact_info);
            }
        }

        class CallViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout mSendPhone;
            private LinearLayout mSendMessage;

            public CallViewHolder(View itemView) {
                super(itemView);
                mSendPhone = (LinearLayout) itemView.findViewById(R.id.send_phone);
                mSendMessage = (LinearLayout) itemView.findViewById(R.id.send_message);
            }
        }
    }
}