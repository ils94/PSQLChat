package com.droidev.postgresqlchat;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayDetailsActivity extends AppCompatActivity {

    TinyDB tinyDB;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_details);

        tinyDB = new TinyDB(this);

        DatabaseDetails selectedDBDetails = (DatabaseDetails) getIntent().getSerializableExtra("selectedDBDetails");

        String identifyName = selectedDBDetails.getIdentifyName().replace("Database Name:\n", "");
        String userName = selectedDBDetails.getUsername().replace("Username:\n", "");
        String dbName = selectedDBDetails.getDbName();
        String dbUser = selectedDBDetails.getDbUser();
        String dbPass = selectedDBDetails.getDbPass();
        String dbHost = selectedDBDetails.getDbHost();
        String dbPort = selectedDBDetails.getDbPort();

        EditText identifyNameEditText = findViewById(R.id.detailsDisplayIdentifyName);
        EditText userNameEditText = findViewById(R.id.detailsDisplayUserName);
        EditText dbNameEditText = findViewById(R.id.detailsDisplayDbName);
        EditText dbUserEditText = findViewById(R.id.detailsDisplayDbUser);
        EditText dbPassEditText = findViewById(R.id.detailsDisplayDbPass);
        EditText dbHostEditText = findViewById(R.id.detailsDisplayDbHost);
        EditText dbPortEditText = findViewById(R.id.detailsDisplayDbPort);

        Button editButton = findViewById(R.id.detailsBtnEdit);
        Button connectButton = findViewById(R.id.detailsBtnConnect);

        identifyNameEditText.setText(identifyName);
        userNameEditText.setText(userName);
        dbNameEditText.setText(dbName);
        dbUserEditText.setText(dbUser);
        dbPassEditText.setText(dbPass);
        dbHostEditText.setText(dbHost);
        dbPortEditText.setText(dbPort);

        connectButton.setOnClickListener(view -> {

            if (identifyNameEditText.getText().toString().isEmpty()
                    || userNameEditText.getText().toString().isEmpty()
                    || dbNameEditText.getText().toString().isEmpty()
                    || dbUserEditText.getText().toString().isEmpty()
                    || dbPassEditText.getText().toString().isEmpty()
                    || dbHostEditText.getText().toString().isEmpty()
                    || dbPortEditText.getText().toString().isEmpty()) {

                Toast.makeText(DisplayDetailsActivity.this, "Fields cannot be empty.", Toast.LENGTH_SHORT).show();

            } else if (userNameEditText.getText().toString().length() > 10) {

                Toast.makeText(DisplayDetailsActivity.this, "User name cannot be bigger than 10 characters.", Toast.LENGTH_SHORT).show();
            } else {

                clearTinyDBKeys();

                tinyDB.putString("user", userNameEditText.getText().toString());
                tinyDB.putString("dbName", dbNameEditText.getText().toString());
                tinyDB.putString("dbUser", dbUserEditText.getText().toString());
                tinyDB.putString("dbPass", dbPassEditText.getText().toString());
                tinyDB.putString("dbHost", dbHostEditText.getText().toString());
                tinyDB.putString("dbPort", dbPortEditText.getText().toString());
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
}
