package com.crte.sipstackhome.ui.suspensionwindow;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by Administrator on 2016/1/22.
 */
public class SuspensionWindowService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        SuspensionWindowManager.createVideoWindow(getApplicationContext());
        return super.onStartCommand(intent, flags, startId);
    }
}
