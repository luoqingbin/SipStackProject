package com.crte.sipstackhome.ui.video;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.api.SipCallSession;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.models.CallState;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.ui.suspensionwindow.SuspensionWindowManager;
import com.crte.sipstackhome.ui.suspensionwindow.SuspensionWindowService;
import com.crte.sipstackhome.utils.log.LogUtils;

import org.webrtc.videoengine.ViERenderer;

/**
 * 测试视频界面
 * 预计功能：
 * 静音   [完成]
 * 扬声器 [完成]
 * 挂断   [完成]
 */
public class VideoActivity extends Activity implements View.OnClickListener, SensorEventListener {
    private static final String TAG = "VideoActivity";
    public static final String VIDEO_CALL_STATE = "call_state";

    public static final String DISTANCE_LOCK = "com.crte.sipstackhome.distance.lock";
    public static final String VIDEO_CALL_LOCK = "com.crte.sipstackhome.videocall.lock";

    private SurfaceView mCameraPreview;
    private SurfaceView mRenderView;
    private ViewGroup mMainFrame;
    private Button mAnswer;
    private Button mHangup;
    private Button mMute;
    private Button mSpeaker;
    private TextView mCallState;

    // 传感器
    private PowerManager mPowerManager;
    private SensorManager mManager;
    private PowerManager.WakeLock mLocalWakeLock = null;
    private PowerManager.WakeLock mVideoWakeLock;

    private CallState mCallStateSave;

