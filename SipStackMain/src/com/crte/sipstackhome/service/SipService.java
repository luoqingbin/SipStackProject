package com.crte.sipstackhome.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;

import com.crte.sipstackhome.api.SipConfigManager;
import com.crte.sipstackhome.api.SipManager;
import com.crte.sipstackhome.exception.SameThreadException;
import com.crte.sipstackhome.models.CallState;
import com.crte.sipstackhome.pjsip.UserAgentBroadcastReceiver;
import com.crte.sipstackhome.ui.MainActivity;
import com.crte.sipstackhome.ui.login.AccountNotification;
import com.crte.sipstackhome.ui.login.LoginActivity;
import com.crte.sipstackhome.ui.preferences.PreferencesWrapper;
import com.crte.sipstackhome.ui.video.VideoActivity;
import com.crte.sipstackhome.utils.log.Log;
import com.crte.sipstackhome.utils.log.LogUtils;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;

public class SipService extends Service {
    private static final String TAG = "SipService";

    private SipWakeLock mSipWakeLock;
    public SipWakeLock sipWakeLock;

    PreferencesWrapper mPreferencesWrapper;

    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.i(TAG, "SipService onCreate");
        mSipWakeLock = new SipWakeLock((PowerManager) getSystemService(Context.POWER_SERVICE));
        sipWakeLock = new SipWakeLock((PowerManager) getSystemService(Context.POWER_SERVICE));

        mPreferencesWrapper = new PreferencesWrapper(this);

        registerServiceBroadcasts();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    UserAgentBroadcastReceiver mUserAgentBroadcastReceiver;

    private WifiManager.WifiLock mWifiLock;

    /**
     * 注册广播<br/>
     * ACTION_DEFER_OUTGOING_UNREGISTER 当呼叫等操作时，延后<br/>
     * ACTION_OUTGOING_UNREGISTER 注销广播<br/>
     */
    private void registerServiceBroadcasts() {
        if (mUserAgentBroadcastReceiver == null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(SipManager.ACTION_SIP_ACCOUNT_CHANGED); // 账户发生变化
            intentFilter.addAction(SipManager.ACTION_SIP_STACK_CHANGE); // 当SIP协议栈发生变化时
            intentFilter.addAction(SipManager.ACTION_FINISH);
            intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION); // 当网络状态发生变化时
            intentFilter.addAction(Intent.ACTION_BOOT_COMPLETED);
            intentFilter.addAction(Intent.ACTION_HEADSET_PLUG);
            intentFilter.addAction(Intent.ACTION_SCREEN_ON);
            intentFilter.addAction(Intent.ACTION_USER_PRESENT);
            intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
            intentFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);

            mUserAgentBroadcastReceiver = new UserAgentBroadcastReceiver(this);
            registerReceiver(mUserAgentBroadcastReceiver, intentFilter);

