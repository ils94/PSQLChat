package com.droidev.postgresqlchat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class generateEncryptKeyActivity extends AppCompatActivity {

    Button buttonGenerate, buttonCopy;
    TextView textViewEncryptKey;

    EncryptUtils encryptUtils;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generate_encrypt_key);

        encryptUtils = new EncryptUtils();

        buttonGenerate = findViewById(R.id.buttonGenerateEncryptKey);
        buttonCopy = findViewById(R.id.buttonCopy);

        textViewEncryptKey = findViewById(R.id.textViewEncryptKey);


        buttonGenerate.setOnClickListener(view -> {

            try {
                textViewEncryptKey.setText(encryptUtils.generateKey());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        buttonCopy.setOnClickListener(view -> copyKey(generateEncryptKeyActivity.this));

    }

    public void copyKey(Context context) {

        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Encrypt Key", textViewEncryptKey.getText().toString());
        clipboard.setPrimaryClip(clip);
    }
}