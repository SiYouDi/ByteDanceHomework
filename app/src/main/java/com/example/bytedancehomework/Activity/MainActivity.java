package com.example.bytedancehomework.Activity;

import android.os.Bundle;

import com.example.bytedancehomework.DBHelper.DatabaseHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.Item.FeedItem;
import com.example.bytedancehomework.Adapter.FlexibleAdapter;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.databinding.ActivityMainBinding;
import com.example.bytedancehomework.tracker.ExposureTracker;

import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
implements FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnLoadMoreListener,
        FlexibleAdapter.OnShowLoadMoreButtonListener
{

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar progressBarLoadMore;
    private ExposureTracker exposureTracker;
    private Button lordMoreButton;

    private FlexibleAdapter adapter;
    private DatabaseHelper dbhelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        progressBarLoadMore=findViewById(R.id.progressBarLoadMore);

        dbhelper=new DatabaseHelper(this);

        initAdapter();
        initRecycleView();
        InitData();
    }

    private void initAdapter() {
        adapter=new FlexibleAdapter(this,new ArrayList<>(), LayoutMode.single,dbhelper);
        adapter.setOnItemClickListener(this);
        adapter.setOnLoadMoreListener(this);
        adapter.setOnShowLoadMoreButtonListener(this);
    }

    private void InitData()
    {
        List<FeedItem> dbItems = dbhelper.getAllFeedItems();
        if (dbItems.size() == 0) {
            addSampleData(); // 添加示例数据到数据库
        }
        // 让 adapter 加载第一页数据
        adapter.refreshData();
    }

    private void addSampleData()
    {
        List<FeedItem> sampleItems = new ArrayList<>();
        sampleItems.add(new FeedItem("Android开发教程",
                "学习如何使用RecyclerView和SQLite创建强大的Android应用",
                "url1", 800, 600,LayoutMode.single));
        sampleItems.add(new FeedItem("Material Design",
                "Material Design是Google推出的设计语言，提供一致的用户体验",
                "url2", 600, 800,LayoutMode.grid));
        sampleItems.add(new FeedItem("Kotlin vs Java",
                "比较Kotlin和Java在Android开发中的优缺点",
                "url3", 400, 300,LayoutMode.grid));
        sampleItems.add(new FeedItem("数据库优化",
                "学习如何优化SQLite数据库查询性能",
                null, 800, 400,LayoutMode.grid));
        sampleItems.add(new FeedItem("UI/UX设计",
                "创建美观且易用的用户界面设计原则",
                null, 300, 500,LayoutMode.single));
        sampleItems.add(new FeedItem("性能调优",
                "提升Android应用性能的技巧和最佳实践",
                null, 700, 900,LayoutMode.single));

        for (FeedItem item : sampleItems) {
            dbhelper.insertFeedItem(item);
        }
    }

    /*
       添加一个新的示例项目
     */
    private void addNewSampleItem() {
        adapter.addNewSampleItem();
        Toast.makeText(this, "已添加新项目", Toast.LENGTH_SHORT).show();
    }

    /*
      清空所有数据
     */
    private void clearAllData() {
        adapter.delData();
        Toast.makeText(this, "已清空所有数据", Toast.LENGTH_SHORT).show();
    }


    private void initRecycleView()
    {
        recyclerView = findViewById(R.id.recycleViewFeed);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        recyclerView.setAdapter(adapter);

        setupMixedLayout();

        //初始化监听器
        initMenuButton();
        initSwipeRefreshLayout();
        initExposureTracker();
        initLoarMoreButton();
    }

    private void initLoarMoreButton() {
        lordMoreButton =findViewById(R.id.buttonLoadMore);
        lordMoreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!adapter.isLoading()&&adapter.hasMore())
                {
                    adapter.loadNextPage();
//                    lordMoreButton.setVisibility(View.GONE);
                }

            }
        });
    }

    private void initExposureTracker() {
        exposureTracker = new ExposureTracker();
        exposureTracker.startTrack(recyclerView, adapter);
    }

    private void initSwipeRefreshLayout() {
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
            }

        });
    }

    private void initMenuButton() {
        ImageButton menuButton = findViewById(R.id.buttonMenu);
        menuButton.setOnClickListener(this::showPopupMenu);
    }

    private void refreshData() {
        swipeRefreshLayout.setRefreshing(true);
        lordMoreButton.setVisibility(View.GONE);
        adapter.refreshData();
    }

    //瀑布屏暂时不做
    private void switchToSingleMode() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        adapter.switchLayoutMode(LayoutMode.single);
    }
    private void switchToGridMode(){
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        adapter.switchLayoutMode(LayoutMode.grid);
    }

    private void setupMixedLayout()
    {
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);

        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if(adapter!=null&& position<adapter.getItemCount())
                {
                    FeedItem item =adapter.getItemAt(position);
                    if(item!=null)
                    {
                        return item.getLayoutMode()==LayoutMode.single?2:1;
                    }
                }
                return 1;
            }
        });
        recyclerView.setLayoutManager(layoutManager);
    }

    public void showPopupMenu(View view) {
        PopupMenu menu=new PopupMenu(this,view);
        MenuInflater menuInflater = menu.getMenuInflater();
        menuInflater.inflate(R.menu.layout_menu,menu.getMenu());

        menu.getMenu().add(Menu.NONE,100,Menu.NONE,"添加样例");
        menu.getMenu().add(Menu.NONE,101,Menu.NONE,"清空数据");

        menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        menu.show();
    }

    //之后可以把menu类封装起来
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

    @Override
    public void onItemClick(FeedItem item) {

        int position=adapter.getPosition(item);
        if(position!=-1)
        {
            item.setFavorite(!item.isFavorite());
            adapter.updateItem(position,item);

            String message=item.isFavorite()?"已收藏":"取消收藏";
            Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        if (exposureTracker != null) {
            exposureTracker.stopTrack();
        }

        if(dbhelper!=null)
        {
            dbhelper.close();
        }

        super.onDestroy();
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
        lordMoreButton.setVisibility(View.GONE);

        Log.d("MainActivity", "加载完成，新增 " + newItems.size() + " 条数据");
    }

    @Override
    public void onLoadError(String error) {

    }

    @Override
    public void onShouldShowLoadMoreButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (lordMoreButton != null && adapter != null && adapter.hasMore() && !adapter.isLoading()) {
                    lordMoreButton.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public void onShouldHideLoadMoreButton() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (lordMoreButton != null) {
                    lordMoreButton.setVisibility(View.GONE);
                }
            }
        });
    }
}