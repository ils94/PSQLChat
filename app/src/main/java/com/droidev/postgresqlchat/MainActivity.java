package com.droidev.postgresqlchat;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private TextView chat;
    private EditText textToSend;
    private Button send;
    private ScrollView scrollView;

    private Boolean autoScroll = true;

    private Handler handler;
    private final int delay = 5000;

    Menu menuItem;

    private TinyDB tinyDB;

    private static final int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();

        if (uri != null) {

            String path = uri.toString();

            if (path.contains("psqlchat")) {

                deepLink(path.replace("https://psqlchat.go/", ""));

            }

            if (path.contains("psqlchat.imgur.com")) {

                openWebView(path.replace("psqlchat.imgur.com", "i.imgur.com"));

                MainActivity.this.finish();
            }
        }

        tinyDB = new TinyDB(this);

        chat = findViewById(R.id.chat);
        textToSend = findViewById(R.id.textToSend);
        send = findViewById(R.id.send);
        scrollView = findViewById(R.id.scrollview);

        textToSend.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                send.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                send.setEnabled(s.toString().trim().length() != 0);
            }

            @Override
            public void afterTextChanged(Editable s) {
                send.setEnabled(s.toString().trim().length() != 0);
            }
        });

        textToSend.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_SEND) {

                sendText();

                return true;
            }

            return false;
        });

        send.setOnClickListener(v -> sendText());

        send.setEnabled(false);

        if (!tinyDB.getString("textSize").isEmpty()) {

            chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Integer.parseInt(tinyDB.getString("textSize")));
        }

        startUp();
    }

    private void openWebView(String url) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra("url", url);
        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.pauseResumeChatLoop:

                pauseResumeChatLoop();

                break;

            case R.id.loginDB:

                login();

                break;

            case R.id.loginImgur:

                saveImgurAPIKey();

                break;

            case R.id.shareLink:

                if (tinyDB.getString("dbName").isEmpty()) {

                    Toast.makeText(this, "There are no credentials saved yet.", Toast.LENGTH_SHORT).show();
                } else {

                    String link = "https://psqlchat.go/"
                            + tinyDB.getString("dbName")
                            + "/" + tinyDB.getString("dbUser")
                            + "/" + tinyDB.getString("dbPass")
                            + "/" + tinyDB.getString("dbHost")
                            + "/" + tinyDB.getString("dbPort");

                    Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
                    shareLinkIntent.setType("text/plain");
                    shareLinkIntent.putExtra(Intent.EXTRA_TEXT, link);
                    startActivity(Intent.createChooser(shareLinkIntent, "Share link to..."));
                }

                break;

            case R.id.changeTextSize:

                changeTextSize();

                break;

            case R.id.showAllMessages:

                showAllMessages();

                break;

            case R.id.notificationsON:

                String permission_storage = Manifest.permission.POST_NOTIFICATIONS;

                if (ContextCompat.checkSelfPermission(this, permission_storage) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission_storage}, 3);
                } else {

                    restartBackgroundService();

                    Toast.makeText(this, "Notifications ON.", Toast.LENGTH_SHORT).show();
                }

                break;

            case R.id.notificationsOFF:

                stopBackgroundService();

                Toast.makeText(this, "Notifications OFF.", Toast.LENGTH_SHORT).show();

                break;

            case R.id.uploadImage:

                String permission_notification = Manifest.permission.READ_MEDIA_IMAGES;

                if (ContextCompat.checkSelfPermission(this, permission_notification) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{permission_notification}, 2);
                } else {

                    pickImage();
                }

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);

        menuItem = menu;

        return super.onCreateOptionsMenu(menu);
    }

    private void loadChat() {

        if (NetworkUtils.isNetworkAvailable(this)) {

            dbQueries db = new dbQueries();

            db.loadChat(MainActivity.this, chat, scrollView, autoScroll);
        }
    }

    private void loadChatHandlerLoop() {
        if (handler == null) {
            handler = new Handler();
        } else {
            handler.removeCallbacksAndMessages(null);
        }

        handler.postDelayed(new Runnable() {
            public void run() {
                loadChat();
                handler.postDelayed(this, delay);
            }
        }, delay);
    }

    private void sendText() {

        if (!textToSend.getText().toString().isEmpty()) {

            prepareToSendText(textToSend.getText().toString());

        } else {

            Toast.makeText(this, "Message cannot be empty.", Toast.LENGTH_SHORT).show();
        }
    }

    private void prepareToSendText(String msg) {

        if (NetworkUtils.isNetworkAvailable(this)) {

            dbQueries db = new dbQueries();

            db.insertIntoChat(MainActivity.this, tinyDB.getString("user"), msg, chat, scrollView, autoScroll);

            textToSend.setText("");

            resumeChatLoop();

            send.setEnabled(false);
        } else {

            Toast.makeText(this, "No connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void login() {

        EditText user = new EditText(this);
        user.setHint("User Name");
        user.setInputType(InputType.TYPE_CLASS_TEXT);
        user.setMaxLines(1);

        EditText dbName = new EditText(this);
        dbName.setHint("dbName");
        dbName.setInputType(InputType.TYPE_CLASS_TEXT);
        dbName.setMaxLines(1);

        EditText dbUser = new EditText(this);
        dbUser.setHint("dbUser");
        dbUser.setInputType(InputType.TYPE_CLASS_TEXT);
        dbUser.setMaxLines(1);

        EditText dbPass = new EditText(this);
        dbPass.setHint("dbPass");
        dbPass.setInputType(InputType.TYPE_CLASS_TEXT);
        dbPass.setMaxLines(1);

        EditText dbHost = new EditText(this);
        dbHost.setHint("dbHost");
        dbHost.setInputType(InputType.TYPE_CLASS_TEXT);
        dbHost.setMaxLines(1);

        EditText dbPort = new EditText(this);
        dbPort.setHint("dbPort");
        dbPort.setInputType(InputType.TYPE_CLASS_NUMBER);
        dbPort.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(user);
        lay.addView(dbName);
        lay.addView(dbUser);
        lay.addView(dbPass);
        lay.addView(dbHost);
        lay.addView(dbPort);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Insert user name and database credentials below:")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setNeutralButton("Clear all", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        user.setText(tinyDB.getString("user"));
        dbName.setText(tinyDB.getString("dbName"));
        dbUser.setText(tinyDB.getString("dbUser"));
        dbPass.setText(tinyDB.getString("dbPass"));
        dbHost.setText(tinyDB.getString("dbHost"));
        dbPort.setText(tinyDB.getString("dbPort"));

        if (dbPort.getText().toString().isEmpty()) {

            dbPort.setText("5432");
        }

        positiveButton.setOnClickListener(v -> {

            if (user.getText().toString().isEmpty() || dbName.getText().toString().isEmpty() || dbUser.getText().toString().isEmpty() || dbPass.getText().toString().isEmpty() || dbHost.getText().toString().isEmpty() || dbPort.getText().toString().isEmpty()) {

                Toast.makeText(MainActivity.this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (user.getText().toString().length() > 10) {

                Toast.makeText(MainActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                clearTinyDBKeys();

                tinyDB.putString("user", user.getText().toString());
                tinyDB.putString("dbName", dbName.getText().toString());
                tinyDB.putString("dbUser", dbUser.getText().toString());
                tinyDB.putString("dbPass", dbPass.getText().toString());
                tinyDB.putString("dbHost", dbHost.getText().toString());
                tinyDB.putString("dbPort", dbPort.getText().toString());

                startUp();

                dialog.dismiss();

                Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();
            }
        });

        neutralButton.setOnClickListener(v -> {

            dbName.setText("");
            dbUser.setText("");
            dbPass.setText("");
            dbHost.setText("");
        });
    }

    private void changeTextSize() {

        float scaledDensity = MainActivity.this.getResources().getDisplayMetrics().scaledDensity;
        float sp = chat.getTextSize() / scaledDensity;

        EditText textSize = new EditText(this);
        textSize.setHint("Current size is: " + sp);
        textSize.setInputType(InputType.TYPE_CLASS_NUMBER);
        textSize.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(textSize);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Adjust Text Size")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            try {

                if (!textSize.getText().toString().isEmpty()) {

                    chat.setTextSize(TypedValue.COMPLEX_UNIT_SP, Float.parseFloat(textSize.getText().toString()));

                    TinyDB tinyDB = new TinyDB(MainActivity.this);

                    tinyDB.remove("textSize");
                    tinyDB.putString("textSize", textSize.getText().toString());

                    dialog.dismiss();
                } else {

                    Toast.makeText(this, "Field cannot be empty.", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {

                Toast.makeText(MainActivity.this, e.toString(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearTinyDBKeys() {

        tinyDB.remove("user");
        tinyDB.remove("dbName");
        tinyDB.remove("dbUser");
        tinyDB.remove("dbPass");
        tinyDB.remove("dbHost");
        tinyDB.remove("dbPort");
    }

    public void deepLink(String link) {

        EditText userName = new EditText(this);
        userName.setHint("Insert your user name here");
        userName.setInputType(InputType.TYPE_CLASS_TEXT);
        userName.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(userName);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Login from Deeplink")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (userName.getText().toString().isEmpty()) {

                Toast.makeText(this, "User name cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (userName.getText().toString().length() > 10) {

                Toast.makeText(MainActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                String[] linkArray = link.split("/");

                clearTinyDBKeys();

                tinyDB.putString("user", userName.getText().toString());
                tinyDB.putString("dbName", linkArray[0]);
                tinyDB.putString("dbUser", linkArray[1]);
                tinyDB.putString("dbPass", linkArray[2]);
                tinyDB.putString("dbHost", linkArray[3]);
                tinyDB.putString("dbPort", linkArray[4]);

                startUp();

                dialog.dismiss();

                Toast.makeText(MainActivity.this, "Saved.", Toast.LENGTH_SHORT).show();

            }
        });
    }

    private void showAllMessages() {

        Intent intent = new Intent(this, ShowAllMessagesActivity.class);
        startActivity(intent);
    }

    private void pauseResumeChatLoop() {

        if (autoScroll) {

            Toast.makeText(MainActivity.this, "Chat loop paused.", Toast.LENGTH_SHORT).show();

            autoScroll = false;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.play);
        } else {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);
        }
    }

    private void resumeChatLoop() {

        if (!autoScroll) {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);
        }

    }

    private void startBackgroundService() {
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void stopBackgroundService() {
        Intent serviceIntent = new Intent(this, MyBackgroundService.class);
        stopService(serviceIntent);
    }

    public void restartBackgroundService() {
        stopBackgroundService();

        startBackgroundService();
    }

    public void startUp() {

        if (!tinyDB.getString("dbName").isEmpty()) {

            loadChat();
            loadChatHandlerLoop();
            tinyDB.remove("receivedMSGs");
        }

    }

    private void pickImage() {

        if (tinyDB.getString("ImgurAPI").isEmpty()) {

            saveImgurAPIKey();
        } else {

            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();

            String imagePath = getImagePath(selectedImageUri);

            ImgurUploader.uploadImage(this, Uri.parse(imagePath), imageUrl -> {
                if (imageUrl != null) {

                    AlertDialog dialog = new AlertDialog.Builder(this)
                            .setTitle("Send this image?")
                            .setPositiveButton("Yes", null)
                            .setNegativeButton("No", null)
                            .show();

                    Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

                    positiveButton.setOnClickListener(v -> {

                        prepareToSendText(imageUrl.replace("i.imgur.com", "psqlchat.imgur.com"));

                        dialog.dismiss();
                    });
                }
            });
        }
    }

    private String getImagePath(Uri uri) {
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor != null) {
            try {
                int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
                return cursor.getString(columnIndex);
            } finally {
                cursor.close();
            }
        } else {
            return uri.getPath();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 2) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickImage();
            } else {
                Toast.makeText(this, "Storage permission is required to pick an image.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == 3) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                restartBackgroundService();
            } else {
                Toast.makeText(this, "Allow notifications first.", Toast.LENGTH_SHORT).show();
            }
        }

    }

    public void saveImgurAPIKey() {

        EditText key = new EditText(this);
        key.setInputType(InputType.TYPE_CLASS_TEXT);
        key.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(key);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Save Imgur API Key")
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        key.setText(tinyDB.getString("ImgurAPI"));

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            if (!key.getText().toString().isEmpty()) {

                tinyDB.remove("ImgurAPI");

                tinyDB.putString("ImgurAPI", key.getText().toString().replace(" ", "").replace("\n", ""));

                dialog.dismiss();
            } else {

                Toast.makeText(this, "Field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}