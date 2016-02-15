package com.crte.sipstackhome.ui.home;

import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.ui.BaseActivity;
import com.crte.sipstackhome.ui.BaseFragment;
import com.crte.sipstackhome.ui.login.LoginActivity;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/29.
 */
public class HomeActivity extends BaseActivity {
    private Toolbar mToolbar;
    private SwipeRefreshLayout mMultiSwipeRefreshLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private FloatingActionButton mCallPhone;

    private ArrayList<BaseFragment> mFragmentList = new ArrayList<>();

    private HomeTopBarFragmentAdapter mTopBarFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mToolbar = getActionBarToolbar(false);
        mToolbar.setTitle("联系人");

        mTabLayout = (TabLayout) findViewById(R.id.sliding_tabs);

        mMultiSwipeRefreshLayout = getSwipeRefreshLayout();
        mViewPager = (ViewPager) findViewById(R.id.view_pager);

        mFragmentList.clear();
        mFragmentList.add(new ContactsFragment());
        mFragmentList.add(new GroupFragment());

        mTopBarFragmentAdapter = new HomeTopBarFragmentAdapter(this, mFragmentList, getSupportFragmentManager());
        mViewPager.setAdapter(mTopBarFragmentAdapter);
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.setTabsFromPagerAdapter(mTopBarFragmentAdapter);
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);

        mCallPhone = (FloatingActionButton) findViewById(R.id.call_phone);
        mCallPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                startIntent(DialupActivity.class, false);
                try {
                    UserAgentBroadcastReceiver.getUserAgent().sendMessage("100", "test to 100", LoginActivity.ACCONT_ID);
                } catch (SameThreadException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        for (int i = 0; i < mFragmentList.size(); i++) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                if (!mFragmentList.get(i).getUserVisibleHint()) {
                    continue;
                }
            }
            Log.i("", "mFragmentList: " + mFragmentList);
            return ViewCompat.canScrollVertically(mFragmentList.get(i).getListView(), -1);
        }
        return false;
    }
}
