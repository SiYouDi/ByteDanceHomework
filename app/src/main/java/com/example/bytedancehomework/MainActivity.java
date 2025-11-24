package com.example.bytedancehomework;

import android.annotation.SuppressLint;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytedancehomework.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
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

        InitDateBase();

    }

    private void InitDateBase()
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


    private void initRecycleView()
    {
        recyclerView = findViewById(R.id.recycleViewFeed);

        switchToSingleMode();

        adapter.setOnItemClickListener(this);
    }

    //瀑布屏暂时不做
    private void switchToSingleMode() {
        LinearLayoutManager layoutManager=new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        if(adapter==null)
        {
            adapter=new FlexibleAdapter(items,LayoutMode.single,dbhelper);
            recyclerView.setAdapter(adapter);
        }
        else
        {
            adapter.setLayoutMode(LayoutMode.single);
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
            adapter.setLayoutMode(LayoutMode.grid);
        }
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