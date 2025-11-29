// MainActivity.java
package com.example.bytedancehomework.ui.Activity;

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

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnLoadMoreListener,
        FlexibleAdapter.OnShowLoadMoreButtonListener,
        VideoPlayManager.PlaybackStateListener {

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

    // ==================== 生命周期方法 ====================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeComponents();
        setupUI();
        initializeData();
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
        adapter = new FlexibleAdapter(this, new ArrayList<>(), LayoutMode.single, dbHelper);
        videoPlayManager = VideoPlayManager.getInstance();
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
        sampleItems.add(new FeedItem("Android开发教程",
                "学习如何使用RecyclerView和SQLite创建强大的Android应用",
                "url1", 800, 600, LayoutMode.single));
        sampleItems.add(new FeedItem("Material Design",
                "Material Design是Google推出的设计语言，提供一致的用户体验",
                "url2", 600, 800, LayoutMode.grid));
        sampleItems.add(new FeedItem("Kotlin vs Java",
                "比较Kotlin和Java在Android开发中的优缺点",
                "url3", 400, 300, LayoutMode.grid));
        sampleItems.add(new FeedItem("数据库优化",
                "学习如何优化SQLite数据库查询性能",
                null, 800, 400, LayoutMode.grid));
        sampleItems.add(new FeedItem("UI/UX设计",
                "创建美观且易用的用户界面设计原则",
                null, 300, 500, LayoutMode.single));
        sampleItems.add(new FeedItem("性能调优",
                "提升Android应用性能的技巧和最佳实践",
                null, 700, 900, LayoutMode.single));

        for (FeedItem item : sampleItems) {
            dbHelper.insertFeedItem(item);
        }
    }

    private void addNewSampleItem() {
        adapter.addNewSampleItem();
        Toast.makeText(this, "已添加新项目", Toast.LENGTH_SHORT).show();
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
        if (position != -1) {
            item.setFavorite(!item.isFavorite());
            adapter.updateItem(position, item);

            String message = item.isFavorite() ? "已收藏" : "取消收藏";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
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

    }

    @Override
    public void onPlaybackPaused(FeedItem item, int currentPosition) {

    }

    @Override
    public void onPlaybackStopped(FeedItem item) {

    }

    @Override
    public void onPlaybackCompleted(FeedItem item) {

    }

    @Override
    public void onPlaybackError(FeedItem item, String error) {

    }

    // ==================== 资源清理方法 ====================

    private void cleanupResources() {
        if (exposureTracker != null) {
            exposureTracker.stopTrack();
        }

        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}