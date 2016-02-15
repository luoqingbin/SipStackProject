package com.crte.sipstackhome.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Build;
import android.view.View;
import android.widget.ImageView;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.ui.BaseActivity;

/**
 * Created by Torment on 2015/8/17.
 */
public class UIUtils {
    @SuppressLint("NewApi")
    public static void setAccessibilityIgnore(View view) {
        view.setClickable(false);
        view.setFocusable(false);
        view.setContentDescription("");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            view.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        }
    }

    public static void setImageViewColorFilter(Activity activity, ImageView imageView, int color) {
        ImageView colorImage = imageView;
        colorImage.setColorFilter(activity.getResources().getColor(BaseActivity.HEADER_COLOR[color]), PorterDuff.Mode.MULTIPLY);
        colorImage.setImageResource(R.drawable.ic_person_grey600);
    }

    public static void setImageViewColorFilter(Activity activity, ImageView imageView, int color, int drawable) {
        ImageView colorImage = imageView;
        colorImage.setColorFilter(activity.getResources().getColor(BaseActivity.HEADER_COLOR[color]), PorterDuff.Mode.MULTIPLY);
        colorImage.setImageResource(drawable);
    }

}
 