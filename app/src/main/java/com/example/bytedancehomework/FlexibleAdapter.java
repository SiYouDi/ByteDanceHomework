package com.example.bytedancehomework;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FlexibleAdapter extends RecyclerView.Adapter<FlexibleAdapter.BaseViewHolder> {

    @NonNull
    @Override
    public FlexibleAdapter.BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull FlexibleAdapter.BaseViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    static class BaseViewHolder extends RecyclerView.ViewHolder
    {
        ImageView imageView;
        TextView textTitle;
        TextView textContent;
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);

            imageView=itemView.findViewById(R.id.imageView);
            textTitle=itemView.findViewById(R.id.textTitle);
            textContent=itemView.findViewById(R.id.textContent);
        }
    }

}

