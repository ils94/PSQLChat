package com.droidev.postgresqlchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;

import androidx.core.app.NotificationCompat;

public class MsgNotification {

    public static void showNotification(Context context, String title, String msgs) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel(
                "psqlchat_channel",
                "PSQLChat Channel",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "psqlchat_channel")
                .setContentTitle(title)
                .setContentText(msgs)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        builder.setStyle(bigTextStyle);

        notificationManager.notify(1, builder.build());
    }
}
