package com.example.audiorecorder;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class ListAdapter extends RecyclerView.Adapter<ViewHolder> {
    final private List<String> FilePath;

    public ListAdapter(List<String> FilePath) { this.FilePath = FilePath;}

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        ViewHolder vh = new ViewHolder(v);
        vh.itemView.setOnClickListener(view -> {
            int position = vh.getBindingAdapterPosition();

            String Path = FilePath.get(position);
            Context context = view.getContext();

            context.startActivity(new Intent(context, PlayerActivity.class)
                    .putExtra("player",Path));
        });

        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = new File(FilePath.get(position)).getName();
        holder.filePath.setText(name);
    }

    @Override
    public int getItemCount() { return this.FilePath.size(); }
}
