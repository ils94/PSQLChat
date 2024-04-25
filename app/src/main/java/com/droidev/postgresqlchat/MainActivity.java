package com.droidev.postgresqlchat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
    private String link;
    LockApp lockApp;

    public static Boolean unlocked = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_main);

        tinyDB = new TinyDB(this);

        lockApp = new LockApp();

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

        String action = getIntent().getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleSendIntent(getIntent());
        } else if (Intent.ACTION_VIEW.equals(action)) {
            handleViewIntent(getIntent());
        }
    }

    private void handleSendIntent(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            textToSend.setText(sharedText);
        }
    }

    private void handleViewIntent(Intent intent) {
        Uri uri = intent.getData();
        if (uri != null) {
            String path = uri.toString();
            if (path.contains("psqlchat.go")) {
                Intent newIntent = new Intent(MainActivity.this, AddCredentialsActivity.class);
                newIntent.putExtra("link", path.replace("https://psqlchat.go/", ""));
                startActivity(newIntent);
            } else {
                textToSend.setText(uri.toString());
            }
        }
    }

    public void checkPassword() {

        TinyDB tinyDB = new TinyDB(MainActivity.this);

        chat.setText("");
        setTitle("");

        if (!tinyDB.getString("password").isEmpty()) {

            lockApp.login(MainActivity.this, success -> {
                if (success) {

                    unlocked = true;
                    isAppRunning = true;

                    startUp();
                }
            });

        } else {

            isAppRunning = true;

            startUp();
        }
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

                shareDBLink();

                break;

            case R.id.changeTextSize:

                changeTextSize();

                break;

            case R.id.showAllMessages:

                showAllMessages();

                break;

            case R.id.notificationsON:

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

                    String permission_storage = Manifest.permission.POST_NOTIFICATIONS;

                    if (ContextCompat.checkSelfPermission(this, permission_storage) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, new String[]{permission_storage}, 3);
                    } else {

                        msgStartAlarm();
                    }
                } else {

                    msgStartAlarm();
                }

                break;

            case R.id.notificationsOFF:

                msgCancelAlarm();

                Toast.makeText(this, "Notifications disabled.", Toast.LENGTH_SHORT).show();

                break;

            case R.id.lockApp:

                lockApp.createPassword(MainActivity.this);

                break;

            case R.id.resetPassword:

                lockApp.changePassword(MainActivity.this);

                break;

            case R.id.removeAppLock:
                lockApp.removeLock(MainActivity.this);

                break;

            case R.id.hideDecryptError:

                String hide = tinyDB.getString("hideError");

                if (hide.equals("ON")) {

                    hide = "OFF";

                    Toast.makeText(this, "Decrypt errors will now be shown.", Toast.LENGTH_SHORT).show();

                } else {

                    hide = "ON";

                    Toast.makeText(this, "Decrypt errors are now hidden.", Toast.LENGTH_SHORT).show();
                }

                tinyDB.remove("hideError");

                tinyDB.putString("hideError", hide);

                break;

            case R.id.changeRows:

                numberOfRows();

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

                    if (autoScroll) {

                        loadChat();
                    }

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

        if (!unlocked) {

            lockApp.dismissLoginDialog();

            checkPassword();
        } else {

            startUp();
        }
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

            textToSend.setText("");

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

            chat.setTextIsSelectable(true);
        } else {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);

            chat.setTextIsSelectable(false);
        }
    }

    private void resumeChatLoop() {

        if (!autoScroll) {

            Toast.makeText(MainActivity.this, "Chat loop resumed.", Toast.LENGTH_SHORT).show();

            autoScroll = true;

            menuItem.findItem(R.id.pauseResumeChatLoop).setIcon(R.drawable.pause);
        }
    }

    @SuppressLint("ScheduleExactAlarm")
    private void msgStartAlarm() {

        msgCancelAlarm();

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MsgAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, 5000, pendingIntent);

        Toast.makeText(this, "Notifications enabled.", Toast.LENGTH_SHORT).show();
    }

    private void msgCancelAlarm() {
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(this, MsgAlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_MUTABLE);

        alarmManager.cancel(pendingIntent);
    }

    public void startUp() {

        tinyDB.remove("receivedMSGs");

        if (tinyDB.getString("hideError").isEmpty()) {

            tinyDB.putString("hideError", "OFF");
        }

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

                msgStartAlarm();
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

    @SuppressLint("NonConstantResourceId")
    public void shareDBLink() {

        linkNoKey();

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Warning: Sharing the link along with the Encryption Key could potentially enable unauthorized individuals to decrypt your database messages. Ensure that you share the link with the Encryption Key through a secure method, and only with someone you trust.");
        LayoutInflater inflater = this.getLayoutInflater();

        View dialogView = inflater.inflate(R.layout.radio_dialog_layout, null);
        builder.setView(dialogView);

        RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group);

        RadioButton radioButton2 = dialogView.findViewById(R.id.radio_button2);
        radioButton2.setChecked(true);

        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_button1:

                    if (tinyDB.getString("dbName").isEmpty()) {

                        Toast.makeText(MainActivity.this, "There are no credentials saved yet.", Toast.LENGTH_SHORT).show();
                    } else {

                        linkWithKey();
                    }

                    break;
                case R.id.radio_button2:

                    if (tinyDB.getString("dbName").isEmpty()) {

                        Toast.makeText(MainActivity.this, "There are no credentials saved yet.", Toast.LENGTH_SHORT).show();
                    } else {

                        linkNoKey();
                    }

                    break;
            }
        });

        builder.setPositiveButton("OK", (dialog, which) -> {

            Intent shareLinkIntent = new Intent(Intent.ACTION_SEND);
            shareLinkIntent.setType("text/plain");
            shareLinkIntent.putExtra(Intent.EXTRA_TEXT, link);
            startActivity(Intent.createChooser(shareLinkIntent, "Share link with..."));
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void linkNoKey() {

        link = "https://psqlchat.go/"
                + tinyDB.getString("dbName")
                + "@@" + tinyDB.getString("dbUser")
                + "@@" + tinyDB.getString("dbPass")
                + "@@" + tinyDB.getString("dbHost")
                + "@@" + tinyDB.getString("dbPort");
    }

    public void linkWithKey() {

        link = "https://psqlchat.go/"
                + tinyDB.getString("dbName")
                + "@@" + tinyDB.getString("dbUser")
                + "@@" + tinyDB.getString("dbPass")
                + "@@" + tinyDB.getString("dbHost")
                + "@@" + tinyDB.getString("dbPort")
                + "@@" + tinyDB.getString("encryptKey");
    }

    public void numberOfRows() {

        EditText rowsEditText = new EditText(this);
        rowsEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        rowsEditText.setHint("");
        rowsEditText.setMaxLines(1);

        LinearLayout lay = new LinearLayout(this);
        lay.setOrientation(LinearLayout.VERTICAL);
        lay.addView(rowsEditText);

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("DB SELECT ROWS")
                .setPositiveButton("Ok", null)
                .setNegativeButton("Cancel", null)
                .setView(lay)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);

        positiveButton.setOnClickListener(v -> {

            String rows = rowsEditText.getText().toString();

            if (!rows.isEmpty()) {

                tinyDB.remove("rows");

                tinyDB.putString("rows", rows);

                Toast.makeText(this, "SELECT ROWS change to: " + rows, Toast.LENGTH_SHORT).show();

                dialog.dismiss();
            } else {

                Toast.makeText(this, "Field cannot be empty.", Toast.LENGTH_SHORT).show();
            }
        });

    }
}