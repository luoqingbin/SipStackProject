package com.crte.sipstackhome.ui.home;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseFragment;

/**
 * 组联系人
 * Created by Torment on 2015/10/30.
 */
public class GroupFragment extends BaseFragment {
    private ListView mListView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        mListView = (ListView) view.findViewById(R.id.listview);
        mListView.setAdapter(new MyAdapter());

        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                getActivity().startActivity(new Intent(getActivity(), GroupInfoActivity.class));
            }
        });
        return view;
    }

    public ListView getListView() {
        return mListView;
    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return 30;
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getActivity().getLayoutInflater().inflate(R.layout.item_contact_person, parent, false);
        }
    }
}
