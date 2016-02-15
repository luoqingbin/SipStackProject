/**
 * Copyright (C) 2010-2012 Regis Montoya (aka r3gis - www.r3gis.fr)
 * This file is part of CSipSimple.
 * <p/>
 * CSipSimple is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * If you own a pjsip commercial license you can also redistribute it
 * and/or modify it under the terms of the GNU Lesser General Public License
 * as an android library.
 * <p/>
 * CSipSimple is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with CSipSimple.  If not, see <http://www.gnu.org/licenses/>.
 * <p/>
 * This file contains relicensed code from Apache copyright of
 * Copyright (C) 2006 The Android Open Source Project
 * <p/>
 * This file contains relicensed code from Apache copyright of
 * Copyright (C) 2006 The Android Open Source Project
 * <p/>
 * This file contains relicensed code from Apache copyright of
 * Copyright (C) 2006 The Android Open Source Project
 */
/**
 * This file contains relicensed code from Apache copyright of 
 * Copyright (C) 2006 The Android Open Source Project
 */

package com.crte.sipstackhome.utils;

import android.content.Context;
import android.media.AudioManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.Vibrator;

import com.crte.sipstackhome.models.CallerInfo;
import com.crte.sipstackhome.utils.log.Log;
import com.crte.sipstackhome.utils.log.LogUtils;

/**
 * 管理手机APP的铃声
 */
public class Ringer {
    private static final String THIS_FILE = "Ringer";
    /**
     * 振动时长 ms
     */
    private static final int VIBRATE_LENGTH = 1000;
    /**
     * 停顿时长 ms
     */
    private static final int PAUSE_LENGTH = 1000;

    // Uri for the ringtone.
    Uri customRingtoneUri;

    /**
     * 手机振动处理
     */
    Vibrator vibrator;

    VibratorThread vibratorThread;
    HandlerThread ringerThread;
    Context context;

    private RingWorkerHandler ringerWorker;

    public Ringer(Context aContext) {
        context = aContext;
        // 获得振动器
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        // 创建线程以及Looper对象
        ringerThread = new HandlerThread("RingerThread");
        ringerThread.start();
        ringerWorker = new RingWorkerHandler(ringerThread.getLooper());
    }

    /**
     * 启动铃声和/或振动
     */
    public void ring(String remoteContact, String defaultRingtone) {
        LogUtils.d(THIS_FILE, "==> ring() called...");

        synchronized (this) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

            // 获得执行联系人的默认铃声
//            Ringtone ringtone = getRingtone(remoteContact, defaultRingtone);

            /* 目前先设置默认铃声（只支持默认铃声） */
            Uri ringtoneUri = Uri.parse(defaultRingtone);
            Ringtone ringtone = RingtoneManager.getRingtone(context, ringtoneUri);

            ringerWorker.setRingtone(ringtone); // 设置铃声

            // 没有铃声没有振动
            int ringerMode = audioManager.getRingerMode(); // 获得铃声模式
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                LogUtils.d(THIS_FILE, "跳过循环和振动，应为配置文件是关闭的");
                return;
            }

            // 振动
            int vibrateSetting = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
            LogUtils.d(THIS_FILE, "v=" + vibrateSetting + " rm=" + ringerMode);
            if (vibratorThread == null && (vibrateSetting == AudioManager.VIBRATE_SETTING_ON || ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
                vibratorThread = new VibratorThread();
                LogUtils.d(THIS_FILE, "开启振动...");
                vibratorThread.start();
            }

            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
                LogUtils.d(THIS_FILE, "音量是0，振动");
                return;
            }

            if (ringtone == null) {
                LogUtils.d(THIS_FILE, "没有铃声");
                return;
            }

