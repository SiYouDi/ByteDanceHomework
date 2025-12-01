// MainActivity.java
package com.example.bytedancehomework.ui.Activity;

import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bytedancehomework.ui.Adapter.FlexibleAdapter;
import com.example.bytedancehomework.data.DBHelper.DatabaseHelper;
import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.data.Item.FeedItem;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.manager.ExposureTracker;
import com.example.bytedancehomework.manager.VideoPlayManager;
import com.example.bytedancehomework.ui.Adapter.VideoViewHolder;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnLoadMoreListener,
        FlexibleAdapter.OnShowLoadMoreButtonListener,
        VideoPlayManager.PlaybackStateListener {

    private String TAG = "MainActivity";
    // UI组件
    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBarLoadMore;
    private Button lordMoreButton;

    // 业务组件
    private FlexibleAdapter adapter;
    private DatabaseHelper dbHelper;
    private ExposureTracker exposureTracker;
    private VideoPlayManager videoPlayManager;

    private int currentAutoPlayPosition=0;

    // ==================== 生命周期方法 ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        setupUI();
        initializeData();
        initVideoPlayManager();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(videoPlayManager!=null)
            videoPlayManager.pausePlayback();
    }

    @Override
    protected void onDestroy() {
        cleanupResources();
        super.onDestroy();
    }

    // ==================== 初始化方法 ====================

    private void initializeComponents() {
        progressBarLoadMore = findViewById(R.id.progressBarLoadMore);
        dbHelper = new DatabaseHelper(this);
        videoPlayManager = VideoPlayManager.getInstance();
        adapter = new FlexibleAdapter(this, new ArrayList<>(), LayoutMode.single, dbHelper,videoPlayManager);
    }

    private void setupUI() {
        setupAdapter();
        setupRecyclerView();
        setupSwipeRefreshLayout();
        setupMenuButton();
        setupLoadMoreButton();
        setupExposureTracker();
    }

    private void initializeData() {
        List<FeedItem> dbItems = dbHelper.getAllFeedItems();
        if (dbItems.size() == 0) {
            addSampleData();
        }
        adapter.refreshData();
    }

    public void initVideoPlayManager()
    {
        videoPlayManager.setDbHelper(dbHelper);
        videoPlayManager.setPlaybackStateListener(this);
    }

    // ==================== UI设置方法 ====================

    private void setupAdapter() {
        adapter.setOnItemClickListener(this);
        adapter.setOnLoadMoreListener(this);
        adapter.setOnShowLoadMoreButtonListener(this);
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.recycleViewFeed);
        recyclerView.setAdapter(adapter);
        setupMixedLayout();
    }

    private void setupSwipeRefreshLayout() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> refreshData());
    }

    private void setupMenuButton() {
        ImageButton menuButton = findViewById(R.id.buttonMenu);
        menuButton.setOnClickListener(this::showPopupMenu);
    }

    private void setupLoadMoreButton() {
        lordMoreButton = findViewById(R.id.buttonLoadMore);
        lordMoreButton.setOnClickListener(v -> {
            if (!adapter.isLoading() && adapter.hasMore()) {
                adapter.loadNextPage();
                lordMoreButton.setVisibility(View.GONE);
            }
        });
    }

    private void setupExposureTracker() {
        exposureTracker = new ExposureTracker();
        exposureTracker.startTrack(recyclerView, adapter);
    }

    // ==================== 布局模式方法 ====================

    private void switchToSingleMode() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter.switchLayoutMode(LayoutMode.single);
    }

    private void switchToGridMode() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(layoutManager);
        adapter.switchLayoutMode(LayoutMode.grid);
    }

    private void setupMixedLayout() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 2);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (adapter != null && position < adapter.getItemCount()) {
                    FeedItem item = adapter.getItemAt(position);
                    if (item != null) {
                        return item.getLayoutMode() == LayoutMode.single ? 2 : 1;
                    }
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
    }

    // ==================== 数据操作方法 ====================

    private void addSampleData() {
        List<FeedItem> sampleItems = new ArrayList<>();
        // 单列图片
        sampleItems.add(new FeedItem("单列图片标题",
                "这是单列布局的图片内容描述",
                "https://example.com/image1.jpg",
                800, 600, LayoutMode.single));

        // 单列视频
        sampleItems.add(new FeedItem("单列视频标题",
                "这是单列布局的视频内容描述",
                "android.resource://" + getPackageName() + "/" + R.raw.test_video_short,
                "android.resource://" + getPackageName()+'/'+R.drawable.video_cover,
                1920, 1080, 120000, LayoutMode.single));

        // 网格图片
        sampleItems.add(new FeedItem("网格图片标题",
                "这是网格布局的图片内容",
                "https://example.com/image2.jpg",
                400, 300, LayoutMode.grid));

        // 网格视频
        sampleItems.add(new FeedItem("网格视频标题",
                "这是网格布局的视频内容",
                "android.resource://" + getPackageName() + "/" + R.raw.test_video_short,
                "android.resource://" + getPackageName()+'/'+R.drawable.video_cover,
                1280, 720, 90000, LayoutMode.grid));

        for (FeedItem item : sampleItems) {
            dbHelper.insertFeedItem(item);
        }
    }

    private void addNewSampleItem() {
        adapter.addNewSampleItem();
        Toast.makeText(this, "已添加新项目", Toast.LENGTH_SHORT).show();
        adapter.refreshData();
    }

    private void clearAllData() {
        adapter.delData();
        Toast.makeText(this, "已清空所有数据", Toast.LENGTH_SHORT).show();
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        lordMoreButton.setVisibility(View.GONE);
        adapter.refreshData();
    }

    // ==================== 菜单相关方法 ====================

    public void showPopupMenu(View view) {
        PopupMenu menu = new PopupMenu(this, view);
        menu.getMenuInflater().inflate(R.menu.layout_menu, menu.getMenu());

        menu.getMenu().add(Menu.NONE, 100, Menu.NONE, "添加样例");
        menu.getMenu().add(Menu.NONE, 101, Menu.NONE, "清空数据");

        menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        menu.show();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_single_item) {
            switchToSingleMode();
            return true;
        } else if (id == R.id.menu_grid_item) {
            switchToGridMode();
            return true;
        } else if (id == R.id.menu_staggered_item) {
            Toast.makeText(this, "该功能暂未开放", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == 100) {
            addNewSampleItem();
            return true;
        } else if (id == 101) {
            clearAllData();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // ==================== 事件监听器实现 ====================

    @Override
    public void onItemClick(FeedItem item) {
        int position = adapter.getPosition(item);
        LayoutMode newLayoutMode=null;
        if (position != -1) {
            LayoutMode oldLayoutMode = item.getLayoutMode();
            if(oldLayoutMode==LayoutMode.single)
                newLayoutMode=LayoutMode.grid;
            else if (oldLayoutMode==LayoutMode.grid) {
                newLayoutMode=LayoutMode.single;
            }

            if(adapter!=null&&newLayoutMode!=null)
            {
                adapter.updateLayoutMode(position,newLayoutMode);
                Log.d(TAG, "onItemClick: "+position+" 的LayoutMode由"+oldLayoutMode+"改为"+item.getLayoutMode());
            }
        }
    }

    @Override
    public void onItemLongClick(FeedItem item) {
        int position = adapter.getPosition(item);
        if (position != -1) {
            adapter.delItem(position);
            Toast.makeText(this, "已删除项目", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLoadMoreStarted() {
        progressBarLoadMore.setVisibility(View.VISIBLE);
        Log.d("MainActivity", "onLoadMoreStarted: ");
    }

    @Override
    public void onLoadComplete(List<FeedItem> newItems) {
        swipeRefreshLayout.setRefreshing(false);
        progressBarLoadMore.setVisibility(View.GONE);
        Log.d("MainActivity", "加载完成，新增 " + newItems.size() + " 条数据");

        // 数据加载完成后，等待 RecyclerView 布局完成再自动播放
        recyclerView.post(() -> {
            if (adapter.getItemCount() > 0) {
                autoPlayVideo();
            }
        });
    }

    @Override
    public void onLoadError(String error) {
        Log.e("MainActivity", "加载出错");
    }

    @Override
    public void onShouldShowLoadMoreButton() {
        runOnUiThread(() -> {
            if (lordMoreButton != null && adapter != null && adapter.hasMore() && !adapter.isLoading()) {
                lordMoreButton.setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void onShouldHideLoadMoreButton() {
        runOnUiThread(() -> {
            if (lordMoreButton != null) {
                lordMoreButton.setVisibility(View.GONE);
            }
        });
    }

    // ==================== 视频监听器实现 ====================
    @Override
    public void onPlaybackStarted(FeedItem item) {
        Log.d("MainActivity", "视频开始播放: " + item.getTitle());
    }

    @Override
    public void onPlaybackPaused(FeedItem item, int currentPosition) {
        int position =adapter.getPosition(item);
        if(position==-1)    return;
        RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
        if(viewHolder!=null)
        {
            if(viewHolder instanceof VideoViewHolder)
            {
                ((VideoViewHolder) viewHolder).showPlayState();
            }
        }
    }

    @Override
    public void onPlaybackStopped(FeedItem item) {

    }

    @Override
    public void onPlaybackCompleted(FeedItem item) {
        runOnUiThread(() -> {
            if (item != null) {
                Toast.makeText(this, "播放完成: " + item.getTitle(), Toast.LENGTH_SHORT).show();
                Log.d("MainActivity", "播放完成");

                recyclerView.postDelayed(() -> {
                    autoPlayNextVideo(item);
                }, 300); // 300ms 延迟
            } else {
                Toast.makeText(this, "播放完成", Toast.LENGTH_SHORT).show();
                Log.w("MainActivity", "onPlaybackCompleted: item is null");
            }

            int position =adapter.getPosition(item);
            if(position==-1)    return;
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(position);
            if(viewHolder!=null)
            {
                if(viewHolder instanceof VideoViewHolder)
                {
                    ((VideoViewHolder) viewHolder).showPlayState();
                }
            }
        });
    }

    @Override
    public void onPlaybackError(FeedItem item, String error) {
        runOnUiThread(() -> {
            String itemTitle = item != null ? item.getTitle() : "未知视频";
            Toast.makeText(this, "播放失败: " + error, Toast.LENGTH_SHORT).show();
            Log.e("MainActivity", "视频播放错误 - " + itemTitle + ": " + error);
        });
    }

    private void autoPlayVideo()
    {
        String TAG="MainActivity";
        Log.d("MainActivity", "autoPlayVideo: 进入");
        for(int i=currentAutoPlayPosition;i<adapter.getItemCount();i++)
        {
//            Log.d(TAG, "autoPlayVideo: "+i);
            RecyclerView.ViewHolder viewHolder = recyclerView.findViewHolderForAdapterPosition(i);
            if(viewHolder!=null)
            {
                FeedItem nextItem=adapter.getItemAt(i);
//                Log.d("MainActivity", "autoPlayVideo: viewHolder非空");
                if(nextItem!=null&&nextItem.isVideo())
                {
//                    Log.d("MainActivity", "autoPlayVideo: "+i+"为videoViewHolder");
                    if(viewHolder instanceof VideoViewHolder)
                    {
                        ((VideoViewHolder) viewHolder).playVideo();
                        currentAutoPlayPosition=i;
                        Log.d("MainActivity", "autoPlayVideo: 播放 "+currentAutoPlayPosition);
                        break;
                    }
                }
            }
        }
    }

    private void autoPlayNextVideo(FeedItem item) {
        Log.d("MainActivity", "进入autoPlayNextVideo");
        int currentPosition = adapter.getPosition(item);
        if(currentPosition==-1) return;

        for(int i=currentPosition+1;i<adapter.getItemCount();i++)
        {
            View nextView = recyclerView.getLayoutManager().findViewByPosition(i);
            if(nextView!=null)
            {
                FeedItem nextItem=adapter.getItemAt(i);
                if(nextItem!=null&&nextItem.isVideo())
                {
                    RecyclerView.ViewHolder viewHolder=recyclerView.findViewHolderForAdapterPosition(i);
                    if(viewHolder instanceof VideoViewHolder)
                    {
                        ((VideoViewHolder) viewHolder).playVideo();
                        currentAutoPlayPosition=i;
                        Log.d("MainActivity", "播放autoPlayNextVideo "+currentAutoPlayPosition);
                        break;
                    }
                }
            }
        }
    }

    // ==================== 资源清理方法 ====================

    private void cleanupResources() {
        if (exposureTracker != null) {
            exposureTracker.stopTrack();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }

        if(videoPlayManager!=null)
        {
            videoPlayManager.release();
        }
    }
}