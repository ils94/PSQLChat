package com.droidev.postgresqlchat;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MyBackgroundService extends Service {

    private static final int NOTIFICATION_ID = 1;
    private static final String CHANNEL_ID = "PSQLChat Notifications";
    private static boolean isServiceRunning = false;

    private Handler handler;
    private Runnable runnable;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!isServiceRunning) {
            isServiceRunning = true;

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
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        isServiceRunning = false;

        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void performRepeatedTask(Context context) {

        if (NetworkUtils.isNetworkAvailable(context)) {

            dbQueries db = new dbQueries();

            MyApplication myApplication = (MyApplication) context.getApplicationContext();

            db.loadLastMsg(context, chat -> {

                TinyDB tinyDB = new TinyDB(context);

                ArrayList<String> chatArrayList = tinyDB.getListString("receivedMSGs");

                List<String> chatList = Arrays.asList(chat.split("@"));

                if (chatList.size() >= 2
                        && !tinyDB.getString("lastMSG").equals(chatList.get(0))
                        && !chatList.get(1).contains(tinyDB.getString("user"))) {

                    tinyDB.putString("lastMSG", chatList.get(0));

                    if (!myApplication.isAppInForeground()) {

                        chatArrayList.add(chatList.get(1));

                        int maxSize = Math.min(chatArrayList.size(), 10);
                        List<String> last10Messages = new ArrayList<>(chatArrayList.subList(chatArrayList.size() - maxSize, chatArrayList.size()));

                        Collections.reverse(last10Messages);

                        tinyDB.putListString("receivedMSGs", chatArrayList);

                        StringBuilder messageBuilder = new StringBuilder();
                        for (String message : last10Messages) {
                            messageBuilder.append(message).append("\n");
                        }

                        showNotification(context, tinyDB.getString("identifyName"), messageBuilder.toString());
                    }
                }
            });
        }
    }


    private void showNotification(Context context, String title, String content) {
        MyApplication myApplication = (MyApplication) context.getApplicationContext();

        if (!myApplication.isAppInForeground()) {
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(title)
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationCompat.BigTextStyle bigTextStyle = new NotificationCompat.BigTextStyle();
            bigTextStyle.bigText(content);
            builder.setStyle(bigTextStyle);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }


    private Notification createNotification() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "PSQLChat Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
        );
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        notificationManager.createNotificationChannel(channel);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());

        PendingIntent restartServicePendingIntent =
                PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmService =
                (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        assert alarmService != null;
        alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, restartServicePendingIntent);

        super.onTaskRemoved(rootIntent);
    }
}