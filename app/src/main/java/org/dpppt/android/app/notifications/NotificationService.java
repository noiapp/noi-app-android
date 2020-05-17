package org.dpppt.android.app.notifications;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import org.dpppt.android.app.R;
import org.dpppt.android.app.util.DebugUtils;


public class NotificationService {

    public static final String NOTIFICATION_CHANNEL_ID = "contact-channel";
    public static final int NOTIFICATION_ID = 42;

    private Context context;

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationService(Context context)
    {
        this.context = context;
        createNotificationChannel();
    }

    public void cancelNotification(int notificationId) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        assert notificationManager != null;
        DebugUtils.logDebug("Cancel notification with id: "+notificationId);

        notificationManager.cancel(notificationId);
    }

    public void pushNotification(String title, String text, int idNotify)
    {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        assert notificationManager != null;

        if (alreadyPushNotificationId(notificationManager.getActiveNotifications(), NOTIFICATION_ID)) {
            return;
        }

        Toast.makeText(context,"Push notification",Toast.LENGTH_LONG).show();
        DebugUtils.logDebug("Push notification job");


        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        PendingIntent contentIntent = null;
        if (launchIntent != null) {
            contentIntent =
                    PendingIntent.getActivity(context, 0, launchIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        }
        Notification notification =
                new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID)
                        .setContentTitle(title)
                        .setContentText(text)
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setSmallIcon(R.drawable.ic_begegnungen)
                        .setContentIntent(contentIntent)
                        .build();

        notificationManager.notify(idNotify, notification);

    }

    public boolean alreadyPushNotificationId(StatusBarNotification[] StatusBarNotifications, int notificationId) {
        for(StatusBarNotification notification: StatusBarNotifications) {
            if (notification.getId() == notificationId) {
                return true;
            }
        }
        return false;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String channelName = context.getString(R.string.app_name);
        NotificationChannel channel =
                new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }
}
