package com.droidev.postgresqlchat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class AddCredentialsActivity extends AppCompatActivity {

    private EditText identifyNameEditText;
    private EditText editTextUser;
    private EditText editTextDbName;
    private EditText editTextDbUser;
    private EditText editTextDbPass;
    private EditText editTextDbHost;
    private EditText editTextDbPort;
    private TinyDB tinyDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_credentials);

        tinyDB = new TinyDB(this);

        String link = getIntent().getStringExtra("link");

        identifyNameEditText = findViewById(R.id.identifyName);
        editTextUser = findViewById(R.id.editTextUser);
        editTextDbName = findViewById(R.id.editTextDbName);
        editTextDbUser = findViewById(R.id.editTextDbUser);
        editTextDbPass = findViewById(R.id.editTextDbPass);
        editTextDbHost = findViewById(R.id.editTextDbHost);
        editTextDbPort = findViewById(R.id.editTextDbPort);
        Button saveButton = findViewById(R.id.saveButton);

        saveButton.setOnClickListener(view -> saveDBCredentials());

        System.out.println("Erro: " + link);

        if (link != null) {

            String[] linkArray = link.split("/");

            editTextDbName.setText(linkArray[0]);
            editTextDbUser.setText(linkArray[1]);
            editTextDbPass.setText(linkArray[2]);
            editTextDbHost.setText(linkArray[3]);
            editTextDbPort.setText(linkArray[4]);
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

        if (identifyName.isEmpty() || userName.isEmpty() || dbName.isEmpty() ||
                dbUser.isEmpty() || dbPass.isEmpty() || dbHost.isEmpty() || dbPort.isEmpty()) {
            Toast.makeText(this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        for (String savedDB : savedDBs) {
            String[] credentials = savedDB.split("\\|");
            if (credentials.length >= 2 && credentials[0].equals(identifyName) || credentials[2].equals(dbName)) {

                Toast.makeText(this, "This database is already saved.", Toast.LENGTH_SHORT).show();

                return;
            }
        }

        String credentialsDB = identifyName + "|" + userName + "|" + dbName + "|" +
                dbUser + "|" + dbPass + "|" + dbHost + "|" + dbPort;

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
    }

    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.chooseDBMenu) {
            Intent intent = new Intent(AddCredentialsActivity.this, DisplayDBsActivity.class);
            startActivity(intent);
            AddCredentialsActivity.this.finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_credentials_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }
}
