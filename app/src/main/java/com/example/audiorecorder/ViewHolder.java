package com.example.audiorecorder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder {
    View view;
    TextView filePath;

    public ViewHolder(@NonNull View itemView) {
        super(itemView);

        this.view = itemView;
        this.filePath = view.findViewById(R.id.file_path);
    }
}
