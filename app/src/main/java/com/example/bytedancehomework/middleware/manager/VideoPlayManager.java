package com.example.bytedancehomework.middleware.manager;

import android.util.Log;
import android.widget.VideoView;

import com.example.bytedancehomework.DBHelper.DatabaseHelper;
import com.example.bytedancehomework.Item.FeedItem;

public class VideoPlayManager {

    public interface PlaybackStateListener
    {
        void onPlaybackStarted(FeedItem item);
        void onPlaybackPaused(FeedItem item,int currentPosition);
        void onPlaybackStopped(FeedItem item);
        void onPlaybackCompleted(FeedItem item);
        void onPlaybackError(FeedItem item,String error);
    }

    private static VideoPlayManager instance;
    private PlaybackStateListener playbackStateListener;
    private VideoView currentVideoView;
    private FeedItem currentPlayingItem;
    private DatabaseHelper dbHelper;

    private VideoPlayManager() {}

    //单例实现
    public static VideoPlayManager getInstance()
    {
        if(instance==null)
        {
            synchronized (VideoPlayManager.class)
            {
                if(instance==null)
                    instance=new VideoPlayManager();
            }
        }
        return instance;
    }
    public void setDbHelper(DatabaseHelper dbHelper)
    {
        this.dbHelper=dbHelper;
    }
    public void setPlaybackStateListener(PlaybackStateListener listener)
    {
        this.playbackStateListener = listener;
    }

    public boolean isPlaying(FeedItem item)
    {
        return currentPlayingItem != null &&
                currentPlayingItem.getId() == item.getId() &&
                currentVideoView != null &&
                currentVideoView.isPlaying();
    }

    public FeedItem getCurrentPlayingItem()
    {
        return currentPlayingItem;
    }

    public void pauseCurrentPlayback()
    {
        if(currentVideoView!=null&&currentVideoView.isPlaying())
            pausePlayback();
    }

    public void setupVideoPlayback(FeedItem item,VideoView videoView)
    {
        pauseCurrentPlayback();

        currentPlayingItem=item;
        currentVideoView=videoView;

        try {
            currentVideoView.setVideoPath(item.getVideoUrl());

            if (item.getLastPlayPosition() > 0) {
                currentVideoView.seekTo((int) item.getLastPlayPosition());
            }

            // 统一在 VideoPlayManager 中设置完成监听器
            currentVideoView.setOnCompletionListener(mp -> {
                stopPlayback();
                // 通知监听器播放完成，由监听器处理UI更新
                if (playbackStateListener != null) {
                    playbackStateListener.onPlaybackCompleted(currentPlayingItem);
                }
            });

        } catch (Exception e) {
            if (playbackStateListener != null) {
                playbackStateListener.onPlaybackError(item, e.getMessage());
            }
            currentPlayingItem = null;
            currentVideoView = null;
        }
    }

    public void startPlayback()
    {
        if(currentVideoView!=null) {
            currentVideoView.start();
            if(playbackStateListener!=null&&currentPlayingItem!=null)
            {
                playbackStateListener.onPlaybackStarted(currentPlayingItem);
            }
        }

    }

    public void pausePlayback()
    {
        if(currentVideoView!=null&&currentVideoView.isPlaying())
        {
            currentVideoView.pause();
            savePlayPosition();
            if (playbackStateListener != null && currentPlayingItem != null) {
                playbackStateListener.onPlaybackPaused(currentPlayingItem,
                        currentVideoView.getCurrentPosition());
            }
        }
    }

    public void stopPlayback()
    {
        if(currentVideoView!=null)
        {
            savePlayPosition();
            currentVideoView.stopPlayback();
            FeedItem stoppedItem = currentPlayingItem;
            currentVideoView=null;
            currentPlayingItem=null;

            if (playbackStateListener != null && stoppedItem != null) {
                playbackStateListener.onPlaybackStopped(stoppedItem);
            }
        }
    }

    private void savePlayPosition() {
        if(currentVideoView!=null&&currentPlayingItem!=null&&dbHelper!=null)
        {
            int position =currentVideoView.getCurrentPosition();
            currentPlayingItem.setLastPlayPosition(position);
            dbHelper.updateVideoPlayPosition(currentPlayingItem.getId(),position);
        }
    }

    public void release()
    {
        stopPlayback();
    }
}
