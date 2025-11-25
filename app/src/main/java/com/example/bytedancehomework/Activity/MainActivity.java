package com.example.bytedancehomework.Activity;

import android.os.Bundle;

import com.example.bytedancehomework.DBHelper.DatabaseHelper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.Item.FeedItem;
import com.example.bytedancehomework.Adapter.FlexibleAdapter;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.databinding.ActivityMainBinding;
import com.example.bytedancehomework.tracker.ExposureTracker;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity
implements FlexibleAdapter.OnItemClickListener
{

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private RecyclerView recyclerView;
    private FlexibleAdapter adapter;
    private DatabaseHelper dbhelper;
    private List<FeedItem> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbhelper=new DatabaseHelper(this);

        InitDate();
        initRecycleView();
    }

    private void InitDate()
    {
        items=dbhelper.getAllFeedItems();

        if(items.size()==0)
        {
            addSampleDate();
            items=dbhelper.getAllFeedItems();
        }
    }

    private void addSampleDate()
    {
        List<FeedItem> sampleItems = new ArrayList<>();
        sampleItems.add(new FeedItem("Android开发教程",
                "学习如何使用RecyclerView和SQLite创建强大的Android应用",
                "url1", 800, 600));
        sampleItems.add(new FeedItem("Material Design",
                "Material Design是Google推出的设计语言，提供一致的用户体验",
                "url2", 600, 800));
        sampleItems.add(new FeedItem("Kotlin vs Java",
                "比较Kotlin和Java在Android开发中的优缺点",
                "url3", 400, 300));
        sampleItems.add(new FeedItem("数据库优化",
                "学习如何优化SQLite数据库查询性能",
                null, 800, 400));
        sampleItems.add(new FeedItem("UI/UX设计",
                "创建美观且易用的用户界面设计原则",
                null, 300, 500));
        sampleItems.add(new FeedItem("性能调优",
                "提升Android应用性能的技巧和最佳实践",
                null, 700, 900));

        for (FeedItem item : sampleItems) {
            dbhelper.insertFeedItem(item);
        }
    }

    /*
       添加一个新的示例项目
     */
    private void addNewSampleItem() {
        FeedItem newItem = new FeedItem(
                "新项目 " + System.currentTimeMillis(),
                "这是通过菜单添加的新项目内容",
                "new_url",
                500, 400
        );
        adapter.addItem(newItem);
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

        switchToSingleMode();
//        switchToGridMode();

        adapter.setOnItemClickListener(this);

        ExposureTracker exposureTracker    =new ExposureTracker();
        exposureTracker.startTrack(recyclerView,items);
    }

    //瀑布屏暂时不做
    private void switchToSingleMode() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if(adapter==null)
        {
            adapter=new FlexibleAdapter(items, LayoutMode.single,dbhelper);
            recyclerView.setAdapter(adapter);
        }
        else
        {
            adapter.switchLayoutMode(LayoutMode.single);
        }
    }
    private void switchToGridMode(){
        GridLayoutManager layoutManager = new GridLayoutManager(this,2);
        recyclerView.setLayoutManager(layoutManager);
        if(adapter==null)
        {
            adapter = new FlexibleAdapter(items,LayoutMode.grid,dbhelper);
            recyclerView.setAdapter(adapter);
        }
        else
        {
            adapter.switchLayoutMode(LayoutMode.grid);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.layout_menu,menu);

        menu.add(Menu.NONE,100,Menu.NONE,"添加样例");
        menu.add(Menu.NONE,101,Menu.NONE,"清空数据");

        return true;
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

    @Override
    public void onItemClick(FeedItem item) {
        int position=items.indexOf(item);
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
        int position = items.indexOf(item);
        if (position != -1) {
            adapter.delItem(position);
            Toast.makeText(this, "已删除项目", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(dbhelper!=null)
        {
            dbhelper.close();
        }
    }
}