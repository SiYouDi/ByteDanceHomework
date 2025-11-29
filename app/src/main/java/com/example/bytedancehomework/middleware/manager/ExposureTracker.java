// ExposureTracker.java
package com.example.bytedancehomework.middleware.manager;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancehomework.middleware.adapter.FlexibleAdapter;
import com.example.bytedancehomework.Item.FeedItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExposureTracker {
    // 常量
    private static final long CHECK_INTERVAL = 50;
    private static final String TAG = "Exposure";

    // 成员变量
    private Map<Long, Integer> itemExposureStage = new HashMap<>();
    private long lastCheckTime = 0;

    private RecyclerView recyclerView;
    private FlexibleAdapter adapter;
    private RecyclerView.AdapterDataObserver dataObserver;
    private boolean isTracking = false;

    // ==================== 公共方法 ====================

    public void startTrack(RecyclerView recyclerView, FlexibleAdapter adapter) {
        isTracking = true;
        this.recyclerView = recyclerView;
        this.adapter = adapter;

        registerDataObserver();
        setupScrollListener();
        recyclerView.post(() -> checkAllVisibleItems());
    }

    public void stopTrack() {
        if (!isTracking) return;

        isTracking = false;
        cleanupResources();
        clearState();
    }

    // ==================== 曝光处理相关方法 ====================

    private void handleExposure(FeedItem item, float ratio) {
        long id = item.getId();
        int currentStage = itemExposureStage.getOrDefault(id, -1);

        if (ratio >= 0f && currentStage == -1) {
            onExposure0(item);
            itemExposureStage.put(id, 0);
        } else if (ratio >= 0.3f && currentStage == 0) {
            onExposure30(item);
            itemExposureStage.put(id, 1);
        } else if (ratio >= 0.5f && currentStage == 1) {
            onExposure50(item);
            itemExposureStage.put(id, 2);
        } else if (ratio >= 1f && currentStage == 2) {
            onExposure100(item);
            itemExposureStage.put(id, 3);
        }
    }

    private void onExposure0(FeedItem item) {
        Log.d(TAG, "onExposure0 " + item.getId());
    }

    private void onExposure30(FeedItem item) {
        Log.d(TAG, "onExposure30 " + item.getId());
    }

    private void onExposure50(FeedItem item) {
        Log.d(TAG, "onExposure50 " + item.getId());
    }

    private void onExposure100(FeedItem item) {
        Log.d(TAG, "onExposure100 " + item.getId());
    }

    // ==================== 可见性检查相关方法 ====================

    private void checkAllVisibleItems() {
        if (!isTracking || recyclerView == null || adapter == null) return;

        List<FeedItem> items = adapter.getAllFeedItems();
        if (items == null || items.isEmpty()) return;

        LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
        if (linearLayoutManager == null) return;

        int firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION ||
                lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        firstVisibleItemPosition = Math.max(0, firstVisibleItemPosition);
        lastVisibleItemPosition = Math.min(items.size() - 1, lastVisibleItemPosition);

        for (int i = firstVisibleItemPosition; i <= lastVisibleItemPosition; i++) {
            checkItemVisibility(linearLayoutManager, items, i);
        }
    }

    private void checkItemVisibility(LinearLayoutManager layoutManager, List<FeedItem> items, int position) {
        View itemView = layoutManager.findViewByPosition(position);

        if (itemView != null && position >= 0 && position < items.size()) {
            FeedItem item = items.get(position);
            float ratio = calExposureRatio(recyclerView, itemView);
            handleExposure(item, ratio);
        } else {
            Log.e("outOfRange", "checkAllVisibleItems");
        }
    }

    // ==================== 工具方法 ====================

    private float calExposureRatio(RecyclerView recyclerView, View itemView) {
        Rect rect = new Rect();
        boolean isVisible = itemView.getGlobalVisibleRect(rect);
        if (!isVisible) return 0f;

        float screenHeight = getScreenHeight(itemView);
        float visibleTop = Math.max(0, rect.top);
        float visibleBottom = Math.min(screenHeight, rect.bottom);
        float visibleHeight = visibleBottom - visibleTop;
        float totalHeight = itemView.getHeight();

        if (totalHeight == 0f) return 0f;

        return visibleHeight / totalHeight;
    }

    private float getScreenHeight(View view) {
        DisplayMetrics metrics = view.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

    // ==================== 监听器设置相关方法 ====================

    private void registerDataObserver() {
        if (adapter == null) return;

        dataObserver = new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onChanged() {
                recyclerView.post(() -> checkAllVisibleItems());
            }
        };

        adapter.registerAdapterDataObserver(dataObserver);
    }

    private void setupScrollListener() {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                long currentCheckTime = System.currentTimeMillis();
                if (currentCheckTime - lastCheckTime >= CHECK_INTERVAL) {
                    super.onScrolled(recyclerView, dx, dy);
                    checkAllVisibleItems();
                    lastCheckTime = currentCheckTime;
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    checkAllVisibleItems();
                }
            }
        });
    }

    // ==================== 资源清理方法 ====================

    private void cleanupResources() {
        if (adapter != null && dataObserver != null) {
            adapter.unregisterAdapterDataObserver(dataObserver);
            dataObserver = null;
        }

        if (recyclerView != null) {
            recyclerView.clearOnScrollListeners();
        }
    }

    private void clearState() {
        itemExposureStage.clear();
        recyclerView = null;
        adapter = null;
    }
}