            // 开启铃声
            ringerWorker.startRinging(audioManager);
        }
    }

    /**
     * @return true if we're playing a ringtone and/or vibrating
     *     to indicate that there's an incoming call.
     *     ("Ringing" here is used in the general sense.  If you literally
     *     need to know if we're playing a ringtone or vibrating, use
     *     isRingtonePlaying() or isVibrating() instead.)
     */
    public boolean isRinging() {
        return (!ringerWorker.isStopped() || vibratorThread != null);
    }

    /**
     * Stops the ringtone and/or vibrator if any of these are actually
     * ringing/vibrating.
     */
    public void stopRing() {
        synchronized (this) {
            Log.d(THIS_FILE, "==> stopRing() called...");
            stopVibrator();
            stopRinger();
        }
    }


    private void stopRinger() {
        ringerWorker.askStop();
    }

    private void stopVibrator() {

        if (vibratorThread != null) {
            vibratorThread.interrupt();
            try {
                vibratorThread.join(250); // Should be plenty long (typ.)
            } catch (InterruptedException e) {
            } // Best efforts (typ.)
            vibratorThread = null;
        }
    }

    public void updateRingerMode() {

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        synchronized (this) {
            int ringerMode = audioManager.getRingerMode();
            // Silent : stop everything
            if (ringerMode == AudioManager.RINGER_MODE_SILENT) {
                stopRing();
                return;
            }

            // Vibrate
            int vibrateSetting = audioManager.getVibrateSetting(AudioManager.VIBRATE_TYPE_RINGER);
            // If not already started restart it
            if (vibratorThread == null && (vibrateSetting == AudioManager.VIBRATE_SETTING_ON || ringerMode == AudioManager.RINGER_MODE_VIBRATE)) {
                vibratorThread = new VibratorThread();
                vibratorThread.start();
            }

            // Vibrate only
            if (ringerMode == AudioManager.RINGER_MODE_VIBRATE || audioManager.getStreamVolume(AudioManager.STREAM_RING) == 0) {
                stopRinger();
                return;
            }

            //Ringer
            ringerWorker.startRinging(audioManager);
        }
    }

    private class VibratorThread extends Thread {
        public void run() {
            try {
                while (true) {
                    vibrator.vibrate(VIBRATE_LENGTH);
                    Thread.sleep(VIBRATE_LENGTH + PAUSE_LENGTH);
                }
            } catch (InterruptedException ex) {
                Log.d(THIS_FILE, "Vibrator thread interrupt");
            } finally {
                vibrator.cancel();
            }
            Log.d(THIS_FILE, "Vibrator thread exiting");
        }
    }

    /**
     * 这个线程处理铃声的播放任务
     */
    private class RingWorkerHandler extends Handler {
        public static final int PROGRESS_RING = 0;
        private Boolean askedStopped = false; // 要求停止
        private Ringtone ringtone = null;

        public RingWorkerHandler(Looper looper) {
            super(looper);
        }

        /**
         * 开启振铃
         * @param audioManager
         */
        public void startRinging(AudioManager audioManager) {
            if (ringtone != null) {
                LogUtils.d(THIS_FILE, "Starting ring with " + ringtone.getTitle(context));
                Message msg = ringerWorker.obtainMessage(RingWorkerHandler.PROGRESS_RING);
                msg.arg1 = RingWorkerHandler.PROGRESS_RING;
                LogUtils.d(THIS_FILE, "Starting ringer...");
                audioManager.setMode(AudioManager.MODE_RINGTONE); // 振铃模式
                ringerWorker.sendMessage(msg);
            }
        }

        /**
         * 手动设置铃声
         * @param ringtone
         */
        public synchronized void setRingtone(Ringtone ringtone) {
            if (this.ringtone != null) {
                this.ringtone.stop();
            }
            this.ringtone = ringtone;
            askedStopped = false;
        }

        public synchronized void askStop() {
            askedStopped = true;
        }

        public synchronized boolean isStopped() {
            return askedStopped || (ringtone == null);
        }

        public void handleMessage(Message msg) {
            if (ringtone == null) {
                return;
            }
            if (msg.arg1 == PROGRESS_RING) {
                synchronized (askedStopped) {
                    if (askedStopped) {
                        ringtone.stop();
                        ringtone = null;
                        return;
                    }
                }

                if (!ringtone.isPlaying()) {
                    ringtone.play();
                }

                Message msgBis = ringerWorker.obtainMessage(RingWorkerHandler.PROGRESS_RING);
                msg.arg1 = RingWorkerHandler.PROGRESS_RING;
                ringerWorker.sendMessageDelayed(msgBis, 100);
            }
        }
    }

    /**
     * 获得指定用户的铃声
     * @param remoteContact 远程联系人
     * @param defaultRingtone 默认的铃声
     * @return
     */
    private Ringtone getRingtone(String remoteContact, String defaultRingtone) {
        Uri ringtoneUri = Uri.parse(defaultRingtone);

        // TODO - 如果这是在一个单独的线程？我们仍然要等待
        CallerInfo callerInfo = CallerInfo.getCallerInfoFromSipUri(context, remoteContact);

        if (callerInfo != null && callerInfo.contactExists && callerInfo.contactRingtoneUri != null) {
            Log.d(THIS_FILE, "Found ringtone for " + callerInfo.name);
            ringtoneUri = callerInfo.contactRingtoneUri;
        }

        return RingtoneManager.getRingtone(context, ringtoneUri);
    }
}
