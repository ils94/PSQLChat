package com.droidev.postgresqlchat;

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
import android.text.InputType;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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

    private boolean isAppRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Uri uri = getIntent().getData();

        if (uri != null) {

            String path = uri.toString();

            if (path.contains("psqlchat.go")) {

                Intent intent = new Intent(MainActivity.this, AddCredentialsActivity.class);
                intent.putExtra("link", path.replace("https://psqlchat.go/", ""));
                startActivity(intent);

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

        textToSend.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendText();
                return true;
            }
            return false;
        });

        send.setOnClickListener(v -> sendText());

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

            case R.id.chooseDB:

                chooseDB();

                break;

            case R.id.addDB:

                addDB();

                break;

            case R.id.saveImgurAPIKey:

                saveImgurAPIKey(true);

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
                if (isAppRunning) {
                    loadChat();
                    handler.postDelayed(this, delay);
                }
            }
        }, delay);
    }

    @Override
    protected void onStop() {
        super.onStop();
        isAppRunning = false;
        chat.setText("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isAppRunning = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isAppRunning = true;
        startUp();
    }

    private void sendText() {

        if (!textToSend.getText().toString().isEmpty()) {

            prepareToSendText(textToSend.getText().toString());
        }
    }

    private void prepareToSendText(String msg) {

        if (NetworkUtils.isNetworkAvailable(this)) {

            dbQueries db = new dbQueries();

            db.insertIntoChat(MainActivity.this, tinyDB.getString("user"), msg, chat, scrollView, autoScroll);

            resumeChatLoop();

            send.setEnabled(false);

            new Handler().postDelayed(() -> send.setEnabled(true), 3000);
        } else {

            Toast.makeText(this, "No connection available.", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("SetTextI18n")
    private void chooseDB() {

        Intent intent = new Intent(MainActivity.this, DisplayDBsActivity.class);
        startActivity(intent);
    }

    @SuppressLint("SetTextI18n")
    private void addDB() {

        Intent intent = new Intent(MainActivity.this, AddCredentialsActivity.class);
        startActivity(intent);
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

        tinyDB.remove("receivedMSGs");

        if (!tinyDB.getString("dbName").isEmpty()) {

            loadChat();
            loadChatHandlerLoop();
            setTitle(tinyDB.getString("identifyName"));
        }
    }

    private void pickImage() {

        if (tinyDB.getString("ImgurAPI").isEmpty()) {

            saveImgurAPIKey(false);
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

            View dialogView = getLayoutInflater().inflate(R.layout.custom_alert_dialog, null);
            ImageView imageView = dialogView.findViewById(R.id.imageView);

            int desiredWidth = 500;
            int desiredHeight = 500;
            imageView.setLayoutParams(new LinearLayout.LayoutParams(desiredWidth, desiredHeight));

            imageView.setImageURI(selectedImageUri);

            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Send this image?")
                    .setPositiveButton("Yes", null)
                    .setNegativeButton("No", null)
                    .setView(dialogView)
                    .show();

            Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

            positiveButton.setOnClickListener(v -> {

                dialog.dismiss();

                ImgurUploader.uploadImage(this, Uri.parse(imagePath), imageUrl -> {
                    if (imageUrl != null) {
                        prepareToSendText(imageUrl.replace("i.imgur.com", "psqlchat.imgur.com"));
                    }
                });
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

    public void saveImgurAPIKey(Boolean justSaving) {

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

                if (!justSaving) {

                    pickImage();
                }
            } else {

                Toast.makeText(this, "Field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}