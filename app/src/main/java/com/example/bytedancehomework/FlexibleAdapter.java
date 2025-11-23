package com.example.bytedancehomework;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

public class FlexibleAdapter extends RecyclerView.Adapter<FlexibleAdapter.BaseViewHolder> {

    ArrayList<FeedItem> items;
    DatabaseHelper dbHelper;
    LayoutMode layoutMode;
    OnItemClickListener itemClickListener;

    public interface OnItemClickListener
    {
        void onItemClick(FeedItem item);
        void onItemLongClick(FeedItem item);
    }

    public FlexibleAdapter(ArrayList<FeedItem>items,LayoutMode layoutMode,DatabaseHelper dbHelper)
    {
        this.items=items;
        this.layoutMode=layoutMode;
        this.dbHelper=dbHelper;
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.itemClickListener=listener;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView;

        switch (layoutMode)
        {
            case single:
                itemView=inflater.inflate(R.layout.layout_feed_item_single,parent,false);
                break;
            case grid:
                itemView=inflater.inflate(R.layout.layout_feed_item_grid,parent,false);
                break;
            case staggered:
                itemView=inflater.inflate(R.layout.layout_feed_item_staggered,parent,false);
                break;
            default:
                itemView=inflater.inflate(R.layout.layout_feed_item_single,parent,false);
        }

        return new BaseViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        FeedItem item=items.get(position);
        holder.bind(item);

        //设置系统监听器
        holder.itemView.setOnClickListener(v->{
            if(itemClickListener!=null)
            {
                itemClickListener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v->{
            if(itemClickListener!=null)
            {
                itemClickListener.onItemLongClick(item);
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void switchLayoutMode(LayoutMode layoutMode)
    {
        if(layoutMode!=this.layoutMode)
        {
            this.layoutMode=layoutMode;
            notifyDataSetChanged();
        }
    }

    public LayoutMode getLayoutMode()
    {
        return layoutMode;
    }

    public void addItem(FeedItem item)
    {
        long id=dbHelper.insertFeedItem(item);
        item.setId(id);
        items.add(0,item);
        notifyItemInserted(0);
    }

    public void delItem(int position)
    {
        FeedItem item=items.get(position);
        dbHelper.deleteFeedItem(item.getId());

        items.remove(position);
        notifyItemRemoved(position);
    }

    public void updateItem(int position,FeedItem item)
    {
        dbHelper.updateFeedItem(item);

        items.set(position,item);
        notifyItemChanged(position);
    }

    public void updateData(List<FeedItem> newItems)
    {
        dbHelper.updateAll(newItems);
        items=(ArrayList<FeedItem>) newItems;
        notifyDataSetChanged();
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

        public void bind(FeedItem item)
        {
            textTitle.setText(item.getTitle());
            textContent.setText(item.getContent());

            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(item.getImageUrl())
                        .apply(new RequestOptions()
                                .placeholder(R.drawable.placeholder_image) // 占位图
                                .error(R.drawable.error_image) // 错误图
                                .centerCrop())
                        .into(imageView);
            } else {
                // 如果没有图片URL，可以设置默认图片或隐藏ImageView
                imageView.setImageResource(R.drawable.placeholder_image);
            }
        }
    }

}

