package com.droidev.postgresqlchat;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;

public class DisplayDBsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_dbs);

        // Retrieve saved databases from TinyDB
        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> savedDBs = tinyDB.getListString("savedDBs");

        // Initialize ListView and ArrayAdapter
        ListView listViewSavedDBs = findViewById(R.id.listViewSavedDBs);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, savedDBs);

        // Set the adapter to the ListView
        listViewSavedDBs.setAdapter(adapter);

        // Set item click listener to start DisplayDetailsActivity
        listViewSavedDBs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Get the selected database details
                String selectedDBDetails = savedDBs.get(position);

                // Start DisplayDetailsActivity and pass the selected details
                Intent intent = new Intent(DisplayDBsActivity.this, DisplayDetailsActivity.class);
                intent.putExtra("selectedDBDetails", selectedDBDetails);
                startActivity(intent);
            }
        });
    }
}