            getExecutor().execute(new SafeStartRunnable());
        }
    }

    public synchronized void acquireResources() {
        if (mUserAgentBroadcastReceiver.mPreferencesWrapper.getPreferenceBooleanValue(SipConfigManager.LOCK_WIFI)) {
            WifiManager wman = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            if (mWifiLock == null) {
                int mode = WifiManager.WIFI_MODE_FULL;

                mWifiLock = wman.createWifiLock(mode, "com.sipstackhome.SipService");
                mWifiLock.setReferenceCounted(false);

                WifiInfo winfo = wman.getConnectionInfo();
                if (winfo != null) {
                    NetworkInfo.DetailedState dstate = WifiInfo.getDetailedStateOf(winfo.getSupplicantState());
                    if (dstate == NetworkInfo.DetailedState.OBTAINING_IPADDR || dstate == NetworkInfo.DetailedState.CONNECTED) {
                        if (!mWifiLock.isHeld()) {
                            LogUtils.d(TAG, "开启WiFi锁");
                            mWifiLock.acquire();
                        }
                    }
                }
            }
        }
    }

    /**
     * 释放锁资源
     * <li/>WiFi
     */
    private synchronized void releaseResources() {
        if (mWifiLock != null && mWifiLock.isHeld()) {
            mWifiLock.release();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LogUtils.d(TAG, "销毁服务");
    }

    public void intentVideo(Context context, CallState callState) {
        Intent intent = buildIntent(context, callState);
        startActivity(intent);
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

    public PreferencesWrapper getmPreferencesWrapper() {
        return mPreferencesWrapper;
    }

    private static HandlerThread executorThread;
    private SipServiceExecutor mExecutor;

    public SipServiceExecutor getExecutor() {
        // create mExecutor lazily
        if (mExecutor == null) {
            mExecutor = new SipServiceExecutor(this);
        }
        return mExecutor;
    }

    public static class SipServiceExecutor extends Handler {
        WeakReference<SipService> handlerService;

        SipServiceExecutor(SipService s) {
            super(createLooper());
            handlerService = new WeakReference<SipService>(s);
        }

        public void execute(Runnable task) {
            SipService s = handlerService.get();
            if (s != null) {
                s.sipWakeLock.acquire(task);
            }
            Message.obtain(this, 0/* don't care */, task).sendToTarget();
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.obj instanceof Runnable) {
                executeInternal((Runnable) msg.obj);
            } else {
                Log.w(TAG, "can't handle msg: " + msg);
            }
        }

        private void executeInternal(Runnable task) {
            try {
                task.run();
            } catch (Throwable t) {
                Log.e(TAG, "run task: " + task, t);
            } finally {

                SipService s = handlerService.get();
                if (s != null) {
                    s.sipWakeLock.release(task);
                }
            }
        }
    }

    private static Looper createLooper() {
        // synchronized (executorThread) {
        if (executorThread == null) {
            Log.d(TAG, "Creating new handler thread");
            // ADT gives a fake warning due to bad parse rule.
            executorThread = new HandlerThread("SipService.Executor");
            executorThread.start();
        }
        // }
        return executorThread.getLooper();
    }

    public abstract static class SipRunnable implements Runnable {
        protected abstract void doRun() throws SameThreadException;

        public void run() {
            try {
                doRun();
            } catch (SameThreadException e) {
                Log.e(TAG, "Not done from same thread");
            }
        }
    }

    public abstract class ReturnRunnable extends SipRunnable {
        private Semaphore runSemaphore;
        private Object resultObject;

        public ReturnRunnable() {
            super();
            runSemaphore = new Semaphore(0);
        }

        public Object getResult() {
            try {
                runSemaphore.acquire();
            } catch (InterruptedException e) {
                Log.e(TAG, "Can't acquire run semaphore... problem...");
            }
            return resultObject;
        }

        protected abstract Object runWithReturn() throws SameThreadException;

        @Override
        public void doRun() throws SameThreadException {
            setResult(runWithReturn());
        }

        private void setResult(Object obj) {
            resultObject = obj;
            runSemaphore.release();
        }
    }

    /*
     * Sip协议模式
     * <b>
     * <li/> 正常开启（添加Lib、开启Sip、开启WiFi锁、注册）
     * <li/> 开启（开启Sip、注册）
     * <li/> 关闭（关闭Sip）
     * <li/> 重启注册（关闭Sip、开启Sip）
     * <li/> 重启注册（关闭Sip、开启Sip、注册）
     * <li/> 正常关闭（关闭Sip、关闭WiFi锁）
     * </b>
     */

    // 正常开启
    public class SafeStartRunnable extends SipRunnable {

        @Override
        protected void doRun() throws SameThreadException {
            LogUtils.d(TAG, "安全启动...");
            if (mUserAgentBroadcastReceiver != null) {
                mUserAgentBroadcastReceiver.loadLibrary();
                mUserAgentBroadcastReceiver.startSipStack();
                mUserAgentBroadcastReceiver.retrievalAccountReg(SipService.this, LoginActivity.ACCONT_ID);
                acquireResources();
            }
        }
    }

    // 开启
    public class StartRunnable extends SipRunnable {
        @Override
        protected void doRun() throws SameThreadException {
            if (mUserAgentBroadcastReceiver != null) {
                mUserAgentBroadcastReceiver.startSipStack();
                mUserAgentBroadcastReceiver.retrievalAccountReg(SipService.this, LoginActivity.ACCONT_ID);
            }
        }
    }

    // 关闭
    public class CloseRunnable extends SipRunnable {
        @Override
        protected void doRun() throws SameThreadException {
            if (mUserAgentBroadcastReceiver != null) {
                mUserAgentBroadcastReceiver.stopSipStack();
            }
        }
    }

    // 重启 支持注册
    public class RestartRunnable extends SipRunnable {
        private boolean isRegister;

        public RestartRunnable(boolean isRegister) {
            this.isRegister = isRegister;
        }

        @Override
        protected void doRun() throws SameThreadException {
            restartSipStack(isRegister);
        }
    }

    public synchronized boolean restartSipStack(boolean isReister) {
        if (mUserAgentBroadcastReceiver != null) {
            if (mUserAgentBroadcastReceiver.stopSipStack()) {
                mUserAgentBroadcastReceiver.startSipStack();
                if (isReister) {
                    mUserAgentBroadcastReceiver.retrievalAccountReg(SipService.this, LoginActivity.ACCONT_ID);
                }
                return true;
            } else {
                Log.e(TAG, "停止协议栈出错！");
                return false;
            }
        }
        return false;
    }

    public synchronized boolean restartSipStack() {
        if (mUserAgentBroadcastReceiver != null) {
            if (mUserAgentBroadcastReceiver.stopSipStack()) {
                mUserAgentBroadcastReceiver.startSipStack();
                mUserAgentBroadcastReceiver.retrievalAccountReg(SipService.this, LoginActivity.ACCONT_ID);
                return true;
            } else {
                Log.e(TAG, "停止协议栈出错！");
                return false;
            }
        }
        return false;
    }

    public class SafeCloseRunnable extends SipRunnable {
        @Override
        protected void doRun() throws SameThreadException {
            LogUtils.d(TAG, "销毁----");
            if (mUserAgentBroadcastReceiver != null) {
                mUserAgentBroadcastReceiver.stopSipStack();
                releaseResources();
                if (mUserAgentBroadcastReceiver != null) {
                    unregisterReceiver(mUserAgentBroadcastReceiver);
                }

                AccountNotification.onUpdateNotification(SipService.this, "", 0, "", 0, true);

                Intent serviceIntent = new Intent(SipManager.INTENT_SIP_SERVICE);
                serviceIntent.setPackage(MainActivity.MAIN_PACKAGE);
                stopService(serviceIntent);
            }
        }
    }

    public void SafeCloseRunnable() {
        getExecutor().execute(new SafeCloseRunnable());
    }

    public void RestartRunnableAndRegister() {
        RestartRunnable();
    }

    public void RestartRunnable() {
        getExecutor().execute(new RestartRunnable(true));
    }
}