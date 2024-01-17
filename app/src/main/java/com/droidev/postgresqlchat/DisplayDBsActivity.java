package com.droidev.postgresqlchat;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DisplayDBsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_dbs);

        // Retrieve saved databases from TinyDB
        TinyDB tinyDB = new TinyDB(this);
        ArrayList<String> savedDBs = tinyDB.getListString("savedDBs");

        // Initialize RecyclerView and RecyclerViewAdapter
        RecyclerView recyclerViewSavedDBs = findViewById(R.id.recyclerViewSavedDBs);
        RecyclerViewAdapter adapter = new RecyclerViewAdapter(savedDBs);

        // Set the adapter to the RecyclerView
        recyclerViewSavedDBs.setAdapter(adapter);
        recyclerViewSavedDBs.setLayoutManager(new LinearLayoutManager(this));

        // Implement swipe-to-delete functionality using ItemTouchHelper
        ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // Remove the swiped item from the list
                int position = viewHolder.getAdapterPosition();
                savedDBs.remove(position);
                adapter.notifyItemRemoved(position);

                // Save the updated list to TinyDB
                tinyDB.putListString("savedDBs", savedDBs);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerViewSavedDBs);

        // Set item click listener to start DisplayDetailsActivity
        adapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
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
