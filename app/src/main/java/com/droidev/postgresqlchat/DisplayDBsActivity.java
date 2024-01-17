package com.droidev.postgresqlchat;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class DisplayDBsActivity extends AppCompatActivity {

    private TinyDB tinyDB;
    private ArrayList<String> savedDBs;
    private ArrayList<DatabaseDetails> databaseDetailsList;
    private RecyclerViewAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_dbs);

        tinyDB = new TinyDB(this);
        savedDBs = tinyDB.getListString("savedDBs");
        databaseDetailsList = new ArrayList<>();

        for (String savedDB : savedDBs) {
            String[] detailsArray = savedDB.split("\\|");
            if (detailsArray.length >= 7) {
                DatabaseDetails databaseDetails = new DatabaseDetails(
                        detailsArray[0], detailsArray[1], detailsArray[2],
                        detailsArray[3], detailsArray[4], detailsArray[5],
                        detailsArray[6]
                );
                databaseDetailsList.add(databaseDetails);
            }
        }

        RecyclerView recyclerViewSavedDBs = findViewById(R.id.recyclerViewSavedDBs);
        adapter = new RecyclerViewAdapter(databaseDetailsList);

        recyclerViewSavedDBs.setAdapter(adapter);
        recyclerViewSavedDBs.setLayoutManager(new LinearLayoutManager(this));

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                    @Override
                    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                        int position = viewHolder.getAdapterPosition();
                        DatabaseDetails removedItem = databaseDetailsList.remove(position);
                        adapter.notifyItemRemoved(position);
                        updateTinyDB(savedDBs, removedItem);
                    }
                };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerViewSavedDBs);

        adapter.setOnItemClickListener(position -> {
            DatabaseDetails selectedDBDetails = databaseDetailsList.get(position);
            Intent intent = new Intent(DisplayDBsActivity.this, DisplayDetailsActivity.class);
            intent.putExtra("selectedDBDetails", selectedDBDetails);
            startActivity(intent);
        });
    }

    private void updateTinyDB(ArrayList<String> savedDBs, DatabaseDetails removedItem) {
        savedDBs.remove(removedItem.getIdentifyName() + "|" + removedItem.getUsername() + "|" +
                removedItem.getDbName() + "|" + removedItem.getDbUser() + "|" +
                removedItem.getDbPass() + "|" + removedItem.getDbHost() + "|" + removedItem.getDbPort());

        tinyDB.putListString("savedDBs", savedDBs);

        Toast.makeText(this, removedItem.getIdentifyName() + " was removed.", Toast.LENGTH_SHORT).show();
    }
}
