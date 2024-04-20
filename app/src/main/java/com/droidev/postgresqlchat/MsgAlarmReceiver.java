package com.droidev.postgresqlchat;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MsgAlarmReceiver extends BroadcastReceiver {
    @SuppressLint("StaticFieldLeak")
    @Override
    public void onReceive(Context context, Intent intent) {

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

                        MsgNotification.showNotification(context, tinyDB.getString("identifyName"), messageBuilder.toString());
                    }
                }
            });
        }
    }
}
