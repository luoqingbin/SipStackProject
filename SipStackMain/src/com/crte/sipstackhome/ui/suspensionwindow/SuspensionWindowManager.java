package com.crte.sipstackhome.ui.suspensionwindow;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.WindowManager;

import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;

/**
 * Created by Administrator on 2016/1/22.
 */
public class SuspensionWindowManager {
    private static WindowManager mWindowManager;
    private static VideoSuspensionWindow mVideoSuspensionWindow;
    private static WindowManager.LayoutParams mVideoLayoutParams;


    public static void createVideoWindow(Context context) {
        WindowManager windowManager = getWindowManager(context);
        int screenWidth = windowManager.getDefaultDisplay().getWidth();
        int screenHeight = windowManager.getDefaultDisplay().getHeight();

        if (mVideoSuspensionWindow == null) {
            mVideoSuspensionWindow = new VideoSuspensionWindow(context);
            if (mVideoLayoutParams == null) {
                mVideoLayoutParams = new WindowManager.LayoutParams();
                mVideoLayoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                mVideoLayoutParams.format = PixelFormat.RGBA_8888;
                mVideoLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mVideoLayoutParams.gravity = Gravity.LEFT | Gravity.TOP;
                mVideoLayoutParams.width = VideoSuspensionWindow.mViewWidth;
                mVideoLayoutParams.height = VideoSuspensionWindow.mViewHeight;
                mVideoLayoutParams.x = screenWidth;
                mVideoLayoutParams.y = screenHeight / 2;
            }
            mVideoSuspensionWindow.setParams(mVideoLayoutParams);
            windowManager.addView(mVideoSuspensionWindow, mVideoLayoutParams);
        }
    }

    /**
     * 将大悬浮窗从屏幕上移除。
     *
     * @param context 必须为应用程序的Context.
     */
    public static void removeVideoWindow(Context context) {
        if (mVideoSuspensionWindow != null) {
            UserAgentBroadcastReceiver.setVideoAndroidCapturer(null);
            WindowManager windowManager = getWindowManager(context);
            windowManager.removeView(mVideoSuspensionWindow);
            mVideoSuspensionWindow = null;
        }
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @param context 必须为应用程序的Context.
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    private static WindowManager getWindowManager(Context context) {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        }
        return mWindowManager;
    }
}
