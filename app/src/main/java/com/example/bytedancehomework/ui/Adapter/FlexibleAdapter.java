// FlexibleAdapter.java
package com.example.bytedancehomework.ui.Adapter;

import android.app.Activity;
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
import com.example.bytedancehomework.Enum.MediaType;
import com.example.bytedancehomework.data.DBHelper.DatabaseHelper;
import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.data.Item.FeedItem;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.manager.VideoPlayManager;

import java.util.ArrayList;
import java.util.List;

public class FlexibleAdapter extends RecyclerView.Adapter<BaseViewHolder> {
    // 常量
    private static final int VIEW_TYPE_SINGLE_IMAGE = 0;
    private static final int VIEW_TYPE_SINGLE_VIDEO = 1;
    private static final int VIEW_TYPE_GRID_IMAGE = 2;
    private static final int VIEW_TYPE_GRID_VIDEO = 3;
    private static final int VIEW_TYPE_STAGGERED_IMAGE = 4;
    private static final int VIEW_TYPE_STAGGERED_VIDEO = 5;

    private final int PAGE_SIZE=5;

    // 成员变量
    private List<FeedItem> items;
    private DatabaseHelper dbHelper;
    private LayoutMode layoutMode;
    private Activity activity;
    private VideoPlayManager videoPlayManager;

    private int currentPage = 0;
    private boolean isLoading = false;
    private boolean hasMore = true;

    // 监听器
    private OnItemClickListener itemClickListener;
    private OnLoadMoreListener loadMoreListener;
    private OnShowLoadMoreButtonListener showLoadMoreButtonListener;

    // ==================== 接口定义 ====================

    public interface OnItemClickListener {
        void onItemClick(FeedItem item);
        void onItemLongClick(FeedItem item);
    }

    public interface OnLoadMoreListener {
        void onLoadMoreStarted();
        void onLoadComplete(List<FeedItem> newItems);
        void onLoadError(String error);
    }

    public interface OnShowLoadMoreButtonListener {
        void onShouldShowLoadMoreButton();
        void onShouldHideLoadMoreButton();
    }

    // ==================== 构造方法 ====================

    public FlexibleAdapter(Activity activity, List<FeedItem> items, LayoutMode layoutMode, DatabaseHelper dbHelper,VideoPlayManager videoPlayManager) {
        this.activity = activity;
        this.items = items;
        this.layoutMode = layoutMode;
        this.dbHelper = dbHelper;
        this.videoPlayManager=videoPlayManager;
    }

