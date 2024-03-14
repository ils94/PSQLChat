package com.droidev.postgresqlchat;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class dbQueries {

    public Connection connectDB(Context context) {

        TinyDB tinyDB = new TinyDB(context);

        Connection connection = null;

        String dbHost = tinyDB.getString("dbHost");
        String dbPort = tinyDB.getString("dbPort");
        String dbName = tinyDB.getString("dbName");
        String dbUser = tinyDB.getString("dbUser");
        String dbPass = tinyDB.getString("dbPass");

        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        if (!dbName.isEmpty()) {

            try {
                connection = DriverManager.getConnection(url, dbUser, dbPass);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return connection;
    }

    public void loadChat(Activity activity, TextView textView, ScrollView scrollView, Boolean autoScroll) {
        new Thread(() -> {
            Connection connection = null;

            try {

                connection = connectDB(activity.getApplicationContext());

                if (connection != null) {

                    loadChatMethod(activity, connection, textView, scrollView, autoScroll);
                }

            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public void searchMessage(Activity activity, TextView textView, String string) {

        new Thread(() -> {
            Connection connection = null;

            try {

                connection = connectDB(activity.getApplicationContext());

                if (connection != null) {

                    PreparedStatement pst;

                    String sql = "SELECT * FROM CHAT ORDER BY ID ASC";

                    pst = connection.prepareStatement(sql);

                    ResultSet rs = pst.executeQuery();

                    SpannableStringBuilder chatBuilder = new SpannableStringBuilder();

                    while (rs.next()) {
                        String user_name = rs.getString("USER_NAME");
                        String user_message = rs.getString("USER_MESSAGE");

                        String messageDecrypted = decryptMessage(activity, user_message);

                        if (messageDecrypted.contains(string)) {

                            chatBuilder.append(textStylized(activity.getApplicationContext(), user_name, messageDecrypted)).append("\n");
                        }
                    }

                    rs.close();
                    pst.close();

                    activity.runOnUiThread(() -> {
                        Spanned spannedText = SpannableStringBuilder.valueOf(chatBuilder);
                        textView.setText(spannedText);
                    });
                }
            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public void insertIntoChat(Activity activity, String user_name, String user_message, TextView textView, ScrollView scrollView, Boolean autoScroll) {

        new Thread(() -> {
            Connection connection = null;

            String encryptedMessage = encryptMessage(activity, user_message);

            if (encryptedMessage.isEmpty()) {

                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), "Can't encrypt message, fault key.", Toast.LENGTH_SHORT).show());

                return;
            }

            try {

                connection = connectDB(activity.getApplicationContext());

                if (connection != null) {

                    PreparedStatement pst;

                    String sql = "INSERT INTO CHAT (USER_NAME, USER_MESSAGE) VALUES (?, ?)";

                    pst = connection.prepareStatement(sql);

                    pst.setString(1, user_name);
                    pst.setString(2, encryptedMessage);

                    pst.executeUpdate();

                    pst.close();

                    loadChatMethod(activity, connection, textView, scrollView, autoScroll);
                }

            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public interface ChatCallback {
        void onChatLoaded(String chat);
    }

    public void loadLastMsg(Context context, ChatCallback callback) {
        new Thread(() -> {
            Connection connection = null;

            try {

                connection = connectDB(context.getApplicationContext());

                if (connection != null) {

                    String sql = "SELECT * FROM CHAT ORDER BY ID DESC LIMIT 1";

                    Statement stmt = connection.createStatement();
                    ResultSet rs = stmt.executeQuery(sql);

                    StringBuilder chatBuilder = new StringBuilder();

                    while (rs.next()) {
                        int ID = rs.getInt("ID");
                        String user_name = rs.getString("USER_NAME");
                        String user_message = rs.getString("USER_MESSAGE");

                        String messageDecrypted = decryptMessage(context, user_message);

                        chatBuilder.append(ID).append("@").append(user_name).append(": ").append(messageDecrypted);
                    }

                    rs.close();
                    stmt.close();

                    callback.onChatLoaded(chatBuilder.toString());
                }

            } catch (SQLException e) {
                Log.e("Error", "Exception in loadLastMsg", e);
            } finally {
                try {
                    if (connection != null) {
                        connection.close();
                    }
                } catch (SQLException e) {
                    Log.e("Error", "SQL Exception: " + e);
                }
            }
        }).start();
    }

    public void loadChatMethod(Activity activity, Connection connection, TextView textView, ScrollView scrollView, Boolean autoScroll) throws SQLException {

        String sql = "SELECT * FROM CHAT ORDER BY ID ASC LIMIT 1000";

        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        SpannableStringBuilder chatBuilder = new SpannableStringBuilder();

        while (rs.next()) {
            String user_name = rs.getString("USER_NAME");
            String user_message = rs.getString("USER_MESSAGE");

            String messageDecrypted = decryptMessage(activity, user_message);

            chatBuilder.append(textStylized(activity.getApplicationContext(), user_name, messageDecrypted)).append("\n");
        }

        rs.close();
        stmt.close();

        activity.runOnUiThread(() -> {
            Spanned spannedText = SpannableStringBuilder.valueOf(chatBuilder);
            textView.setText(spannedText);

            if (autoScroll) {
                scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
            }
        });
    }

    public SpannableStringBuilder textStylized(Context context, String user_name, String user_message) {

        TinyDB tinyDB = new TinyDB(context);

        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(user_name + ": " + user_message);

        if (user_name.equals(tinyDB.getString("user"))) {

            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.BLUE), 0, user_name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        } else {

            spannableStringBuilder.setSpan(new ForegroundColorSpan(Color.RED), 0, user_name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        spannableStringBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, user_name.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        return spannableStringBuilder;
    }

    public String encryptMessage(Context context, String string) {
        TinyDB tinyDB = new TinyDB(context);
        EncryptUtils encryptUtils = new EncryptUtils();

        return encryptUtils.encrypt(string, tinyDB.getString("encryptKey"));
    }

    public String decryptMessage(Context context, String string) {

        TinyDB tinyDB = new TinyDB(context);
        EncryptUtils encryptUtils = new EncryptUtils();

        return encryptUtils.decrypt(string, tinyDB.getString("encryptKey"));
    }
}
