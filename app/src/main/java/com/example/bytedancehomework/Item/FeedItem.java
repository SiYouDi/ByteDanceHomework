package com.example.bytedancehomework.Item;

public class FeedItem {
    private long id; // 数据库主键
    private String title;
    private String content;
    private String imageUrl;
    private int imageWidth;
    private int imageHeight;
    private long createdAt;
    private int isFavorite;

    public FeedItem() {
    }

    public FeedItem(String title, String content, String imageUrl, int imageWidth, int imageHeight) {
        this.title = title;
        this.content = content;
        this.imageUrl = imageUrl;
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.createdAt = System.currentTimeMillis();
        this.isFavorite = 0;
    }

    // Getter 方法
    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public int getIsFavorite() {
        return isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite == 1;
    }


    // Setter 方法
    public void setId(long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setImageWidth(int imageWidth) {
        this.imageWidth = imageWidth;
    }

    public void setImageHeight(int imageHeight) {
        this.imageHeight = imageHeight;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public void setIsFavorite(int isFavorite) {
        this.isFavorite = isFavorite;
    }

    public void setFavorite(boolean favorite) {
        this.isFavorite = favorite ? 1 : 0;
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
                ", createdAt=" + createdAt +
                ", isFavorite=" + isFavorite +
                '}';
    }
}