    // ==================== 监听器设置方法 ====================

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.itemClickListener = listener;
    }

    public void setOnLoadMoreListener(OnLoadMoreListener listener) {
        this.loadMoreListener = listener;
    }

    public void setOnShowLoadMoreButtonListener(OnShowLoadMoreButtonListener listener) {
        this.showLoadMoreButtonListener = listener;
    }

    // ==================== RecyclerView.Adapter方法 ====================

    @Override
    public int getItemViewType(int position) {
        FeedItem item = items.get(position);

        // 组合 layoutMode 和 mediaType 生成唯一的视图类型
        if (item.getMediaType() == MediaType.image) {
            switch (item.getLayoutMode()) {
                case single:
                    return VIEW_TYPE_SINGLE_IMAGE;
                case grid:
                    return VIEW_TYPE_GRID_IMAGE;
                case staggered:
                    return VIEW_TYPE_STAGGERED_IMAGE;
            }
        } else { // VIDEO
            switch (item.getLayoutMode()) {
                case single:
                    return VIEW_TYPE_SINGLE_VIDEO;
                case grid:
                    return VIEW_TYPE_GRID_VIDEO;
                case staggered:
                    return VIEW_TYPE_STAGGERED_VIDEO;
            }
        }
        return VIEW_TYPE_SINGLE_IMAGE; // 默认
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        switch (viewType) {
            case VIEW_TYPE_SINGLE_IMAGE:
                View singleImageView = inflater.inflate(R.layout.layout_feed_item_single, parent, false);
                return new ImageViewHolder(singleImageView);

            case VIEW_TYPE_SINGLE_VIDEO:
                View singleVideoView = inflater.inflate(R.layout.layout_video_feed_item_single, parent, false);
                return new VideoViewHolder(singleVideoView, videoPlayManager);

            case VIEW_TYPE_GRID_IMAGE:
                View gridImageView = inflater.inflate(R.layout.layout_feed_item_grid, parent, false);
                return new ImageViewHolder(gridImageView);

            case VIEW_TYPE_GRID_VIDEO:
                View gridVideoView = inflater.inflate(R.layout.layout_video_feed_item_grid, parent, false);
                return new VideoViewHolder(gridVideoView, videoPlayManager);

            case VIEW_TYPE_STAGGERED_IMAGE:
                View staggeredImageView = inflater.inflate(R.layout.layout_feed_item_staggered, parent, false);
                return new ImageViewHolder(staggeredImageView);

            default:
                View defaultView = inflater.inflate(R.layout.layout_feed_item_single, parent, false);
                return new ImageViewHolder(defaultView);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        FeedItem item = items.get(position);
        holder.bind(item);

        setupLoadMoreButtonVisibility(position);
        setupItemClickListeners(holder, item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    // ==================== 数据操作方法 ====================

    public void addItem(FeedItem item) {
        long id = dbHelper.insertFeedItem(item);
        item.setId(id);
        items.add(0, item);
        notifyItemInserted(0);
    }

    public void addNewSampleItem() {
        List<FeedItem> sampleItems = new ArrayList<>();
        // 单列视频
        sampleItems.add(new FeedItem("单列视频标题",
                "这是单列布局的视频内容描述",
                "android.resource://" + activity.getPackageName() + "/" + R.raw.test_video_short,
                "android.resource://" + activity.getPackageName()+'/'+R.drawable.video_cover,
                1920, 1080, 120000, LayoutMode.single));

        // 网格视频
        sampleItems.add(new FeedItem("网格视频标题",
                "这是网格布局的视频内容",
                "android.resource://" + activity.getPackageName() + "/" + R.raw.test_video_short,
                "android.resource://" + activity.getPackageName()+'/'+R.drawable.video_cover,
                1280, 720, 90000, LayoutMode.grid));

        for (FeedItem item : sampleItems) {
            dbHelper.insertFeedItem(item);
        }
    }

    public void delItem(int position) {
        FeedItem item = items.get(position);
        dbHelper.deleteFeedItem(item.getId());

        items.remove(position);
        notifyItemRemoved(position);
    }

    public void delData() {
        dbHelper.deleteData();
        items.clear();
        notifyDataSetChanged();
    }

    public void updateData(List<FeedItem> newItems) {
        dbHelper.updateAll(newItems);
        items = (ArrayList<FeedItem>) newItems;
        notifyDataSetChanged();
    }

    public void updateItem(int position, FeedItem newItem) {
        if (position < 0 || position >= items.size()) {
            Log.e("FlexibleAdapter", "updateItem: 位置越界 - " + position);
            return;
        }

        if (newItem == null) {
            Log.e("FlexibleAdapter", "updateItem: newItem 为 null");
            return;
        }

        int rowsAffected = dbHelper.updateFeedItem(newItem);
        if (rowsAffected > 0) {
            items.set(position, newItem);
            notifyItemChanged(position);
        } else {
            Log.e("FlexibleAdapter", "updateItem: 数据库更新失败");
        }
    }

    // ==================== 分页加载方法 ====================

    public void loadNextPage() {
        if (isLoading || !hasMore) return;

        isLoading = true;

        if (loadMoreListener != null) {
            loadMoreListener.onLoadMoreStarted();
        }

        new Thread(() -> {
            try {
                List<FeedItem> newItems = dbHelper.getFeedItemsByPage(currentPage, PAGE_SIZE);
                handleLoadResult(newItems);
            } catch (Exception e) {
                handleLoadError(e);
            }
        }).start();
    }

    public void refreshData() {
        currentPage = 0;
        isLoading = false;
        hasMore = true;

        items.clear();
        notifyDataSetChanged();
        loadNextPage();
    }

    // ==================== 布局模式方法 ====================

    public void switchLayoutMode(LayoutMode layoutMode) {
        if (layoutMode != this.layoutMode) {
            this.layoutMode = layoutMode;
            notifyDataSetChanged();
        }
    }

    // ==================== 获取方法 ====================

    public List<FeedItem> getAllFeedItems() {
        return items;
    }

    public FeedItem getItemAt(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    public int getPosition(FeedItem item) {
        return items.indexOf(item);
    }

    public LayoutMode getLayoutMode() {
        return layoutMode;
    }

    public boolean isLoading() {
        return isLoading;
    }

    public boolean hasMore() {
        return hasMore;
    }

    // ==================== 私有方法 ====================

    private void setupLoadMoreButtonVisibility(int position) {
        if (position >= getItemCount() - 2 && hasMore && !isLoading && showLoadMoreButtonListener != null) {
            activity.runOnUiThread(() -> {
                Log.d("Adapter", "onBindViewHolder: 显示加载更多按钮");
                showLoadMoreButtonListener.onShouldShowLoadMoreButton();
            });
        }

        if (position < getItemCount() - 2 && showLoadMoreButtonListener != null) {
            activity.runOnUiThread(() -> {
                Log.d("Adapter", "onBindViewHolder: 隐藏加载更多按钮");
                showLoadMoreButtonListener.onShouldHideLoadMoreButton();
            });
        }
    }

    private void setupItemClickListeners(BaseViewHolder holder, FeedItem item) {
        holder.itemView.setOnClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemClick(item);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (itemClickListener != null) {
                itemClickListener.onItemLongClick(item);
            }
            return false;
        });
    }

    private void handleLoadResult(List<FeedItem> newItems) {
        activity.runOnUiThread(() -> {
            try {
                if (newItems != null && !newItems.isEmpty()) {
                    if (newItems.size() < PAGE_SIZE) {
                        hasMore = false;
                    }

                    int startPosition = items.size();
                    items.addAll(newItems);
                    notifyItemRangeInserted(startPosition, newItems.size());
                    currentPage++;
                } else {
                    hasMore = false;
                }

                isLoading = false;

                if (loadMoreListener != null) {
                    loadMoreListener.onLoadComplete(newItems != null ? newItems : new ArrayList<>());
                }
            } catch (Exception e) {
                isLoading = false;
                Log.e("FlexibleAdapter", "UI update error: " + e.getMessage());
            }
        });
    }

    private void handleLoadError(Exception e) {
        activity.runOnUiThread(() -> {
            isLoading = false;
            if (loadMoreListener != null) {
                loadMoreListener.onLoadError(e.getMessage());
            }
            Log.e("FlexibleAdapter", "loadNextPage error: " + e.getMessage());
        });
    }


}