package com.crte.sipstackhome.ui;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.customview.MultiSwipeRefreshLayout;
import com.crte.sipstackhome.ui.call.CallRecordActivity;
import com.crte.sipstackhome.ui.home.HomeActivity;
import com.crte.sipstackhome.ui.message.MessageRecordActivity;
import com.crte.sipstackhome.ui.preferences.MainPrefsActivity;
import com.crte.sipstackhome.ui.video.VideoRecordActivity;
import com.crte.sipstackhome.utils.UIUtils;

import java.util.ArrayList;

/**
 * 大部分Activity都将继承这个基本Activity，这个Activity负责DrawerLayout，
 * SwipeRefreshLayout以及Toobar的创建，以及一些通用方法
 */
public abstract class BaseActivity extends AppCompatActivity implements MultiSwipeRefreshLayout.CanChildScrollUpCallback {
    public static final String ACTIVITY_THEME_COLOR = "activity.theme.color";

    public static final int FLAG_INIT_SYNC_DATA = 1;
    public static final int FLAG_SYNC_DATA = 2;
    public static final int FLAG_CLOSE_ACTIVITY = 3;
    public static final int FLAG_CHANGE_PTT_STATE = 4;
    public static final int FLAG_DEFAULT_PTT_STATE = 5;

    public static final int[] HEADER_COLOR = {R.color.red_500, R.color.teal_500, R.color.blue_500, R.color.deep_orange_500, R.color.blue_grey_500, R.color.deep_purple_500};
    public static final int[] HEADER_COLOR_BAR = {R.color.red_800, R.color.teal_800, R.color.blue_800, R.color.deep_orange_800, R.color.blue_grey_800, R.color.deep_purple_800};

    protected static final int ITEM_CONTACT_PERSON = 0;
    protected static final int ITEM_CALL_RECORDING = 1;
    protected static final int ITEM_MESSAGE_RECORD = 2;
    protected static final int ITEM_VIDEO_RECORD = 3;
    protected static final int ITEM_SETTING = 4;
    protected static final int ITEM_FILLING_LINE = -1;
    protected static final int ITEM_SEGMENTATION_LINE = -2;
    protected static int ITEM_SELECT_INVALID = ITEM_CONTACT_PERSON;
    protected static int ITEM_SELECT = ITEM_CONTACT_PERSON;

    protected SharedPreferences mSettings;
    protected DrawerLayout mDrawerLayout;
    protected BaseActionBarDrawerToggle mActionBarDrawerToggle;
    protected LinearLayout mSelectListItem;
    protected LinearLayout mUserInfo;
    protected Toolbar mActionBarToolbar;
    protected SwipeRefreshLayout mSwipeRefreshLayout;
    //    protected SwipeRefreshSyncObserver mSwipeRefreshSyncObserver;
    protected RecyclerView mRecyclerView;
    protected LinearLayoutManager mLinearLayoutManager;
    protected AlertDialog mAlertDialog;
    protected TextView mNetworkState;

    private ArrayList<Integer> mNavDrawerItems = new ArrayList<Integer>();
    private View[] mNavDrawerItemViews = null;

    private static final int[] NAVDRAWER_TITLE_RES_ID = new int[]{
            R.string.contact_personBean,
            R.string.call_record,
            R.string.message_record,
            R.string.video_record,
            R.string.ui_settings,
    };

    private static final int[] NAVDRAWER_ICON_RES_ID = new int[]{
            R.drawable.ic_people_white,
            R.drawable.ic_call_white,
            R.drawable.ic_question_answer_white,
            R.drawable.ic_chat_white,
            R.drawable.ic_settings_white,
    };

    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        mSettings = getSharedPreferences(Settings.sharedPrefsFile, Integer.parseInt(Build.VERSION.SDK) >= 11 ? 4 : MODE_PRIVATE);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setDrawerLayout();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
//            case R.id.action_search:
//                startActivity(new Intent(BaseActivity.this, SearchActivity.class));
//                return true;
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        if (mDrawerLayout == null) {
            return;
        }

        overridePendingTransition(0, 0);
        mSelectListItem = (LinearLayout) findViewById(R.id.select_list_item);
        if (mSelectListItem != null) {
            populateNavDrawer();
        }

        if (mActionBarToolbar != null) {
            mActionBarToolbar.setNavigationIcon(R.drawable.ic_launcher);
            mActionBarToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mDrawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }

