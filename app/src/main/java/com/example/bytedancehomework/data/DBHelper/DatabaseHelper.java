// DatabaseHelper.java
package com.example.bytedancehomework.data.DBHelper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.bytedancehomework.Enum.MediaType;
import com.example.bytedancehomework.data.Item.FeedItem;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    // 数据库信息
    private static final String DATABASE_NAME = "feed_app.db";
    private static final int DATABASE_VERSION = 2; // 版本升级，因为表结构变更

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
    public static final String COLUMN_LAYOUT_MODE = "layout_mode";

    // 新增的视频相关列
    public static final String COLUMN_VIDEO_URL = "video_url";
    public static final String COLUMN_VIDEO_COVER_URL = "video_cover_url";
    public static final String COLUMN_VIDEO_DURATION = "video_duration";
    public static final String COLUMN_LAST_PLAY_POSITION = "last_play_position";
    public static final String COLUMN_VIDEO_WIDTH = "video_width";
    public static final String COLUMN_VIDEO_HEIGHT = "video_height";
    public static final String COLUMN_MEDIA_TYPE = "media_type";

    // 创建表的SQL语句 - 更新版本
    private static final String CREATE_TABLE_FEED_ITEMS =
            "CREATE TABLE " + TABLE_FEED_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT NOT NULL, " +
                    COLUMN_CONTENT + " TEXT, " +
                    COLUMN_IMAGE_URL + " TEXT, " +
                    COLUMN_IMAGE_WIDTH + " INTEGER DEFAULT 0, " +
                    COLUMN_IMAGE_HEIGHT + " INTEGER DEFAULT 0, " +
                    COLUMN_CREATED_AT + " INTEGER NOT NULL, " +
                    COLUMN_IS_FAVORITE + " INTEGER DEFAULT 0, " +
                    COLUMN_LAYOUT_MODE + " INTEGER NOT NULL, " +
                    COLUMN_VIDEO_URL + " TEXT, " +
                    COLUMN_VIDEO_COVER_URL + " TEXT, " +
                    COLUMN_VIDEO_DURATION + " INTEGER DEFAULT 0, " +
                    COLUMN_LAST_PLAY_POSITION + " INTEGER DEFAULT 0, " +
                    COLUMN_VIDEO_WIDTH + " INTEGER DEFAULT 0, " +
                    COLUMN_VIDEO_HEIGHT + " INTEGER DEFAULT 0, " +
                    COLUMN_MEDIA_TYPE + " INTEGER DEFAULT 0" + // 0=IMAGE, 1=VIDEO
                    ");";

    // 查询列数组 - 更新包含所有列
    private static final String[] ALL_COLUMNS = {
            COLUMN_ID,
            COLUMN_TITLE,
            COLUMN_CONTENT,
            COLUMN_IMAGE_URL,
            COLUMN_IMAGE_WIDTH,
            COLUMN_IMAGE_HEIGHT,
            COLUMN_CREATED_AT,
            COLUMN_IS_FAVORITE,
            COLUMN_LAYOUT_MODE,
            COLUMN_VIDEO_URL,
            COLUMN_VIDEO_COVER_URL,
            COLUMN_VIDEO_DURATION,
            COLUMN_LAST_PLAY_POSITION,
            COLUMN_VIDEO_WIDTH,
            COLUMN_VIDEO_HEIGHT,
            COLUMN_MEDIA_TYPE
    };

    // ==================== 构造方法 ====================

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // ==================== 数据库生命周期方法 ====================

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_FEED_ITEMS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            // 从版本1升级到版本2：添加视频相关字段
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_VIDEO_URL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_VIDEO_COVER_URL + " TEXT");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_VIDEO_DURATION + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_LAST_PLAY_POSITION + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_VIDEO_WIDTH + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_VIDEO_HEIGHT + " INTEGER DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_FEED_ITEMS + " ADD COLUMN " + COLUMN_MEDIA_TYPE + " INTEGER DEFAULT 0");
        } else {
            // 其他版本升级逻辑
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_FEED_ITEMS);
            onCreate(db);
        }
    }

    // ==================== 数据插入方法 ====================

    public long insertFeedItem(FeedItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createContentValues(item);

        long id = db.insert(TABLE_FEED_ITEMS, null, values);
        db.close();

        return id;
    }

    // ==================== 数据删除方法 ====================

    public int deleteFeedItem(long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        int count = db.delete(TABLE_FEED_ITEMS, selection, selectionArgs);
        db.close();

        return count;
    }

    public int deleteData() {
        SQLiteDatabase db = this.getWritableDatabase();
        int count = db.delete(TABLE_FEED_ITEMS, null, null);
        db.close();

        return count;
    }

    // ==================== 数据更新方法 ====================

    public int updateFeedItem(FeedItem item) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = createContentValues(item);

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(item.getId())};

        int count = db.update(TABLE_FEED_ITEMS, values, selection, selectionArgs);
        Log.d("dbHelper", "updateFeedItem: " + count);
        db.close();

        return count;
    }

    // 更新播放位置（单独方法，因为这是频繁操作）
    public int updateVideoPlayPosition(long itemId, long playPosition) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_PLAY_POSITION, playPosition);

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(itemId)};

        int count = db.update(TABLE_FEED_ITEMS, values, selection, selectionArgs);
        db.close();

        return count;
    }

    public boolean updateAll(List<FeedItem> items) {
        SQLiteDatabase db = null;
        try {
            db = getWritableDatabase();
            db.beginTransaction();

            // 清空旧数据
            db.delete(TABLE_FEED_ITEMS, null, null);

            // 插入新数据
            for (FeedItem item : items) {
                ContentValues values = createContentValues(item);
                db.insert(TABLE_FEED_ITEMS, null, values);
            }

            db.setTransactionSuccessful();
            return true;

        } catch (Exception e) {
            Log.e("update错误", "updateAll: ", e);
            return false;
        } finally {
            if (db != null) {
                db.endTransaction();
                db.close();
            }
        }
    }

    // ==================== 数据查询方法 ====================

    public FeedItem getFeedItemById(long id) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_ID + " = ?";
        String[] selectionArgs = {String.valueOf(id)};

        Cursor cursor = db.query(TABLE_FEED_ITEMS, ALL_COLUMNS, selection, selectionArgs, null, null, null);

        FeedItem item = extractFeedItemFromCursor(cursor);
        db.close();

        return item;
    }

    public List<FeedItem> getAllFeedItems() {
        List<FeedItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_FEED_ITEMS,
                ALL_COLUMNS,
                null, null, null, null,
                COLUMN_CREATED_AT + " DESC");

        extractFeedItemsFromCursor(cursor, items);
        db.close();

        return items;
    }

    public List<FeedItem> getFeedItemsByPage(int page, int pageSize) {
        List<FeedItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        int offset = page * pageSize;
        String limit = pageSize + " OFFSET " + offset;

        Cursor cursor = db.query(
                TABLE_FEED_ITEMS,
                ALL_COLUMNS,
                null, null, null, null,
                COLUMN_CREATED_AT + " DESC",
                limit
        );

        extractFeedItemsFromCursor(cursor, items);
        db.close();

        return items;
    }

    public List<FeedItem> getFavoriteFeedItems() {
        List<FeedItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_IS_FAVORITE + " = ?";
        String[] selectionArgs = {"1"};

        Cursor cursor = db.query(
                TABLE_FEED_ITEMS,
                ALL_COLUMNS,
                selection, selectionArgs, null, null,
                COLUMN_CREATED_AT + " DESC"
        );

        extractFeedItemsFromCursor(cursor, items);
        db.close();

        return items;
    }

    // 新增：根据媒体类型查询
    public List<FeedItem> getFeedItemsByMediaType(MediaType mediaType) {
        List<FeedItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String selection = COLUMN_MEDIA_TYPE + " = ?";
        String[] selectionArgs = {String.valueOf(mediaType.ordinal())};

        Cursor cursor = db.query(
                TABLE_FEED_ITEMS,
                ALL_COLUMNS,
                selection, selectionArgs, null, null,
                COLUMN_CREATED_AT + " DESC"
        );

        extractFeedItemsFromCursor(cursor, items);
        db.close();

        return items;
    }

    // ==================== 工具方法 ====================

    private ContentValues createContentValues(FeedItem item) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TITLE, item.getTitle());
        values.put(COLUMN_CONTENT, item.getContent());
        values.put(COLUMN_CREATED_AT, item.getCreatedAt());
        values.put(COLUMN_IS_FAVORITE, item.getIsFavorite());
        values.put(COLUMN_LAYOUT_MODE, item.getLayoutMode().ordinal());
        values.put(COLUMN_MEDIA_TYPE, item.getMediaType().ordinal());

        // 根据媒体类型设置相应的字段
        if (item.getMediaType() == MediaType.image) {
            values.put(COLUMN_IMAGE_URL, item.getImageUrl());
            values.put(COLUMN_IMAGE_WIDTH, item.getImageWidth());
            values.put(COLUMN_IMAGE_HEIGHT, item.getImageHeight());
            // 视频字段设为 null 或默认值
            values.put(COLUMN_VIDEO_URL, (String) null);
            values.put(COLUMN_VIDEO_COVER_URL, (String) null);
            values.put(COLUMN_VIDEO_DURATION, 0);
            values.put(COLUMN_LAST_PLAY_POSITION, 0);
            values.put(COLUMN_VIDEO_WIDTH, 0);
            values.put(COLUMN_VIDEO_HEIGHT, 0);
        } else { // VIDEO
            values.put(COLUMN_VIDEO_URL, item.getVideoUrl());
            values.put(COLUMN_VIDEO_COVER_URL, item.getVideoCoverUrl());
            values.put(COLUMN_VIDEO_DURATION, item.getVideoDuration());
            values.put(COLUMN_LAST_PLAY_POSITION, item.getLastPlayPosition());
            values.put(COLUMN_VIDEO_WIDTH, item.getVideoWidth());
            values.put(COLUMN_VIDEO_HEIGHT, item.getVideoHeight());
            // 图片字段设为 null 或默认值
            values.put(COLUMN_IMAGE_URL, (String) null);
            values.put(COLUMN_IMAGE_WIDTH, 0);
            values.put(COLUMN_IMAGE_HEIGHT, 0);
        }

        return values;
    }

    private FeedItem extractFeedItemFromCursor(Cursor cursor) {
        FeedItem item = null;
        if (cursor != null && cursor.moveToFirst()) {
            try {
                item = new FeedItem();
                populateFeedItemFromCursor(cursor, item);
            } catch (Exception e) {
                Log.e("SqlSearch", "getFeedItemById: " + e.toString());
            } finally {
                cursor.close();
            }
        }
        return item;
    }

    private void extractFeedItemsFromCursor(Cursor cursor, List<FeedItem> items) {
        if (cursor != null && cursor.moveToFirst()) {
            try {
                do {
                    FeedItem item = new FeedItem();
                    populateFeedItemFromCursor(cursor, item);
                    items.add(item);
                } while (cursor.moveToNext());
            } catch (Exception e) {
                Log.e("SqlSearch", "getAllFeedItems: " + e.toString());
            } finally {
                cursor.close();
            }
        }
    }

    private void populateFeedItemFromCursor(Cursor cursor, FeedItem item) {
        item.setId(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)));
        item.setTitle(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)));
        item.setContent(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTENT)));
        item.setImageUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URL)));
        item.setImageWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_WIDTH)));
        item.setImageHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_HEIGHT)));
        item.setCreatedAt(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_CREATED_AT)));
        item.setIsFavorite(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IS_FAVORITE)));

        int layoutModeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_LAYOUT_MODE));
        item.setLayoutModeFromValue(layoutModeValue);

        // 新增视频相关字段
        item.setVideoUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_URL)));
        item.setVideoCoverUrl(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_COVER_URL)));
        item.setVideoDuration(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_DURATION)));
        item.setLastPlayPosition(cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_LAST_PLAY_POSITION)));
        item.setVideoWidth(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_WIDTH)));
        item.setVideoHeight(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_VIDEO_HEIGHT)));

        int mediaTypeValue = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MEDIA_TYPE));
        item.setMediaTypeFromValue(mediaTypeValue);
    }
}