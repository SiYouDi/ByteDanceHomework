package com.example.bytedancehomework.tracker;

import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancehomework.Item.FeedItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExposureTracker {
    private Map<Long,Integer> itemExposureStage=new HashMap<>();
    private long lastCheckTime=0;
    private static final long CHECK_INTERVAL=50;

    public void startTrack(RecyclerView recyclerView, List<FeedItem> items)
    {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                long currentCheckTime=System.currentTimeMillis();
                if(currentCheckTime-lastCheckTime>=CHECK_INTERVAL)
                {
                    super.onScrolled(recyclerView, dx, dy);
                    checkAllVisibleItems(recyclerView,items);
                    lastCheckTime=currentCheckTime;
                }
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if(newState==RecyclerView.SCROLL_STATE_IDLE)
                {
                    checkAllVisibleItems(recyclerView,items);
                }
            }


        });

        recyclerView.post(()->checkAllVisibleItems(recyclerView,items));
    }
    private void checkAllVisibleItems(RecyclerView recyclerView, List<FeedItem> items) {
        LinearLayoutManager linearLayoutManager =(LinearLayoutManager) recyclerView.getLayoutManager();
        int firstVisibleItemPosition=linearLayoutManager.findFirstVisibleItemPosition();
        int lastVisibleItemPosition=linearLayoutManager.findLastVisibleItemPosition();

        for(int i=firstVisibleItemPosition;i<=lastVisibleItemPosition;i++)
        {
            View itemView = linearLayoutManager.findViewByPosition(i);

            if(itemView!=null)
            {
                RecyclerView.ViewHolder viewHolder = recyclerView.getChildViewHolder(itemView);
                int position = viewHolder.getAdapterPosition();

                FeedItem item = items.get(position);

                float ratio =calExposureRatio(recyclerView,itemView);
                handleExposure(item,ratio);
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
