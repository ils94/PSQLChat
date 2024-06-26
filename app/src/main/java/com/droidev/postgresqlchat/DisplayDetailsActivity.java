package com.droidev.postgresqlchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class DisplayDetailsActivity extends AppCompatActivity {

    private TinyDB tinyDB;
    private ArrayList<String> savedDBs;
    private String current;

    private EditText identifyNameEditText;
    private EditText userNameEditText;
    private EditText dbNameEditText;
    private EditText dbUserEditText;
    private EditText dbPassEditText;
    private EditText dbHostEditText;
    private EditText dbPortEditText;

    private EditText dbEncryptKeyEditText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE,
                WindowManager.LayoutParams.FLAG_SECURE);

        setContentView(R.layout.activity_display_details);

        setTitle("Database's Details");

        tinyDB = new TinyDB(this);
        savedDBs = tinyDB.getListString("savedDBs");

        DatabaseDetails selectedDBDetails = (DatabaseDetails) getIntent().getSerializableExtra("selectedDBDetails");

        assert selectedDBDetails != null;
        String identifyName = selectedDBDetails.getIdentifyName().replace("Database Name:\n", "");
        String userName = selectedDBDetails.getUsername().replace("Username:\n", "");
        String dbName = selectedDBDetails.getDbName();
        String dbUser = selectedDBDetails.getDbUser();
        String dbPass = selectedDBDetails.getDbPass();
        String dbHost = selectedDBDetails.getDbHost();
        String dbPort = selectedDBDetails.getDbPort();
        String dbEncryptKey = selectedDBDetails.getDbEcryptKey();

        identifyNameEditText = findViewById(R.id.detailsDisplayIdentifyName);
        userNameEditText = findViewById(R.id.detailsDisplayUserName);
        dbNameEditText = findViewById(R.id.detailsDisplayDbName);
        dbUserEditText = findViewById(R.id.detailsDisplayDbUser);
        dbPassEditText = findViewById(R.id.detailsDisplayDbPass);
        dbHostEditText = findViewById(R.id.detailsDisplayDbHost);
        dbPortEditText = findViewById(R.id.detailsDisplayDbPort);
        dbEncryptKeyEditText = findViewById(R.id.detailsDisplayEncryptKey);

        Button editButton = findViewById(R.id.detailsBtnEdit);
        Button connectButton = findViewById(R.id.detailsBtnConnect);
        Button encryptionKeyButton = findViewById(R.id.detailsBtnGenerateEncryptKey);

        identifyNameEditText.setText(identifyName);
        userNameEditText.setText(userName);
        dbNameEditText.setText(dbName);
        dbUserEditText.setText(dbUser);
        dbPassEditText.setText(dbPass);
        dbHostEditText.setText(dbHost);
        dbPortEditText.setText(dbPort);
        dbEncryptKeyEditText.setText(dbEncryptKey);

        current = (identifyNameEditText.getText().toString()
                + "@@" + userNameEditText.getText().toString()
                + "@@" + dbNameEditText.getText().toString()
                + "@@" + dbUserEditText.getText().toString()
                + "@@" + dbPassEditText.getText().toString()
                + "@@" + dbHostEditText.getText().toString()
                + "@@" + dbPortEditText.getText().toString()
                + "@@" + dbEncryptKeyEditText.getText().toString());

        connectButton.setOnClickListener(view -> {

            if (identifyNameEditText.getText().toString().isEmpty()
                    || userNameEditText.getText().toString().isEmpty()
                    || dbNameEditText.getText().toString().isEmpty()
                    || dbUserEditText.getText().toString().isEmpty()
                    || dbPassEditText.getText().toString().isEmpty()
                    || dbHostEditText.getText().toString().isEmpty()
                    || dbPortEditText.getText().toString().isEmpty()
                    || dbEncryptKeyEditText.toString().isEmpty()) {

                Toast.makeText(DisplayDetailsActivity.this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (userNameEditText.getText().toString().length() > 10) {

                Toast.makeText(DisplayDetailsActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                clearTinyDBKeys();

                tinyDB.putString("identifyName", identifyNameEditText.getText().toString());
                tinyDB.putString("user", userNameEditText.getText().toString());
                tinyDB.putString("dbName", dbNameEditText.getText().toString());
                tinyDB.putString("dbUser", dbUserEditText.getText().toString());
                tinyDB.putString("dbPass", dbPassEditText.getText().toString());
                tinyDB.putString("dbHost", dbHostEditText.getText().toString());
                tinyDB.putString("dbPort", dbPortEditText.getText().toString());
                tinyDB.putString("encryptKey", dbEncryptKeyEditText.getText().toString());

                Toast.makeText(DisplayDetailsActivity.this, "Set database's credentials to connect to: " + identifyNameEditText.getText().toString() + ".", Toast.LENGTH_SHORT).show();

                DisplayDetailsActivity.this.finish();
            }
        });

        editButton.setOnClickListener(view -> updateTinyDB());

        encryptionKeyButton.setOnClickListener(view -> generateEncryptKey());

    }

    private void clearTinyDBKeys() {

        tinyDB.remove("user");
        tinyDB.remove("dbName");
        tinyDB.remove("dbUser");
        tinyDB.remove("dbPass");
        tinyDB.remove("dbHost");
        tinyDB.remove("dbPort");
        tinyDB.remove("encryptKey");
    }

    private void updateTinyDB() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setMessage("Save changes?");
        builder.setPositiveButton("Yes", (dialog, id) -> {

            String identifyName = identifyNameEditText.getText().toString();
            String userName = userNameEditText.getText().toString();
            String dbName = dbNameEditText.getText().toString();
            String dbUser = dbUserEditText.getText().toString();
            String dbPass = dbPassEditText.getText().toString();
            String dbHost = dbHostEditText.getText().toString();
            String dbPort = dbPortEditText.getText().toString();
            String encryptKey = dbEncryptKeyEditText.getText().toString();

            String[] fields = {identifyName, userName, dbName, dbUser, dbPass, dbHost, dbPort, encryptKey};

            for (String field : fields) {
                if (field.isEmpty()) {
                    Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            String newValue = identifyName + "@@" + userName + "@@" + dbName + "@@" + dbUser + "@@" + dbPass + "@@" + dbHost + "@@" + dbPort + "@@" + encryptKey;

            int index = savedDBs.indexOf(current);

            if (index != -1) {

                savedDBs.set(index, newValue);
            }

            tinyDB.putListString("savedDBs", savedDBs);

            Toast.makeText(DisplayDetailsActivity.this, "Credentials updated.", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("No", (dialog, id) -> dialog.cancel());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.chooseDBMenu2:

                Intent intent = new Intent(DisplayDetailsActivity.this, DisplayDBsActivity.class);
                startActivity(intent);
                DisplayDetailsActivity.this.finish();

                break;

            case R.id.addDB2:

                Intent intent2 = new Intent(DisplayDetailsActivity.this, AddCredentialsActivity.class);
                startActivity(intent2);
                DisplayDetailsActivity.this.finish();

                break;

            case R.id.qrCode:

                generateQRCode();

                break;

            case R.id.scanNewKey:

                startQRCodeScanner();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.display_details_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    public void generateEncryptKey() {

        EncryptUtils encryptUtils = new EncryptUtils();

        String key;

        try {
            key = encryptUtils.generateKey();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (!dbEncryptKeyEditText.getText().toString().isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to generate a new Encryption Key? You will lose your current Key permanently, and won't be able to Decrypt messages that were Encrypted with that Key.");
            builder.setPositiveButton("Yes", (dialog, id) -> dbEncryptKeyEditText.setText(key));
            builder.setNegativeButton("No", (dialog, id) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

            dbEncryptKeyEditText.setText(key);
        }
    }

    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the Encrypt Key QR Code");
        integrator.initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show();
            } else {

                String scannedResult = result.getContents();

                dbEncryptKeyEditText.setText(scannedResult);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void generateQRCode() {

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setTitle("Which QR Code to generate:")
                .setPositiveButton("Credentials", null)
                .setNegativeButton("Encrypt Key", null)
                .setNeutralButton("Cancel", null)
                .show();

        Button positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        Button negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE);
        Button neutralButton = dialog.getButton(AlertDialog.BUTTON_NEUTRAL);

        positiveButton.setOnClickListener(view -> {

            String dbCredentials = identifyNameEditText.getText().toString()
                    + "@@" + dbNameEditText.getText().toString()
                    + "@@" + dbUserEditText.getText().toString()
                    + "@@" + dbPassEditText.getText().toString()
                    + "@@" + dbHostEditText.getText().toString()
                    + "@@" + dbPortEditText.getText().toString()
                    + "@@" + dbEncryptKeyEditText.getText().toString();

            Intent intent3 = new Intent(DisplayDetailsActivity.this, QrCodeActivity.class);
            intent3.putExtra("dbCredentials", dbCredentials);
            startActivity(intent3);
            DisplayDetailsActivity.this.finish();

            dialog.dismiss();
        });

        negativeButton.setOnClickListener(view -> {

            String encryptKey = dbEncryptKeyEditText.getText().toString();

            Intent intent3 = new Intent(DisplayDetailsActivity.this, QrCodeActivity.class);
            intent3.putExtra("encryptKey", encryptKey);
            startActivity(intent3);
            DisplayDetailsActivity.this.finish();

            dialog.dismiss();
        });

        neutralButton.setOnClickListener(view -> dialog.cancel());
    }
}
