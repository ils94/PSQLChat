package com.droidev.postgresqlchat;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

public class QrCodeActivity extends AppCompatActivity {

    String intentString;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        Intent intent = getIntent();

        String dbCredentials = intent.getStringExtra("dbCredentials");
        String encryptKey = intent.getStringExtra("encryptKey");

        if (dbCredentials != null) {

            String[] contents = dbCredentials.split("/");
            setTitle(contents[0]);
            intentString = dbCredentials;
        } else if (encryptKey != null) {

            intentString = encryptKey;
            setTitle("Encrypt Key");
        }

        ImageView imageView = findViewById(R.id.imageView);

        try {
            Bitmap bitmap = generateQRCode(intentString);
            imageView.setImageBitmap(bitmap);
        } catch (WriterException e) {
            Toast.makeText(this, "Error generating QR code", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private Bitmap generateQRCode(String text) throws WriterException {
        BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, 1000, 1000);
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                bitmap.setPixel(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
            }
        }

        return bitmap;
    }
}