    private boolean muteState = true; // 静音状态
    private int callState = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initIntentValues(getIntent());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.activity_video);

        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mLocalWakeLock = mPowerManager.newWakeLock(32, DISTANCE_LOCK);

        mMainFrame = (ViewGroup) findViewById(R.id.video_relative);
        mAnswer = (Button) findViewById(R.id.answer);
        mHangup = (Button) findViewById(R.id.hangup);
        mMute = (Button) findViewById(R.id.mute);
        mSpeaker = (Button) findViewById(R.id.speaker);
        mCallState = (TextView) findViewById(R.id.call_state);
        mAnswer.setOnClickListener(this);
        mHangup.setOnClickListener(this);
        mMute.setOnClickListener(this);
        mSpeaker.setOnClickListener(this);

        takeKeyEvents(true);
        initAudioVideo();
    }

    @Override
    protected void onStart() {
        super.onStart();
//        if (mCallStateSave.stateId == SipCallSession.InvState.CONFIRMED && mManager != null) {
//            mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
//        }
    }

    public void initIntentValues(Intent intent) {
        mCallStateSave = intent.getParcelableExtra(VIDEO_CALL_STATE);
        if(mCallStateSave == null) {
            mCallStateSave = new CallState();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initIntentValues(intent);
        initAudioVideo();
    }

    public void initAudioVideo() {
        if (mCallStateSave.stateId == SipCallSession.InvState.CALLING) {
            setButtonShow(false, true, false, false);
        }

        if (mCallStateSave.stateId == SipCallSession.InvState.INCOMING || mCallStateSave.stateId == SipCallSession.InvState.EARLY) {
            setButtonShow(true, true, false, false);
        }

        if (mCallStateSave.stateId == SipCallSession.InvState.CONFIRMED) {
            mManager.registerListener(this, mManager.getDefaultSensor(Sensor.TYPE_PROXIMITY), SensorManager.SENSOR_DELAY_NORMAL);
            if (mCallStateSave.useVideo) {
                mCameraPreview = (SurfaceView) findViewById(R.id.camera_preview);
                mCameraPreview.setVisibility(View.VISIBLE);
                mCameraPreview = ViERenderer.CreateLocalRenderer(mCameraPreview);

                if (mVideoWakeLock == null) {
                    mVideoWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, VIDEO_CALL_LOCK);
                    mVideoWakeLock.setReferenceCounted(false);
                }
                if (mVideoWakeLock != null && mVideoWakeLock.isHeld()) {
                    mVideoWakeLock.release();
                }
                setVideoCall(mCallStateSave.callId);
                runOnUiThread(new UpdateVideoPreviewRunnable(true));

                Intent intent = new Intent(this, SuspensionWindowService.class);
                startService(intent);
            }

            mCallState.setVisibility(View.VISIBLE);
            setButtonShow(false, true, true, true);
        }

        if (mCallStateSave.stateId == SipCallSession.InvState.DISCONNECTED) {
            if (mCallStateSave.stateCode == SipCallSession.StatusCode.BUSY_HERE) {
                Toast.makeText(this, "对方正在通话中... [" + mCallStateSave.stateCode + "]", Toast.LENGTH_SHORT).show();
            } else if (mCallStateSave.stateCode == SipCallSession.StatusCode.TEMPORARILY_UNAVAILABLE) {
                Toast.makeText(this, "用户忙请稍后再拨", Toast.LENGTH_SHORT).show();
            } else {
                mCallState.setVisibility(View.GONE);
                setButtonShow(false, false, false, false);
                runOnUiThread(new UpdateVideoPreviewRunnable(false, mCallStateSave.callId));

                Intent publishIntent = new Intent(SipManager.ACTION_SIP_STACK_CHANGE);
                VideoActivity.this.sendBroadcast(publishIntent);

                SuspensionWindowManager.removeVideoWindow(this);
                Intent intent = new Intent(this, SuspensionWindowService.class);
                this.stopService(intent);
            }

            new Thread() {
                @Override
                public void run() {
                    super.run();
                    try {
                        UserAgentBroadcastReceiver.getUserAgent().mMediaManager.setSpeakerOn(false);
                        sleep(1500);
                        handler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (SameThreadException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
        }
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            finish();
        }
    };

    public void setButtonShow(boolean answer, boolean hangup, boolean mute, boolean speaker) {
        if (mAnswer != null && mHangup != null && mMute != null && mSpeaker != null) {
            mAnswer.setVisibility(answer ? View.VISIBLE : View.GONE);
            mHangup.setVisibility(hangup ? View.VISIBLE : View.GONE);
            mMute.setVisibility(mute ? View.VISIBLE : View.GONE);
            mSpeaker.setVisibility(speaker ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.answer: // 接听
                runOnUiThread(new UpdateCallRunnable(R.id.answer, mCallStateSave.callId));
                break;

            case R.id.hangup: // 挂断
                runOnUiThread(new UpdateCallRunnable(R.id.hangup, mCallStateSave.callId));
                break;

            case R.id.mute: // 静音
                runOnUiThread(new UpdateCallRunnable(R.id.mute));
                break;

            case R.id.speaker: // 设置扬声器
                runOnUiThread(new UpdateCallRunnable(R.id.speaker));
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] its = event.values;
        if (its != null && event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            // 贴近手机
            if (its[0] == 0.0) {
                if (mLocalWakeLock.isHeld()) {
                    return;
                } else {
                    mLocalWakeLock.acquire();
                }
                // 远离手机
            } else {
                if (mLocalWakeLock.isHeld()) {
                    return;
                } else {
                    mLocalWakeLock.setReferenceCounted(false);
                    mLocalWakeLock.release();
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class UpdateCallRunnable implements Runnable {
        private int mUpdateState;
        private int mCallId;

        public UpdateCallRunnable(int updateState, int callId) {
            this.mUpdateState = updateState;
            this.mCallId = callId;
        }

        public UpdateCallRunnable(int updateState) {
            this.mUpdateState = updateState;
        }

        @Override
        public void run() {
            try {
                switch (mUpdateState) {
                    case R.id.answer:
                        UserAgentBroadcastReceiver.getUserAgent().callAnswer(mCallId, SipCallSession.StatusCode.OK, true);
                        break;

                    case R.id.hangup:
                        UserAgentBroadcastReceiver.getUserAgent().callHangup(mCallId, SipCallSession.StatusCode.BUSY_HERE);
                        break;

                    case R.id.mute:
                        UserAgentBroadcastReceiver.getUserAgent().mMediaManager.setMuteOn();
                        break;

                    case R.id.speaker:
                        UserAgentBroadcastReceiver.getUserAgent().mMediaManager.setSpeakerOn();
                        break;

                    case -1:
                        UserAgentBroadcastReceiver.getUserAgent().mMediaManager.setSpeakerOn(false);
                        Thread.sleep(1100);
                        break;
                }
            } catch (SameThreadException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 显示视频
     */
    private class UpdateVideoPreviewRunnable implements Runnable {
        private boolean mVideoState;
        private int mCallId;

        public UpdateVideoPreviewRunnable(boolean videoState) {
            this.mVideoState = videoState;
        }

        public UpdateVideoPreviewRunnable(boolean videoState, int callId) {
            this.mVideoState = videoState;
            this.mCallId = callId;
        }

        @Override
        public void run() {
            LogUtils.d(TAG, "开启捕获布局资源：" + mVideoState);
            if (mVideoState) {
                if (mVideoWakeLock != null) {
                    mVideoWakeLock.acquire();
                }
            } else {
                if (mVideoWakeLock != null && mVideoWakeLock.isHeld()) {
                    mVideoWakeLock.release();
                }
                UserAgentBroadcastReceiver.setVideoAndroidRenderer(mCallId, mRenderView);
            }
        }
    }

    public void setVideoCall(int call_id) {
        LogUtils.d(TAG, "设置渲染布局");
        mRenderView = ViERenderer.CreateRenderer(this, true);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.ALIGN_RIGHT, RelativeLayout.TRUE);
        lp.addRule(RelativeLayout.ALIGN_TOP, RelativeLayout.TRUE);
        mMainFrame.addView(mRenderView, lp);
        UserAgentBroadcastReceiver.setVideoAndroidRenderer(call_id, mRenderView);
    }

    private void detachVideoPreview() {
        if (mMainFrame != null && mCameraPreview != null) {
            mMainFrame.removeView(mCameraPreview);
        }
        if (mManager != null) {
            mManager.unregisterListener(this); //注销传感器监听
        }
        if (mLocalWakeLock != null && mLocalWakeLock.isHeld()) {
            mLocalWakeLock.release();
        }

        if (mVideoWakeLock != null && mVideoWakeLock.isHeld()) {
            mVideoWakeLock.release();
        }
        if (mCameraPreview != null) {
            mCameraPreview = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_HOME) {
            moveTaskToBack(true);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

//        if(mManager != null) {
//            mManager.unregisterListener(this); //注销传感器监听
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        detachVideoPreview();
    }
}