package com.example.bytedancehomework.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.bytedancehomework.Item.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    //数据库信息
    private static final String DATABASE_NAME = "feed_app.db";
    private static final int DATABASE_VERSION = 1;

    // 表名和列名
    public static final String TABLE_FEED_ITEMS = "feed_items";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_CONTENT = "content";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_IMAGE_WIDTH = "image_width";
    public static final String COLUMN_IMAGE_HEIGHT = "image_height";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_IS_FAVORITE = "is_favorite";

    //创建表的信息
    private static final String CREATE_TABLE_FEED_ITEMS =
            "CREATE TABLE " + TABLE_FEED_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_IMAGE_WIDTH + " INTEGER DEFAULT 0, " +
                    COLUMN_IMAGE_HEIGHT + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0" +
                    ");";

    public DatabaseHelper(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //第一次调用数据库是创建
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(CREATE_TABLE_FEED_ITEMS);
    }

    //版本升级功能较为简单：删库重建。后续考虑升级
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL("DROP TABLE IF EXISTS "+TABLE_FEED_ITEMS);
        onCreate(db);
    }

    /*
    插入新的FeedItem到数据库
    @param item:要插入的新数据
    @return 返回新插入数据在数据库中的ID，插入失败则返回-1
     */
    public long insertFeedItem(FeedItem item)
    {
        SQLiteDatabase db=this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE,item.getTitle());
        values.put(COLUMN_CONTENT, item.getContent());
        values.put(COLUMN_IMAGE_URL, item.getImageUrl());
        values.put(COLUMN_IMAGE_WIDTH, item.getImageWidth());
        values.put(COLUMN_IMAGE_HEIGHT, item.getImageHeight());
        values.put(COLUMN_CREATED_AT, item.getCreatedAt());
        values.put(COLUMN_IS_FAVORITE, item.getIsFavorite());

        long id = db.insert(TABLE_FEED_ITEMS,null,values);
        db.close();

        return id;
    }

    /*
    删除指定id的元素
    @param 要删除元素的id
    @return 被删除的行数，0表示没有找到匹配的记录
     */
    public int deleteFeedItem(long id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int count= db.delete(TABLE_FEED_ITEMS,selection,selectionArgs);
        db.close();

        return count;
    }

    /*
    删除所有元素
    @return 被删除的行数，0表示没有找到匹配的记录
     */
    public int deleteData()
    {
        SQLiteDatabase db =this.getWritableDatabase();

        int count=db.delete(TABLE_FEED_ITEMS,null,null);
        db.close();

        return count;
    }

    /*

    修改对应的FeedItem
    @param item:修改后的FeedItem
    @return 被修改的行数，0表示没有找到匹配的记录
     */
    public int updateFeedItem(FeedItem item)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_CONTENT, item.getContent());
        values.put(COLUMN_IMAGE_URL, item.getImageUrl());
        values.put(COLUMN_IMAGE_WIDTH, item.getImageWidth());
        values.put(COLUMN_IMAGE_HEIGHT, item.getImageHeight());
        values.put(COLUMN_CREATED_AT, item.getCreatedAt());
        values.put(COLUMN_IS_FAVORITE, item.getIsFavorite());

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};
        int count = db.update(TABLE_FEED_ITEMS,values,selection,selectionArgs);

        return count;
    }

    public boolean updateAll(List<FeedItem> items)
    {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();

            // 1. 清空旧数据
            db.delete(TABLE_FEED_ITEMS, null, null);

            // 2. 插入新数据
            for (FeedItem item : items) {
                ContentValues values = new ContentValues();

                values.put(COLUMN_TITLE, item.getTitle());
                values.put(COLUMN_CONTENT, item.getContent());
                values.put(COLUMN_IMAGE_URL, item.getImageUrl());
                values.put(COLUMN_IMAGE_WIDTH, item.getImageWidth());
                values.put(COLUMN_IMAGE_HEIGHT, item.getImageHeight());
                values.put(COLUMN_CREATED_AT, item.getCreatedAt());
                values.put(COLUMN_IS_FAVORITE, item.getIsFavorite());

                db.insert(TABLE_FEED_ITEMS, null, values);
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e("update错误", "updateAll: ");
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    /*
    通过id查找对应的FeedItem并返回
    @param id:想要查找的FeedItem的id
    @return id对应的FeedItem，null表示没有匹配的记录
     */
    public FeedItem getFeedItemById(long id)
    {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns={
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_CONTENT,
                COLUMN_IMAGE_URL,
                COLUMN_IMAGE_WIDTH,
                COLUMN_IMAGE_HEIGHT,
                COLUMN_CREATED_AT,
                COLUMN_IS_FAVORITE
        };

        String selection =COLUMN_ID+" = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor =db.query(TABLE_FEED_ITEMS,columns,selection,selectionArgs,null,null,null);

        FeedItem item=null;
        if(cursor!=null&&cursor.moveToFirst())
        {
            try
            {
                item=new FeedItem();
                item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                item.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
                item.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
                item.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                item.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)));
            }
            catch (Exception e)
            {
                Log.e("SqlSearch", "getFeedItemById: "+e.toString() );
            }
            finally {
                cursor.close();
            }

        }
        db.close();

        return item;
    }

    /*
    返回数据库中所有FeedItem
    @return 数据库中所有FeedItem
     */
    public List<FeedItem> getAllFeedItems()
    {
        List<FeedItem> items= new ArrayList<FeedItem>();
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns={
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_CONTENT,
                COLUMN_IMAGE_URL,
                COLUMN_IMAGE_WIDTH,
                COLUMN_IMAGE_HEIGHT,
                COLUMN_CREATED_AT,
                COLUMN_IS_FAVORITE
        };

        Cursor cursor = db.query(TABLE_FEED_ITEMS,
                columns,
                null,null,null,null,
                COLUMN_CREATED_AT + " DESC");

        if(cursor!=null && cursor.moveToFirst())
        {
            try
            {
                do {
                    FeedItem item = new FeedItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                    item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                    item.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
                    item.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
                    item.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                    item.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)));

                    items.add(item);
                } while (cursor.moveToNext());
            }catch (Exception e){
                Log.e("SqlSearch", "getAllFeedItems: "+e.toString() );
            }
            finally {
                cursor.close();
            }
        }
        db.close();

        return items;
    }

    /*
    返回数据库中所有Favorite为1的FeedItem
    @return 数据库中所有Favorite为1的FeedItem
     */
    public List<FeedItem> getFavoriteFeedItems() {
        List<FeedItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] columns = {
                COLUMN_ID,
                COLUMN_TITLE,
                COLUMN_CONTENT,
                COLUMN_IMAGE_URL,
                COLUMN_IMAGE_WIDTH,
                COLUMN_IMAGE_HEIGHT,
                COLUMN_CREATED_AT,
                COLUMN_IS_FAVORITE
        };

        String selection = COLUMN_IS_FAVORITE + " = ?";
        String[] selectionArgs = { "1" };

        Cursor cursor = db.query(
                TABLE_FEED_ITEMS,
                columns,
                selection, selectionArgs, null, null,
                COLUMN_CREATED_AT + " DESC"
        );

        if(cursor!=null && cursor.moveToFirst())
        {
            try
            {
                do {
                    FeedItem item = new FeedItem();
                    item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                    item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
                    item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
                    item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
                    item.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
                    item.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
                    item.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
                    item.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)));

                    items.add(item);
                } while (cursor.moveToNext());
            }catch (Exception e){
                Log.e("SqlSearch", "getAllFeedItems: "+e.toString() );
            }
            finally {
                cursor.close();
            }
        }
        db.close();
        return items;
    }

}
