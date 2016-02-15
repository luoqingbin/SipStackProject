package com.crte.sipstackhome.ui.suspensionwindow;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.models.CallState;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.ui.video.VideoActivity;

import org.webrtc.videoengine.ViERenderer;

import java.lang.reflect.Field;

/**
 * 悬浮的视频采集窗口
 * Created by Administrator on 2016/1/22.
 */
public class VideoSuspensionWindow extends LinearLayout {
    private static final String TAG = "VideoSuspensionWindow";

    /**
     * 记录小悬浮窗的宽度
     */
    public static int mViewWidth;

    /**
     * 记录小悬浮窗的高度
     */
    public static int mViewHeight;

    /**
     * 记录当前手指位置在屏幕上的横坐标值
     */
    private float xInScreen;

    /**
     * 记录当前手指位置在屏幕上的纵坐标值
     */
    private float yInScreen;

    /**
     * 记录手指按下时在屏幕上的横坐标的值
     */
    private float xDownInScreen;

    /**
     * 记录手指按下时在屏幕上的纵坐标的值
     */
    private float yDownInScreen;

    /**
     * 记录手指按下时在小悬浮窗的View上的横坐标的值
     */
    private float xInView;

    /**
     * 记录手指按下时在小悬浮窗的View上的纵坐标的值
     */
    private float yInView;

    /**
     * 记录系统状态栏的高度
     */
    private static int statusBarHeight;

    private WindowManager.LayoutParams mParams;

    private WindowManager mWindowManager;

    private SurfaceView mCameraPreview;

    private Context mContext;

    public VideoSuspensionWindow(final Context context) {
        super(context);
        mContext = context;
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        LayoutInflater.from(context).inflate(R.layout.window_suspension, this);
        View view = findViewById(R.id.window_layout);
        mViewWidth = view.getLayoutParams().width;
        mViewHeight = view.getLayoutParams().height;

        mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
        mCameraPreview = ViERenderer.CreateLocalRenderer(mCameraPreview);
        UserAgentBroadcastReceiver.setVideoAndroidCapturer(mCameraPreview);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                // 手指按下时记录必要数据,纵坐标的值都需要减去状态栏高度
                xInView = event.getX();
                yInView = event.getY();
                xDownInScreen = event.getRawX();
                yDownInScreen = event.getRawY() - getStatusBarHeight();
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                break;
            case MotionEvent.ACTION_MOVE:
                xInScreen = event.getRawX();
                yInScreen = event.getRawY() - getStatusBarHeight();
                // 手指移动的时候更新小悬浮窗的位置
                updateViewPosition();
                break;
            case MotionEvent.ACTION_UP:
                // 如果手指离开屏幕时，xDownInScreen和xInScreen相等，且yDownInScreen和yInScreen相等，则视为触发了单击事件。
                if (xDownInScreen == xInScreen && yDownInScreen == yInScreen) {
                    mContext.startActivity(intentVideoTo(mContext, new CallState()));
                }
                break;
            default:
                break;
        }
        return true;
    }

    public Intent intentVideoTo(Context context, CallState callState) {
        return buildIntent(context, callState);
    }

    public Intent buildIntent(Context context, CallState callState) {
        CallState cState;
        if (callState == null) {
            cState = new CallState();
        } else {
            cState = callState;
        }

        Intent intent = new Intent(context, VideoActivity.class);
        intent.putExtra(VideoActivity.VIDEO_CALL_STATE, cState);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    /**
     * 将小悬浮窗的参数传入，用于更新小悬浮窗的位置。
     *
     * @param params 小悬浮窗的参数
     */
    public void setParams(WindowManager.LayoutParams params) {
        mParams = params;
    }

    /**
     * 用于获取状态栏的高度。
     *
     * @return 返回状态栏高度的像素值。
     */
    private int getStatusBarHeight() {
        if (statusBarHeight == 0) {
            try {
                Class<?> c = Class.forName("com.android.internal.R$dimen");
                Object o = c.newInstance();
                Field field = c.getField("status_bar_height");
                int x = (Integer) field.get(o);
                statusBarHeight = getResources().getDimensionPixelSize(x);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return statusBarHeight;
    }

    /**
     * 更新小悬浮窗在屏幕中的位置。
     */
    private void updateViewPosition() {
        mParams.x = (int) (xInScreen - xInView);
        mParams.y = (int) (yInScreen - yInView);
        mWindowManager.updateViewLayout(this, mParams);
    }

}