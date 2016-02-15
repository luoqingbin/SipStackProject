package com.crte.sipstackhome.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipProfile;
import com.crte.sipstackhome.customview.CircleImageView;
import com.crte.sipstackhome.customview.SideBar;
import com.crte.sipstackhome.db.DatabaseContentProvider;
import com.crte.sipstackhome.models.Contacts;
import com.crte.sipstackhome.ui.BaseFragment;
import com.crte.sipstackhome.utils.UIUtils;

import java.util.ArrayList;

/**
 * 联系人
 * Created by Torment on 2015/10/30.
 */
public class ContactsFragment extends BaseFragment {
    private ListView mListView;
    private SideBar mSideBar;
    private ContactsAdapter mContactsAdapter;
    private TextView mContentDialog;
    private TextView mTitleCatalog;
    private ArrayList<Contacts> mContactsLists; // 联系人数据集合

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mSideBar = (SideBar) view.findViewById(R.id.sidrbar);
        mSideBar.setOnTouchingLetterChangedListener(new SideBar.OnTouchingLetterChangedListener() {
            @Override
            public void onTouchingLetterChanged(String s) {
                int position = mContactsAdapter.getPositionForSection(s.charAt(0));
                if (position != -1) {
                    mListView.setSelection(position);
                }
            }
        });

        mContentDialog = (TextView) view.findViewById(R.id.dialog); // 获得中间显示的数据
        mSideBar.setTextView(mContentDialog);
        initData();
        mListView = (ListView) view.findViewById(R.id.listview);
        mContactsAdapter = new ContactsAdapter(getActivity());
        mListView.setAdapter(mContactsAdapter);

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getActivity(), ContactsInfoActivity.class);
                intent.putExtra(ContactsInfoActivity.ACTIVITY_USER_COLOR, mContactsLists.get(position).color);
                getActivity().startActivity(intent);
            }
        });

        mTitleCatalog = (TextView) view.findViewById(R.id.title_catalog);
        return view;
    }

    private static Cursor getManagedCursor(Activity activity) {
        return activity.managedQuery(SipProfile.CONTACT_PERSON_CONTENT_URI, Contacts.FULL_PROJECTION, null, null, DatabaseContentProvider.DEFAULT_SORT_ORDER);
    }

    public void initData() {
        if (mContactsLists == null) {
            mContactsLists = new ArrayList<>();
        } else {
            mContactsLists.clear();
        }

        Cursor cursor = getManagedCursor(getActivity());
        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                Contacts contacts = new Contacts();
                contacts.username = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.FIELD_USERNAME));
                contacts.sortLetters = cursor.getString(cursor.getColumnIndexOrThrow(Contacts.FIELD_SORT_LETTERS));
                contacts.color = cursor.getInt(cursor.getColumnIndexOrThrow(Contacts.FIELD_COLOR));
                mContactsLists.add(contacts);
            }
        }
    }

    public ListView getListView() {
        return mListView;
    }

    public void setTitleCatalog(String title) {
        mTitleCatalog.setText(title);
    }

    class ContactsAdapter extends BaseAdapter {
        private Activity mActivity;
        private LayoutInflater mLayoutInflater;

        public ContactsAdapter(Activity activity) {
            this.mActivity = activity;
            this.mLayoutInflater = mActivity.getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mContactsLists.size();
        }

        @Override
        public Object getItem(int position) {
            return mContactsLists.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ContactViewHolder mViewHolder = null;
            if (convertView == null) {
                mViewHolder = new ContactViewHolder();
                convertView = mLayoutInflater.inflate(R.layout.item_contact_person, parent, false);
                mViewHolder.mUsername = (TextView) convertView.findViewById(R.id.username);
                mViewHolder.mCatalog = (TextView) convertView.findViewById(R.id.catalog);
                mViewHolder.mHeaderImageView = (CircleImageView) convertView.findViewById(R.id.header_imageview);
                convertView.setTag(mViewHolder);
            } else {
                mViewHolder = (ContactViewHolder) convertView.getTag();
            }

            // 显示操作
            int section = getSectionForPosition(position);

            //如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
            if (position == getPositionForSection(section)) {
                mViewHolder.mCatalog.setVisibility(View.VISIBLE);
                mViewHolder.mCatalog.setText(mContactsLists.get(position).sortLetters);
            } else {
                mViewHolder.mCatalog.setVisibility(View.INVISIBLE);
            }
            mViewHolder.mUsername.setText(mContactsLists.get(position).username);
            UIUtils.setImageViewColorFilter(mActivity, mViewHolder.mHeaderImageView, mContactsLists.get(position).color);
            return convertView;
        }

        public int getSectionForPosition(int position) {
            return mContactsLists.get(position).sortLetters.charAt(0);
        }

        /**
         * 根据分类的首字母的Char ascii值获取其第一次出现该首字母的位置
         */
        public int getPositionForSection(int section) {
            for (int i = 0; i < getCount(); i++) {
                String sortStr = mContactsLists.get(i).sortLetters;
                char firstChar = sortStr.toUpperCase().charAt(0);
                if (firstChar == section) {
                    return i;
                }
            }
            return -1;
        }

        class ContactViewHolder {
            private CircleImageView mHeaderImageView;
            private TextView mUsername;
            private TextView mCatalog;
        }
    }
}
