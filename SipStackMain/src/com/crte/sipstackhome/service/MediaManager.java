package com.crte.sipstackhome.service;

import android.content.Context;
import android.media.AudioManager;

import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.utils.Ringer;
import com.crte.sipstackhome.utils.log.LogUtils;

/**
 * Created by Administrator on 2016/1/12.
 */
public class MediaManager {
    private static final String TAG = "MediaManager";

    private UserAgentBroadcastReceiver mUserAgentBroadcastReceiver;
    private SipService mSipService;

    /**
     * 铃声管理类
     */
    private Ringer mRinger;
    private AudioManager mAudioManager;

    // 使用扬声器
    private boolean userWantSpeaker = false;
    // 麦克风静音
    private boolean userWantMicrophoneMute = false;

    public MediaManager(UserAgentBroadcastReceiver userAgentBroadcastReceiver) {
        this.mUserAgentBroadcastReceiver = userAgentBroadcastReceiver;
        this.mSipService = mUserAgentBroadcastReceiver.mSipService;
        mAudioManager = (AudioManager) mSipService.getSystemService(Context.AUDIO_SERVICE);
        mRinger = new Ringer(mSipService);
    }

    public synchronized void setSpeakerOn() throws SameThreadException {
        if (mAudioManager != null) {
            mUserAgentBroadcastReceiver.setNoSnd();
            mUserAgentBroadcastReceiver.setSnd();
            mAudioManager.setSpeakerphoneOn(userWantSpeaker = !userWantSpeaker);

            mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    public synchronized void setSpeakerOn(boolean speaker) throws SameThreadException {
        if (mAudioManager != null) {
            mUserAgentBroadcastReceiver.setNoSnd();
            mUserAgentBroadcastReceiver.setSnd();

            if (mAudioManager.isSpeakerphoneOn()) {
                mAudioManager.setSpeakerphoneOn(speaker);
                userWantSpeaker = false;
                mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                        mAudioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                        AudioManager.STREAM_VOICE_CALL);
            }
        }
    }

    public synchronized void setMuteOn() throws SameThreadException {
        mUserAgentBroadcastReceiver.confAdjustTxLevel(0, 1.0f);
        mUserAgentBroadcastReceiver.confAdjustRxLevel(0, (userWantMicrophoneMute = !userWantMicrophoneMute) ? 0 : 1.0f);
    }

    /**
     * 开启铃声
     *
     * @param remoteContact
     */
    public synchronized void startRing(String remoteContact) {
        if (!mRinger.isRinging()) {
            // 播放默认铃声
            mRinger.ring(remoteContact, "content://settings/system/ringtone");
        } else {
            LogUtils.d(TAG, "铃声已响....");
        }
    }

    public synchronized void stopRing() {
        if (mRinger.isRinging()) {
            mRinger.stopRing();
        }
    }
}
