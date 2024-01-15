package com.droidev.postgresqlchat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Arrays;
import java.util.List;

public class MyBackgroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PSQLChat New MSG";

    private Handler handler;
    private Runnable runnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(NOTIFICATION_ID, createNotification());

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                performRepeatedTask(MyBackgroundService.this);

                handler.postDelayed(this, 5000);
            }
        };

        handler.post(runnable);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void performRepeatedTask(Context context) {
        dbQueries db = new dbQueries();

        db.loadLastMsg(context, chat -> {
            TinyDB tinyDB = new TinyDB(context);

            List<String> chatList = Arrays.asList(chat.split("@"));

            if (!tinyDB.getString("lastMSG").equals(chatList.get(0))) {

                showNotification(context, chatList.get(1));

                tinyDB.putString("lastMSG", chatList.get(0));
            }
        });
    }

    private void showNotification(Context context, String content) {
        MyApplication myApplication = (MyApplication) context.getApplicationContext();

        if (!myApplication.isAppInForeground()) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("New Message")
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private Notification createNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PSQLChat New MSG",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
}



