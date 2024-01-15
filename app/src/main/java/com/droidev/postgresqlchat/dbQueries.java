package com.droidev.postgresqlchat;

import android.app.Activity;
import android.content.Context;
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

    public Connection connectDB(Context context) throws SQLException {

        TinyDB tinyDB = new TinyDB(context);

        Connection connection = null;

        String dbHost = tinyDB.getString("dbHost");
        String dbPort = tinyDB.getString("dbPort");
        String dbName = tinyDB.getString("dbName");
        String dbUser = tinyDB.getString("dbUser");
        String dbPass = tinyDB.getString("dbPass");

        String url = "jdbc:postgresql://" + dbHost + ":" + dbPort + "/" + dbName;

        if (!dbName.isEmpty()) {

            connection = DriverManager.getConnection(url, dbUser, dbPass);
        }

        return connection;
    }

    public void loadChat(Activity activity, TextView textView, ScrollView scrollView, Boolean autoScroll) {
        new Thread(() -> {
            Connection connection = null;

            try {

                String sql = "SELECT * FROM CHAT ORDER BY ID ASC LIMIT 1000";

                connection = connectDB(activity);

                Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery(sql);

                StringBuilder chatBuilder = new StringBuilder();

                while (rs.next()) {
                    String user_name = rs.getString("USER_NAME");
                    String user_message = rs.getString("USER_MESSAGE");

                    chatBuilder.append(user_name).append(": ").append(user_message).append("\n");
                }

                rs.close();
                stmt.close();

                activity.runOnUiThread(() -> {
                    textView.setText(chatBuilder.toString());

                    if (autoScroll) {

                        scrollView.post(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN));
                    }
                });
            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    assert connection != null;
                    connection.close();
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

                connection = connectDB(activity);

                PreparedStatement pst;

                String sql = "SELECT * FROM CHAT WHERE USER_NAME ILIKE ? OR USER_MESSAGE ILIKE ? ORDER BY ID ASC";

                pst = connection.prepareStatement(sql);

                pst.setString(1, "%" + string + "%");
                pst.setString(2, "%" + string + "%");

                ResultSet rs = pst.executeQuery();

                StringBuilder chatBuilder = new StringBuilder();

                while (rs.next()) {

                    String user_name = rs.getString("USER_NAME");
                    String user_message = rs.getString("USER_MESSAGE");

                    chatBuilder.append(user_name).append(": ").append(user_message).append("\n");
                }

                rs.close();
                pst.close();

                activity.runOnUiThread(() -> textView.setText(chatBuilder.toString()));
            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    assert connection != null;
                    connection.close();
                } catch (SQLException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }

    public void insertIntoChat(Activity activity, String user_name, String user_message) {

        new Thread(() -> {
            Connection connection = null;

            try {

                connection = connectDB(activity);

                PreparedStatement pst;

                String sql = "INSERT INTO CHAT (USER_NAME, USER_MESSAGE) VALUES (?, ?)";

                pst = connection.prepareStatement(sql);

                pst.setString(1, user_name);
                pst.setString(2, user_message);

                pst.executeUpdate();

                pst.close();

            } catch (SQLException e) {
                activity.runOnUiThread(() ->
                        Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    assert connection != null;
                    connection.close();
                } catch (SQLException e) {
                    activity.runOnUiThread(() ->
                            Toast.makeText(activity.getBaseContext(), e.toString(), Toast.LENGTH_SHORT).show());
                }
            }
        }).start();
    }
}
