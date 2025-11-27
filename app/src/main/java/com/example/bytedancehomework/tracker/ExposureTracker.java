package com.example.bytedancehomework.tracker;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancehomework.Adapter.FlexibleAdapter;
import com.example.bytedancehomework.Item.FeedItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExposureTracker {
    private Map<Long,Integer> itemExposureStage=new HashMap<>();
    private long lastCheckTime=0;
    private static final long CHECK_INTERVAL=50;

    private RecyclerView recyclerView;
    private FlexibleAdapter adapter;
    private RecyclerView.AdapterDataObserver dataObserver;
    private boolean isTracking = false;

    public void startTrack(RecyclerView recyclerView, FlexibleAdapter adapter)
    {
        isTracking=true;
        this.recyclerView=recyclerView;
        this.adapter=adapter;

        registerDataObserver();

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                long currentCheckTime=System.currentTimeMillis();
                if(currentCheckTime-lastCheckTime>=CHECK_INTERVAL)
                {
                    super.onScrolled(recyclerView, dx, dy);
                    checkAllVisibleItems();
                    lastCheckTime=currentCheckTime;
                }
            }
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState==RecyclerView.SCROLL_STATE_IDLE)
                {
                    checkAllVisibleItems();
                }
            }
        });
        recyclerView.post(()->checkAllVisibleItems());
    }

    public void stopTrack() {
        if (!isTracking) return;

        isTracking = false;

        // 移除数据监听器
        if (adapter != null && dataObserver != null) {
            adapter.unregisterAdapterDataObserver(dataObserver);
            dataObserver = null;
        }

        // 移除滚动监听
        if (recyclerView != null) {
            recyclerView.clearOnScrollListeners();
        }

        // 清理状态
        itemExposureStage.clear();
        recyclerView = null;
        adapter = null;
    }

    private void registerDataObserver() {
        if(adapter==null)
            return;

        dataObserver=new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                // 新数据插入时重新检查曝光
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onItemRangeRemoved(int positionStart, int itemCount) {
                // 数据删除时重新检查曝光
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                // 数据变化时重新检查曝光
                recyclerView.post(() -> checkAllVisibleItems());
            }

            @Override
            public void onChanged() {
                // 数据全部更新时重新检查曝光
                recyclerView.post(() -> checkAllVisibleItems());
            }
        };

        adapter.registerAdapterDataObserver(dataObserver);
    }

    private void checkAllVisibleItems() {
        if(!isTracking || recyclerView==null || adapter==null)
            return;

        List<FeedItem> items=adapter.getAllFeedItems();
        if (items == null || items.isEmpty()) {
            return;
        }

        LinearLayoutManager linearLayoutManager =(LinearLayoutManager) recyclerView.getLayoutManager();
        if(linearLayoutManager==null)   return;

        int firstVisibleItemPosition=linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition=linearLayoutManager.findLastVisibleItemPosition();

        if (firstVisibleItemPosition == RecyclerView.NO_POSITION ||
                lastVisibleItemPosition == RecyclerView.NO_POSITION) {
            return;
        }

        firstVisibleItemPosition = Math.max(0, firstVisibleItemPosition);
        lastVisibleItemPosition = Math.min(items.size() - 1, lastVisibleItemPosition);

        for(int i=firstVisibleItemPosition;i<=lastVisibleItemPosition;i++)
        {
            View itemView = linearLayoutManager.findViewByPosition(i);

            if(itemView!=null)
            {

                if (i != RecyclerView.NO_POSITION &&
                        i >= 0 &&
                        i < items.size()) {

                    FeedItem item = items.get(i);  // 使用有效的 adapterPosition
                    float ratio = calExposureRatio(recyclerView, itemView);
                    handleExposure(item, ratio);
                }
                else
                {
                    Log.e("outOfRange", "checkAllVisibleItems");
                }
            }
        }
    }

    private void handleExposure(FeedItem item, float ratio) {
        long id =item.getId();
        int currentStage = itemExposureStage.getOrDefault(id,-1);

        if(ratio>=0f&&currentStage==-1)
        {
            onExposure0(item);
            itemExposureStage.put(id,0);
        }
        else if(ratio>=0.3f&&currentStage==0)
        {
            onExposure30(item);
            itemExposureStage.put(id,1);
        }
        else if(ratio>=0.5f&&currentStage==1)
        {
            onExposure50(item);
            itemExposureStage.put(id,2);
        }
        else if(ratio>=1f&&currentStage==2)
        {
            onExposure100(item);
            itemExposureStage.put(id,3);
        }
    }

    private void onExposure100(FeedItem item) {
        String TAG="Exposure";
        Log.d(TAG, "onExposure100 "+item.getId());
    }

    private void onExposure50(FeedItem item) {
        String TAG="Exposure";
        Log.d(TAG, "onExposure50 "+item.getId());
    }

    private void onExposure30(FeedItem item) {
        String TAG="Exposure";
        Log.d(TAG, "onExposure30 "+item.getId());
    }

    private void onExposure0(FeedItem item) {
        String TAG="Exposure";
        Log.d(TAG, "onExposure0 "+item.getId());
    }

    private float calExposureRatio(RecyclerView recyclerView, View itemView) {
        Rect rect =new Rect();
        boolean isVisible = itemView.getGlobalVisibleRect(rect);
        if(!isVisible)
            return 0f;

        float screenHeight = getScreenHeight(itemView);

        float visibleTop=Math.max(0,rect.top);
        float visibleBottom=Math.min(screenHeight,rect.bottom);
        float visibleHeight=visibleBottom-visibleTop;

        float totalHeight=itemView.getHeight();

        if(totalHeight==0f)
            return 0f;

        float ratio=visibleHeight/totalHeight;
        return ratio;
    }


    private float getScreenHeight(View view) {
        DisplayMetrics metrics = view.getResources().getDisplayMetrics();
        return metrics.heightPixels;
    }

}
