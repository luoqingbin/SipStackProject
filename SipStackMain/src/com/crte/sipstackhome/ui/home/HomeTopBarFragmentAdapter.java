package com.crte.sipstackhome.ui.home;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseFragment;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/12/29.
 */
public class HomeTopBarFragmentAdapter extends FragmentPagerAdapter {
    private Context mContext;
    private ArrayList<BaseFragment> mFragments;

    public HomeTopBarFragmentAdapter(Context context, ArrayList<BaseFragment> fragments, FragmentManager fragmentManager) {
        super(fragmentManager);
        this.mFragments = fragments;
        this.mContext = context;
    }

    private int[] imageResId = {
            R.drawable.ic_person_white,
            R.drawable.ic_people_white,
    };

    private String[] textResId = {"人", "组"};

    public HomeTopBarFragmentAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public Fragment getItem(int arg0) {
        return mFragments.get(arg0);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        Drawable image = mContext.getResources().getDrawable(imageResId[position]);
        image.setBounds(0, 0, image.getIntrinsicWidth() / 2, image.getIntrinsicHeight() / 2);
        SpannableString sb = new SpannableString(" ");
        ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM);
        sb.setSpan(imageSpan, 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return sb;
    }
}
