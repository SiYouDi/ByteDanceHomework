package com.example.bytedancehomework.Adapter;


import android.app.Activity;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bytedancehomework.DBHelper.DatabaseHelper;
import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.Item.FeedItem;
import com.example.bytedancehomework.R;

import java.util.ArrayList;
import java.util.List;

public class FlexibleAdapter extends RecyclerView.Adapter<FlexibleAdapter.BaseViewHolder> {

    List<FeedItem> items;
    DatabaseHelper dbHelper;
    LayoutMode layoutMode;
    OnItemClickListener itemClickListener;
    OnLoadMoreListener loadMoreListener;
    Activity activity;

    private static final int VIEW_TYPE_SINGLE = 0;
    private static final int VIEW_TYPE_GRID = 1;
    private static final int VIEW_TYPE_STAGGERED = 2;

    private final int PAGE_SIZE=5;
    private int currentPage=0;
    private boolean isLoading = false;
    private boolean hasMore = true;

    private long DELAY_TIME =2000;
    private OnShowLoadMoreButtonListener showLoadMoreButtonListener;

    public boolean isLoading()
    {
        return isLoading;
    }

    public boolean hasMore()
    {
        return hasMore;
    }

    public int getPosition(FeedItem item) {
        int position = items.indexOf(item);
        return position;
    }

    //事件监听器
    public interface OnItemClickListener
    {
        void onItemClick(FeedItem item);
        void onItemLongClick(FeedItem item);
    }

    public interface OnLoadMoreListener
    {
        void onLoadMoreStarted();
        void onLoadComplete(List<FeedItem> newItems);
        void onLoadError(String error);
    }

    public interface OnShowLoadMoreButtonListener {
        void onShouldShowLoadMoreButton();
        void onShouldHideLoadMoreButton();
    }

    public FlexibleAdapter(Activity activity,List<FeedItem>items,LayoutMode layoutMode,DatabaseHelper dbHelper)
    {
        this.activity=activity;
        this.items=items;
        this.layoutMode=layoutMode;
        this.dbHelper=dbHelper;
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        this.itemClickListener=listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener)
    {
        this.loadMoreListener =listener;
    }

    public void setOnShowLoadMoreButtonListener(OnShowLoadMoreButtonListener listener) {
        this.showLoadMoreButtonListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        FeedItem item = items.get(position);
        LayoutMode layoutMode1 = item.getLayoutMode();
        return layoutMode1.ordinal();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView=null;

        switch (viewType)
        {
            case VIEW_TYPE_SINGLE:
                itemView=inflater.inflate(R.layout.layout_feed_item_single,parent,false);
                break;
            case VIEW_TYPE_GRID:
                itemView=inflater.inflate(R.layout.layout_feed_item_grid,parent,false);
                break;
            case VIEW_TYPE_STAGGERED:
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

        if(position >= getItemCount() - 2 && hasMore && !isLoading && showLoadMoreButtonListener != null)
        {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Adapter", "onBindViewHolder: 显示加载更多按钮");
                    showLoadMoreButtonListener.onShouldShowLoadMoreButton();
                }
            });
        }
        if (position < getItemCount() - 2 && showLoadMoreButtonListener != null) {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("Adapter", "onBindViewHolder: 隐藏加载更多按钮");
                    showLoadMoreButtonListener.onShouldHideLoadMoreButton();
                }
            });
        }

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

//    private Runnable loadMoreRunnable = new Runnable() {
//        @Override
//        public void run() {
//            if(!isLoading&&hasMore)
//            {
//                loadNextPage();
//            }
//        }
//    };

    public void loadNextPage() {
        if(isLoading||!hasMore)
            return;

        isLoading=true;

        if (loadMoreListener != null) {
            loadMoreListener.onLoadMoreStarted();
        }

        new Thread(()->{
            try {
                List<FeedItem> newItems= dbHelper.getFeedItemsByPage(currentPage,PAGE_SIZE);

                activity.runOnUiThread(()->{
                    try {
                        if(newItems!=null && !newItems.isEmpty()) {
                            if(newItems.size() < PAGE_SIZE) {
                                hasMore = false;
                            }

                            int startPosition = items.size();
                            items.addAll(newItems);
                            notifyItemRangeInserted(startPosition, newItems.size());
                            currentPage++;
                        } else {
                            // 如果没有数据，也认为没有更多数据了
                            hasMore = false;
                        }

                        isLoading = false;

                        if(loadMoreListener != null) {
                            loadMoreListener.onLoadComplete(newItems != null ? newItems : new ArrayList<>());
                        }
                    } catch (Exception e) {
                        isLoading = false;
                        Log.e("FlexibleAdapter", "UI update error: " + e.getMessage());
                    }
                });

            } catch (Exception e){
                activity.runOnUiThread(()->{
                    isLoading=false;
                    if(loadMoreListener!=null) {
                        loadMoreListener.onLoadError(e.getMessage());
                    }
                    Log.e("FlexibleAdapter", "loadNextPage error: " + e.getMessage());
                });
            }
        }).start();
    }

    public void refreshData() {
        currentPage=0;
        isLoading=false;
        hasMore=true;

        items.clear();
        notifyDataSetChanged();

        loadNextPage();
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
    // 修正方法名拼写
    public void addNewSampleItem() {
        FeedItem newItem = new FeedItem(
                "新项目 " + System.currentTimeMillis(),
                "这是通过菜单添加的新项目内容",
                "new_url",
                500, 400,LayoutMode.single
        );
        addItem(newItem);
    }


    public void delItem(int position)
    {
        FeedItem item=items.get(position);
        dbHelper.deleteFeedItem(item.getId());

        items.remove(position);
        notifyItemRemoved(position);
    }

    public void delData()
    {
        dbHelper.deleteData();

        items.clear();
        notifyDataSetChanged();
    }


    public void updateData(List<FeedItem> newItems)
    {
        dbHelper.updateAll(newItems);
        items=(ArrayList<FeedItem>) newItems;
        notifyDataSetChanged();
    }

    public void updateItem(int position, FeedItem newItem)
    {
        // 参数检查
        if (position < 0 || position >= items.size()) {
            Log.e("FlexibleAdapter", "updateItem: 位置越界 - " + position);
            return;
        }

        if (newItem == null) {
            Log.e("FlexibleAdapter", "updateItem: newItem 为 null");
            return;
        }

        // 更新数据库
        int rowsAffected = dbHelper.updateFeedItem(newItem);
        if (rowsAffected > 0) {
            // 更新内存中的列表
            items.set(position, newItem);
            // 通知适配器该位置的数据发生变化
            notifyItemChanged(position);
        } else {
            Log.e("FlexibleAdapter", "updateItem: 数据库更新失败");
        }
    }

    public List<FeedItem> getAllFeedItems() {
        return items;
    }
    public FeedItem getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
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

