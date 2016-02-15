package com.crte.sipstackhome.ui.preferences;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseActivity;

/**
 * Created by Administrator on 2015/12/29.
 */
public class MainPrefsActivity extends BaseActivity implements PrefsHeadersFragment.ToFromFragment {
    private Toolbar mToolbar;

    private PrefsHeadersFragment mPrefsHeadersFragment;

    private boolean mFragmentFlag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainprefs);

        mToolbar = getActionBarToolbar(true);
        if (savedInstanceState == null) {
            mPrefsHeadersFragment = new PrefsHeadersFragment();
            mPrefsHeadersFragment.settoFromFragment(this);
            settingFragmet(mPrefsHeadersFragment, "all", "设置");
            mFragmentFlag = false;
        }
    }

    public void settingFragmet(Fragment fragment, String strFlag, String setting) {
        mToolbar.setTitle(setting);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.settings, fragment, strFlag);
        fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmentTransaction.commit();
    }


    @Override
    public void onBackPressed() {
        closeFragmentToActivity();
    }

    public void closeFragmentToActivity() {
        if (mFragmentFlag) {
            closeFragment();
        } else {
            finish();
        }
    }

    public void closeFragment() {
        mFragmentFlag = false;
        settingFragmet(mPrefsHeadersFragment, "all", "设置");
    }

    @Override
    public void toBaseFragment() {
        mFragmentFlag = true;
        settingFragmet(CurrencyPreferFragment.getCurrencyPreferFragment(CurrencyPreferFragment.PREFS_BASE), "base", "基本设置");
    }

    @Override
    public void toUserFragment() {
        mFragmentFlag = true;
        settingFragmet(CurrencyPreferFragment.getCurrencyPreferFragment(CurrencyPreferFragment.PREFS_NETWORK), "network", "网络设置");
    }

    @Override
    public void toSettingFragment() {
        mFragmentFlag = true;
        settingFragmet(CurrencyPreferFragment.getCurrencyPreferFragment(CurrencyPreferFragment.PREFS_MEDIA), "media", "媒体设置");
    }
}
