package com.droidev.postgresqlchat;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<DatabaseDetails> dataSet;
    private OnItemClickListener onItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewIdentifyName;
        private final TextView textViewEditTextUser;
        private final CardView cardView;

        public ViewHolder(View view) {
            super(view);
            cardView = view.findViewById(R.id.cardViewSavedDB);
            textViewIdentifyName = view.findViewById(R.id.textViewIdentifyName);
            textViewEditTextUser = view.findViewById(R.id.textViewEditTextUser);
        }

        public CardView getCardView() {
            return cardView;
        }

        public TextView getTextViewIdentifyName() {
            return textViewIdentifyName;
        }

        public TextView getTextViewEditTextUser() {
            return textViewEditTextUser;
        }
    }

    public RecyclerViewAdapter(ArrayList<DatabaseDetails> data) {
        this.dataSet = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_saved_db, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint({"RecyclerView", "SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        DatabaseDetails details = dataSet.get(position);

        holder.getTextViewIdentifyName().setText("Database Name:\n" + details.getIdentifyName());
        holder.getTextViewEditTextUser().setText("Username:\n" + details.getUsername());

        holder.getCardView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(position);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }
}
