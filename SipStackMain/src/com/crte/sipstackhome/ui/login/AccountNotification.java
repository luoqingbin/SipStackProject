package com.crte.sipstackhome.ui.login;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.crte.sipstackhome.R;
import com.crte.sipstackhome.models.CallState;
import com.crte.sipstackhome.service.SipService;
import com.crte.sipstackhome.ui.MainActivity;
import com.crte.sipstackhome.ui.video.VideoActivity;

/**
 * 更新Notification显示状态
 * Created by Administrator on 2015/12/29.
 */
public class AccountNotification {
    public static final int REGISTER_NOTIFICATION = 1;
    public static final int MESSAGE_NOTIFICATION = 2;
    public static final int CALL_NOTIFICATION = 3;

    private static final String TAG = "AccountNotification";

    private static CallState mAccCallState;

    public static void onUpdateNotification(SipService sipService, String accId, int stateCode, String stateMessage, int notificationType) {
        onUpdateNotification(sipService, accId, stateCode, stateMessage, notificationType, false);
    }

    public static void onUpdateNotification(SipService sipService, String accId, int stateCode, String stateMessage, int notificationType, CallState accCallState) {
        mAccCallState = accCallState;
        onUpdateNotification(sipService, accId, stateCode, stateMessage, notificationType, false);
    }

    /**
     * 显示当前用户的注册状态
     *
     * @param sipService   上下文
     * @param accId        用户ID
     * @param stateCode    成功失败的状态类型
     * @param stateMessage 状态消息
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void onUpdateNotification(SipService sipService, String accId, int stateCode, String stateMessage, int notificationType, boolean cancel) {
        int notType = notificationType;

        NotificationManager manager = (NotificationManager) sipService.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification.Builder build11 = new Notification.Builder(sipService);

        build11.setSmallIcon(R.drawable.ic_launcher);

        switch (notType) {
            case REGISTER_NOTIFICATION:
                build11.setContentTitle(accId + " - " + stateCode);
                build11.setContentText(stateMessage);
                build11.setAutoCancel(false);
                build11.setOngoing(true);
                build11.setContentIntent(PendingIntent.getActivity(sipService, 0, createIntent(sipService, MainActivity.class), 0));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    build11.setColor(sipService.getResources().getColor(R.color.material_deep_teal_200)); // 背景颜色
                }

                break;
            case MESSAGE_NOTIFICATION:
                build11.setSmallIcon(R.drawable.ic_email_white);
                build11.setContentTitle(accId + " - " + stateCode);
                build11.setContentText(stateMessage);
                build11.setAutoCancel(true);
                build11.setOngoing(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    build11.setColor(sipService.getResources().getColor(R.color.blue_300)); // 背景颜色
                }
                build11.setDefaults(Notification.DEFAULT_SOUND);
                break;

            case CALL_NOTIFICATION:
                build11.setSmallIcon(R.drawable.ic_call_white);
                build11.setContentTitle(accId + " - " + stateCode);
                build11.setContentText(stateMessage);
                build11.setAutoCancel(false);
                build11.setOngoing(true);

                Intent intent = new Intent(sipService, VideoActivity.class);
                intent.putExtra(VideoActivity.VIDEO_CALL_STATE, new CallState());
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                build11.setContentIntent(PendingIntent.getActivity(sipService, 0, intent, 0));

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    build11.setColor(sipService.getResources().getColor(R.color.red_300)); // 背景颜色
                }
                break;
        }

        // 创建显示
        Notification notification = build11.getNotification();

        if (cancel) {
            manager.cancelAll();
        } else {
            manager.notify(notType, notification);
        }
    }

    public static Intent createIntent(Context context, Class<?> cls) {
        Intent startActivity = new Intent();
        startActivity.setClass(context, cls);
        startActivity.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        return startActivity;
    }
}
