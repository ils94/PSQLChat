package com.droidev.postgresqlchat;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

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

        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "psqlchat_channel")
                .setContentTitle(title)
                .setContentText(msgs)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();

        builder.setStyle(bigTextStyle);

        notificationManager.notify(1, builder.build());
    }
}
