package com.droidev.postgresqlchat;

import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class DisplayDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_details);

        // Retrieve the selected database details from the Intent
        String selectedDBDetails = getIntent().getStringExtra("selectedDBDetails");

        // Split the string using "|" as the delimiter
        String[] detailsArray = selectedDBDetails.split("\\|");

        // Initialize EditText views
        EditText identifyNameEditText = findViewById(R.id.displayIdentifyName);
        EditText userNameEditText = findViewById(R.id.displayUserName);
        EditText dbNameEditText = findViewById(R.id.displayDbName);
        EditText dbUserEditText = findViewById(R.id.displayDbUser);
        EditText dbPassEditText = findViewById(R.id.displayDbPass);
        EditText dbHostEditText = findViewById(R.id.displayDbHost);
        EditText dbPortEditText = findViewById(R.id.displayDbPort);

        // Display the details in the corresponding EditText views
        if (detailsArray.length == 7) {
            identifyNameEditText.setText(detailsArray[0]);
            userNameEditText.setText(detailsArray[1]);
            dbNameEditText.setText(detailsArray[2]);
            dbUserEditText.setText(detailsArray[3]);
            dbPassEditText.setText(detailsArray[4]);
            dbHostEditText.setText(detailsArray[5]);
            dbPortEditText.setText(detailsArray[6]);
        }
    }
}
