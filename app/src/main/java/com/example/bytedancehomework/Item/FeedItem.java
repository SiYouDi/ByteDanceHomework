package com.example.bytedancehomework.Item;

import com.example.bytedancehomework.Enum.LayoutMode;
import com.example.bytedancehomework.Enum.MediaType;

public class FeedItem {

    //公共部分
    private long id; // 数据库主键
    private String title;
    private String content;

    //图片相关字段
    private String imageUrl;
    private int imageWidth;
    private int imageHeight;

    // 视频相关字段
    private String videoUrl;
    private String videoCoverUrl; // 视频封面图
    private int videoDuration; // 视频时长（毫秒）
    private long lastPlayPosition; // 上次播放位置（毫秒）
    private int videoWidth;
    private int videoHeight;

    private long createdAt;
    private int isFavorite;
    private LayoutMode layoutMode;
    private MediaType mediaType;

    public FeedItem() {
    }

    // 图片构造方法
    public FeedItem(String title, String content, String imageUrl,
                    int imageWidth, int imageHeight, LayoutMode layoutMode) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.createdAt = System.currentTimeMillis();
        this.isFavorite = 0;
        this.layoutMode = layoutMode;
        this.mediaType = MediaType.image;
    }

    // 视频构造方法
    public FeedItem(String title, String content, String videoUrl, String videoCoverUrl,
                    int videoWidth, int videoHeight, int videoDuration, LayoutMode layoutMode) {
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
        this.videoCoverUrl = videoCoverUrl;
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoDuration = videoDuration;
        this.createdAt = System.currentTimeMillis();
        this.isFavorite = 0;
        this.layoutMode = layoutMode;
        this.mediaType = MediaType.video;
        this.lastPlayPosition = 0;
    }
    // Getter 方法
    public long getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getImageUrl() { return imageUrl; }
    public int getImageWidth() { return imageWidth; }
    public int getImageHeight() { return imageHeight; }
    public String getVideoUrl() { return videoUrl; }
    public String getVideoCoverUrl() { return videoCoverUrl; }
    public int getVideoDuration() { return videoDuration; }
    public long getLastPlayPosition() { return lastPlayPosition; }
    public int getVideoWidth() { return videoWidth; }
    public int getVideoHeight() { return videoHeight; }
    public long getCreatedAt() { return createdAt; }
    public int getIsFavorite() { return isFavorite; }
    public LayoutMode getLayoutMode() { return layoutMode; }
    public MediaType getMediaType() { return mediaType; }

    public boolean isFavorite() {
        return isFavorite == 1;
    }
    public boolean isVideo() { return mediaType==MediaType.video; }


    // Setter 方法
    public void setId(long id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setContent(String content) { this.content = content; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public void setImageWidth(int imageWidth) { this.imageWidth = imageWidth; }
    public void setImageHeight(int imageHeight) { this.imageHeight = imageHeight; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public void setVideoCoverUrl(String videoCoverUrl) { this.videoCoverUrl = videoCoverUrl; }
    public void setVideoDuration(int videoDuration) { this.videoDuration = videoDuration; }
    public void setLastPlayPosition(long lastPlayPosition) { this.lastPlayPosition = lastPlayPosition; }
    public void setVideoWidth(int videoWidth) { this.videoWidth = videoWidth; }
    public void setVideoHeight(int videoHeight) { this.videoHeight = videoHeight; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public void setIsFavorite(int isFavorite) { this.isFavorite = isFavorite; }
    public void setFavorite(boolean favorite) { this.isFavorite = favorite ? 1 : 0; }
    public void setLayoutMode(LayoutMode layoutMode) { this.layoutMode = layoutMode; }
    public void setMediaType(MediaType mediaType) { this.mediaType = mediaType; }

    public void setLayoutModeFromValue(int value) {
        LayoutMode[] modes = LayoutMode.values();
        if (value >= 0 && value < modes.length) {
            this.layoutMode = modes[value];
        } else {
            this.layoutMode = LayoutMode.single; // 默认值
        }
    }

    public void setMediaTypeFromValue(int value)
    {
        MediaType[] types = MediaType.values();
        if(value>=0&&value<types.length)
        {
            this.mediaType=types[value];
        }
        else
        {
            this.mediaType =MediaType.image; //默认值
        }
    }

    @Override
    public String toString() {
        return "FeedItem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", content='" + content + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", imageWidth=" + imageWidth +
                ", imageHeight=" + imageHeight +
                ", videoUrl='" + videoUrl + '\'' +
                ", videoCoverUrl='" + videoCoverUrl + '\'' +
                ", videoDuration=" + videoDuration +
                ", lastPlayPosition=" + lastPlayPosition +
                ", videoWidth=" + videoWidth +
                ", videoHeight=" + videoHeight +
                ", createdAt=" + createdAt +
                ", isFavorite=" + isFavorite +
                ", layoutMode=" + layoutMode +
                ", mediaType=" + mediaType +
                '}';
    }
}
