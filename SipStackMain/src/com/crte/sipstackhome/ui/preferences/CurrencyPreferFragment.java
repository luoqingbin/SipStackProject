package com.crte.sipstackhome.ui.preferences;

import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crte.sipstackhome.R;

/**
 * Created by Administrator on 2016/1/11.
 */
public class CurrencyPreferFragment extends PreferenceFragment {
    public static final int PREFS_BASE = 1;
    public static final int PREFS_NETWORK = 2;
    public static final int PREFS_MEDIA = 3;

    private int preferState;

    public static CurrencyPreferFragment getCurrencyPreferFragment(int preferState) {
        CurrencyPreferFragment currencyPreferFragment = new CurrencyPreferFragment(preferState);
        return currencyPreferFragment;
    }

    public CurrencyPreferFragment(int preferState) {
        this.preferState = preferState;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        int resourceId = -1;
        switch (preferState) {
            case PREFS_BASE:
                resourceId = R.xml.prefs_base;
                break;
            case PREFS_NETWORK:
                resourceId = R.xml.prefs_network;
                break;
            case PREFS_MEDIA:
                resourceId = R.xml.prefs_media;
                break;
        }
        addPreferencesFromResource(resourceId);

        return super.onCreateView(inflater, container, savedInstanceState);
    }
}
