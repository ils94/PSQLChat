package com.droidev.postgresqlchat;

import android.os.Bundle;
import android.view.View;
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

        String selectedDBDetails = getIntent().getStringExtra("selectedDBDetails");

        String[] detailsArray = selectedDBDetails.split("\\|");

        EditText identifyNameEditText = findViewById(R.id.displayIdentifyName);
        EditText userNameEditText = findViewById(R.id.displayUserName);
        EditText dbNameEditText = findViewById(R.id.displayDbName);
        EditText dbUserEditText = findViewById(R.id.displayDbUser);
        EditText dbPassEditText = findViewById(R.id.displayDbPass);
        EditText dbHostEditText = findViewById(R.id.displayDbHost);
        EditText dbPortEditText = findViewById(R.id.displayDbPort);

        Button editButton = findViewById(R.id.btnEdit);
        Button connectButton = findViewById(R.id.btnConnect);

        if (detailsArray.length == 7) {
            identifyNameEditText.setText(detailsArray[0]);
            userNameEditText.setText(detailsArray[1]);
            dbNameEditText.setText(detailsArray[2]);
            dbUserEditText.setText(detailsArray[3]);
            dbPassEditText.setText(detailsArray[4]);
            dbHostEditText.setText(detailsArray[5]);
            dbPortEditText.setText(detailsArray[6]);
        }

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
