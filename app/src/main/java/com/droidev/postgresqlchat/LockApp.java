package com.droidev.postgresqlchat;

import android.app.AlertDialog;
import android.content.Context;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

public class LockApp {

    private AlertDialog alertDialog;

    public void createPassword(Context context) {

        TinyDB tinyDB = new TinyDB(context);

        EditText pass1 = new EditText(context);
        pass1.setHint("Input your Password");
        pass1.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText pass2 = new EditText(context);
        pass2.setHint("Confirm your Password");
        pass2.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(pass1);
        lay.addView(pass2);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Create a Password")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton((AlertDialog.BUTTON_NEGATIVE));

        positiveButton.setOnClickListener(v -> {

            String text1 = pass1.getText().toString();
            String text2 = pass2.getText().toString();

            if (text1.equals(text2) && !text1.isEmpty()) {

                tinyDB.putString("password", text1);

                tinyDB.putBoolean("lock", true);

                Toast.makeText(context, "App Lock was enabled.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            } else {

                Toast.makeText(context, "Passwords doesn't match.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

    }

    public void removeLock(Context context) {

        TinyDB tinyDB = new TinyDB(context);

        EditText pass1 = new EditText(context);
        pass1.setHint("Input your Password");
        pass1.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(pass1);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setTitle("Remove lock")
                .setView(lay)
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton((AlertDialog.BUTTON_NEGATIVE));

        positiveButton.setOnClickListener(v -> {

            String text1 = pass1.getText().toString();
            String text2 = tinyDB.getString("password");

            if (text1.equals(text2)) {

                tinyDB.remove("password");

                tinyDB.putBoolean("lock", false);

                Toast.makeText(context, "App Lock was removed.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            } else {

                Toast.makeText(context, "Wrong Password.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

    }

    public void changePassword(Context context) {

        TinyDB tinyDB = new TinyDB(context);

        EditText pass1 = new EditText(context);
        pass1.setHint("Old Password");
        pass1.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText pass2 = new EditText(context);
        pass2.setHint("New Password");
        pass2.setInputType(InputType.TYPE_CLASS_TEXT);

        EditText pass3 = new EditText(context);
        pass3.setHint("Confirm new Password");
        pass3.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(pass1);
        lay.addView(pass2);
        lay.addView(pass3);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(lay)
                .setTitle("Change Password")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton((AlertDialog.BUTTON_NEGATIVE));

        positiveButton.setOnClickListener(v -> {

            String oldPassword = tinyDB.getString("password");
            String text1 = pass1.getText().toString();
            String text2 = pass2.getText().toString();
            String text3 = pass3.getText().toString();

            if (text1.equals(oldPassword) && text2.equals(text3)) {

                tinyDB.remove("password");

                tinyDB.putString("password", text2);

                Toast.makeText(context, "Password was changed.", Toast.LENGTH_SHORT).show();

                dialog.dismiss();

            } else {

                Toast.makeText(context, "Passwords doesn't match.", Toast.LENGTH_SHORT).show();
            }
        });

        negativeButton.setOnClickListener(v -> dialog.dismiss());

    }

    public interface LoginCallback {
        void onLoginResult(boolean success);
    }

    public void dismissLoginDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
            alertDialog = null;
        }
    }

    public void login(Context context, LoginCallback callback) {

        TinyDB tinyDB = new TinyDB(context);

        EditText pass1 = new EditText(context);
        pass1.setHint("Input your Password");
        pass1.setInputType(InputType.TYPE_CLASS_TEXT);

        LinearLayout lay = new LinearLayout(context);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(pass1);

        alertDialog = new AlertDialog.Builder(context)
                .setCancelable(false)
                .setView(lay)
                .setTitle("Input your Password")
                .setPositiveButton("Ok", null)
                .show();

        Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            String text1 = pass1.getText().toString();
            String text2 = tinyDB.getString("password");

            if (text1.equals(text2)) {
                alertDialog.dismiss();
                callback.onLoginResult(true);
            } else {
                Toast.makeText(context, "Wrong Password.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