        mUserInfo = (LinearLayout) findViewById(R.id.left_user_info);
        mUserInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
//                startIntent(UserInfoActivity.class, false);
            }
        });

        mActionBarDrawerToggle = new BaseActionBarDrawerToggle(this, mDrawerLayout, mActionBarToolbar, R.string.drawer_open, R.string.drawer_close);
        mActionBarDrawerToggle.syncState();
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
    }

    @Override
    protected void onResume() {
        super.onResume();
//        TextView leftUsername = (TextView) findViewById(R.id.left_username);
//        TextView leftAddress = (TextView) findViewById(R.id.left_id_address);
//
//        if (leftUsername == null || leftAddress == null) {
//            return;
//        }
//
//        leftUsername.setText(getUsername());
//        leftAddress.setText(getServer());
    }

    protected Toolbar getActionBarToolbar(boolean isDisplayHomeAsUpEnabled) {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mActionBarToolbar != null) {
                mActionBarToolbar.setTitle("");
                setSupportActionBar(mActionBarToolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(isDisplayHomeAsUpEnabled);
            }
        }
        return mActionBarToolbar;
    }

    protected RecyclerView getLinearLayoutRecyclerView(int orientation) {
        if (mRecyclerView == null) {
            mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
            if (mRecyclerView != null) {
                mLinearLayoutManager = new LinearLayoutManager(this);
                mLinearLayoutManager.setOrientation(orientation);
                mRecyclerView.setLayoutManager(mLinearLayoutManager);
            }
        }
        return mRecyclerView;
    }

    protected SwipeRefreshLayout getSwipeRefreshLayout() {
        if (mSwipeRefreshLayout == null) {
            mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swiperefresh);
            mSwipeRefreshLayout.setColorSchemeResources(HEADER_COLOR);
//            if (SignApplication.APPLICATION_INIT_SYNC) {
//                mSwipeRefreshLayout.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        SignApplication.APPLICATION_INIT_SYNC = false;
//                        mSwipeRefreshLayout.setRefreshing(true);
//                        requestSync();
//                    }
//                });
//            }

            mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    requestSync();
                }
            });
//            mSwipeRefreshSyncObserver = new SwipeRefreshSyncObserver();
//            SyncService.sSyncAdapter.registeredObserver(mSwipeRefreshSyncObserver);

            if (mSwipeRefreshLayout instanceof MultiSwipeRefreshLayout) {
                MultiSwipeRefreshLayout mMultiSwipeRefreshLayout = (MultiSwipeRefreshLayout) mSwipeRefreshLayout;
                mMultiSwipeRefreshLayout.setCanChildScrollUpCallback(this);
            }
        }
        return mSwipeRefreshLayout;
    }

    protected void requestSync() {
        Snackbar.make(mSwipeRefreshLayout, "开始同步数据...", Snackbar.LENGTH_SHORT).show();
//        SyncUtils.getInstance(BaseActivity.this).requestSync(setSyncMark());
    }

    protected Bundle setSyncMark() {
        return null;
    }

    @Override
    public boolean canSwipeRefreshChildScrollUp() {
        return false;
    }

    protected void populateNavDrawer() {
        mNavDrawerItems.add(ITEM_FILLING_LINE);
        mNavDrawerItems.add(ITEM_CONTACT_PERSON);
        mNavDrawerItems.add(ITEM_CALL_RECORDING);
        mNavDrawerItems.add(ITEM_MESSAGE_RECORD);
        mNavDrawerItems.add(ITEM_VIDEO_RECORD);
        mNavDrawerItems.add(ITEM_SEGMENTATION_LINE);
        mNavDrawerItems.add(ITEM_SETTING);

        mNavDrawerItemViews = new View[mNavDrawerItems.size()];
        mSelectListItem.removeAllViews();
        int i = 0;
        for (int itemId : mNavDrawerItems) {
            mNavDrawerItemViews[i] = makeNavDrawerItem(itemId, mSelectListItem);
            mSelectListItem.addView(mNavDrawerItemViews[i]);
            ++i;
        }
    }

    protected View makeNavDrawerItem(final int itemId, ViewGroup container) {
        boolean selected = selectItemInvalid() == itemId;
        int layoutToInflate = 0;
        if (itemId == ITEM_FILLING_LINE) {
            layoutToInflate = R.layout.item_filling_line;
        } else if (itemId == ITEM_SEGMENTATION_LINE) {
            layoutToInflate = R.layout.item_segmentation_line;
        } else {
            layoutToInflate = R.layout.item_navdrawer;
        }
        View view = getLayoutInflater().inflate(layoutToInflate, container, false);

        if (isSeparator(itemId)) {
            UIUtils.setAccessibilityIgnore(view);
            return view;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);
        int iconId = itemId >= 0 && itemId < NAVDRAWER_ICON_RES_ID.length ? NAVDRAWER_ICON_RES_ID[itemId] : 0;
        int titleId = itemId >= 0 && itemId < NAVDRAWER_TITLE_RES_ID.length ? NAVDRAWER_TITLE_RES_ID[itemId] : 0;

        iconView.setVisibility(iconId > 0 ? View.VISIBLE : View.GONE);
        if (iconId > 0) {
            iconView.setImageResource(iconId);
        }
        titleView.setText(getString(titleId));

        formatNavDrawerItem(view, itemId, selected);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNavDrawerItemClicked(itemId);
            }
        });
        return view;
    }

