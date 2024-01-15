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
        // Your background task code goes here
        performBackgroundTask();

        // Start the service in the foreground with a notification
        startForeground(NOTIFICATION_ID, createNotification());

        // Create a handler and a runnable for your loop
        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                // Your repeated task code goes here
                performRepeatedTask(MyBackgroundService.this);

                // Schedule the runnable to run again after a delay (e.g., every 5 seconds)
                handler.postDelayed(this, 5000); // 5000 milliseconds (5 seconds)
            }
        };

        // Start the initial run of the runnable
        handler.post(runnable);

        // Return START_STICKY to restart the service if it gets killed by the system
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Cleanup or release resources if needed
        // Remove callbacks to avoid memory leaks
        if (handler != null) {
            handler.removeCallbacks(runnable);
        }
    }

    private void performBackgroundTask() {
        // Implement your background task logic here
        // This is where you call the methods that you want to be initialized in the service
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

        // Only show the notification if the app is not in the foreground
        if (!myApplication.isAppInForeground()) {
            // Create an explicit intent for launching an activity when the notification is clicked
            Intent intent = new Intent(context, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            // Create the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.pause) // Replace with your own small icon
                    .setContentTitle("New Message")
                    .setContentText(content)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true); // Dismiss the notification when clicked

            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(NOTIFICATION_ID, builder.build());
        }
    }

    private Notification createNotification() {
        // Create a notification channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "PSQLChat New MSG",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

        // Create a notification for the foreground service
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        return builder.build();
    }
}



