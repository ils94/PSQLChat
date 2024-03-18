package com.droidev.postgresqlchat;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class AddCredentialsActivity extends AppCompatActivity {

    private EditText identifyNameEditText;
    private EditText editTextUser;
    private EditText editTextDbName;
    private EditText editTextDbUser;
    private EditText editTextDbPass;
    private EditText editTextDbHost;
    private EditText editTextDbPort;
    private EditText editTextEncryptKey;
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credentials);

        setTitle("Save New Database");

        tinyDB = new TinyDB(this);

        String link = getIntent().getStringExtra("link");

        identifyNameEditText = findViewById(R.id.identifyName);
        editTextUser = findViewById(R.id.editTextUser);
        editTextDbName = findViewById(R.id.editTextDbName);
        editTextDbUser = findViewById(R.id.editTextDbUser);
        editTextDbPass = findViewById(R.id.editTextDbPass);
        editTextDbHost = findViewById(R.id.editTextDbHost);
        editTextDbPort = findViewById(R.id.editTextDbPort);
        editTextEncryptKey = findViewById(R.id.editTextEncryptKey);

        Button saveButton = findViewById(R.id.saveButton);
        saveButton.setOnClickListener(view -> saveDBCredentials());

        Button generateEncryptKeyButton = findViewById(R.id.generateEncryptKeyButton);
        generateEncryptKeyButton.setOnClickListener(view -> generateEncryptKey());

        if (link != null) {

            String[] linkArray = link.split("/");

            if (linkArray.length == 6) {

                editTextDbName.setText(linkArray[0]);
                editTextDbUser.setText(linkArray[1]);
                editTextDbPass.setText(linkArray[2]);
                editTextDbHost.setText(linkArray[3]);
                editTextDbPort.setText(linkArray[4]);
                editTextEncryptKey.setText(linkArray[5]);
            } else if (linkArray.length == 5) {

                editTextDbName.setText(linkArray[0]);
                editTextDbUser.setText(linkArray[1]);
                editTextDbPass.setText(linkArray[2]);
                editTextDbHost.setText(linkArray[3]);
                editTextDbPort.setText(linkArray[4]);
            } else {

                Toast.makeText(this, "Link is invalid.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveDBCredentials() {
        ArrayList<String> savedDBs = tinyDB.getListString("savedDBs");

        String identifyName = identifyNameEditText.getText().toString();
        String userName = editTextUser.getText().toString();
        String dbName = editTextDbName.getText().toString();
        String dbUser = editTextDbUser.getText().toString();
        String dbPass = editTextDbPass.getText().toString();
        String dbHost = editTextDbHost.getText().toString();
        String dbPort = editTextDbPort.getText().toString();
        String encryptKey = editTextEncryptKey.getText().toString();

        String[] fields = {identifyName, userName, dbName, dbUser, dbPass, dbHost, dbPort, encryptKey};

        for (String field : fields) {
            if (field.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        for (String savedDB : savedDBs) {
            String[] credentials = savedDB.split("\\|");

            if (credentials[0].equals(identifyName)) {

                Toast.makeText(this, "This database is already saved.", Toast.LENGTH_SHORT).show();

                return;
            }
        }

        String credentialsDB = identifyName + "|" + userName + "|" + dbName + "|" +
                dbUser + "|" + dbPass + "|" + dbHost + "|" + dbPort + "|" + encryptKey;

        savedDBs.add(credentialsDB);

        tinyDB.putListString("savedDBs", savedDBs);

        Toast.makeText(this, "Credentials saved successfully.", Toast.LENGTH_SHORT).show();

        identifyNameEditText.getText().clear();
        editTextUser.getText().clear();
        editTextDbName.getText().clear();
        editTextDbUser.getText().clear();
        editTextDbPass.getText().clear();
        editTextDbHost.getText().clear();
        editTextDbPort.getText().clear();
        editTextEncryptKey.getText().clear();
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.chooseDBMenu:

                Intent intent = new Intent(AddCredentialsActivity.this, DisplayDBsActivity.class);
                startActivity(intent);
                AddCredentialsActivity.this.finish();

                break;

            case R.id.scanCode:

                startQRCodeScanner();

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_credentials_menu, menu);

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

        if (!editTextEncryptKey.getText().toString().isEmpty()) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Do you want to generate a new Encryption Key? You will lose your current Key permanently, and won't be able to Decrypt messages that were Encrypted with that Key.");
            builder.setPositiveButton("Yes", (dialog, id) -> editTextEncryptKey.setText(key));

            builder.setNegativeButton("No", (dialog, id) -> dialog.cancel());

            AlertDialog dialog = builder.create();
            dialog.show();
        } else {

            editTextEncryptKey.setText(key);
        }
    }


    private void startQRCodeScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setPrompt("Scan the Credentials QR Code");
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

                String[] contents = scannedResult.split("/");

                identifyNameEditText.setText(contents[0]);
                editTextDbName.setText(contents[1]);
                editTextDbUser.setText(contents[2]);
                editTextDbPass.setText(contents[3]);
                editTextDbHost.setText(contents[4]);
                editTextDbPort.setText(contents[5]);
                editTextEncryptKey.setText(contents[6]);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}