//    protected String getUsername() {
//        return mSettings.getString(Settings.PREF_USERNAME, "");
//    }
//
//    protected String getServer() {
//        return mSettings.getString(Settings.PREF_SERVER, "");
//    }

    protected void formatNavDrawerItem(View view, int itemId, boolean selected) {
        if (isSeparator(itemId)) {
            return;
        }

        ImageView iconView = (ImageView) view.findViewById(R.id.icon);
        TextView titleView = (TextView) view.findViewById(R.id.title);

        if (selected) {
            view.setBackgroundResource(R.drawable.selected_navdrawer_item_background);
        } else {
            view.setBackgroundResource(R.drawable.selected_navdrawer_item_background_while);
        }

        titleView.setTextColor(selected ? getResources().getColor(R.color.indigo_500) : getResources().getColor(R.color.navdrawer_text_color));
        iconView.setColorFilter(selected ? getResources().getColor(R.color.indigo_500) : getResources().getColor(R.color.navdrawer_icon_tint));
    }

    protected int selectItemInvalid() {
        return ITEM_SELECT_INVALID;
    }

    private boolean isSeparator(int itemId) {
        return itemId == ITEM_FILLING_LINE || itemId == ITEM_SEGMENTATION_LINE;
    }

    protected void onNavDrawerItemClicked(int itemId) {
        if (ITEM_SELECT_INVALID == itemId) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        ITEM_SELECT_INVALID = itemId;
        if (ITEM_SETTING != ITEM_SELECT_INVALID) {
            setSelectedNavDrawerItem(itemId);
        }

        mDrawerLayout.closeDrawer(GravityCompat.START);
    }

    private void setSelectedNavDrawerItem(int itemId) {
        if (mNavDrawerItemViews != null) {
            for (int i = 0; i < mNavDrawerItemViews.length; i++) {
                if (i < mNavDrawerItems.size()) {
                    int thisItemId = mNavDrawerItems.get(i);
                    formatNavDrawerItem(mNavDrawerItemViews[i], thisItemId, itemId == thisItemId);
                }
            }
        }
    }

    protected void startIntent(Class<?> cla, boolean finishState) {
        Intent intent = new Intent(BaseActivity.this, cla);
        startActivity(intent);
        if (finishState) {
            finish();
        }
    }

    protected void setNewWorkState(String stateMessage) {
        mNetworkState = (TextView) findViewById(R.id.left_network_state);

        if (mNetworkState != null) {
            mNetworkState.setText(stateMessage);
        }
    }


    @Override
    public void onBackPressed() {
        if (mDrawerLayout != null && mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            if (this instanceof CallRecordActivity || this instanceof HomeActivity || this instanceof MessageRecordActivity) {
                System.exit(0);
            }
        }
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        if (mSwipeRefreshSyncObserver != null) {
//            SyncService.sSyncAdapter.unRegisteredObserver(mSwipeRefreshSyncObserver);
//        }
//    }

    public void setCallState(String state) {
    }

    public void setCallUserInfoState(String targetUri, String fromUri) {
    }

//    private class SwipeRefreshSyncObserver implements SyncObserver {
//        @Override
//        public void syncState(boolean state) {
//            mHandler.sendEmptyMessage(FLAG_SYNC_DATA);
//        }
//    }

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case FLAG_INIT_SYNC_DATA:
                    mSwipeRefreshLayout.setRefreshing(true);
                    break;
                case FLAG_SYNC_DATA:
                    if (mSwipeRefreshLayout != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                    }
                    break;
            }
        }
    };

    private class BaseActionBarDrawerToggle extends ActionBarDrawerToggle {

        public BaseActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        public BaseActionBarDrawerToggle(Activity activity, DrawerLayout drawerLayout, Toolbar toolbar, int openDrawerContentDescRes, int closeDrawerContentDescRes) {
            super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        }

        @Override
        public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            if (ITEM_SELECT == ITEM_SELECT_INVALID) {
                return;
            }

            if (ITEM_SETTING != ITEM_SELECT_INVALID) {
                ITEM_SELECT = ITEM_SELECT_INVALID;
            } else {
                ITEM_SELECT_INVALID = ITEM_SELECT;
                startIntent(MainPrefsActivity.class, false);
                return;
            }
            switch (ITEM_SELECT) {
                case ITEM_CONTACT_PERSON:
                    startIntent(HomeActivity.class, true);
                    break;
                case ITEM_CALL_RECORDING:
                    startIntent(CallRecordActivity.class, true);
                    break;
                case ITEM_MESSAGE_RECORD:
                    startIntent(MessageRecordActivity.class, true);
                    break;
                case ITEM_VIDEO_RECORD:
                    startIntent(VideoRecordActivity.class, true);
                    break;
            }
        }
    }
}