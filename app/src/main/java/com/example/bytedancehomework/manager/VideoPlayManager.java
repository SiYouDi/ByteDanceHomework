package com.example.bytedancehomework.manager;

import android.os.Looper;
import android.util.Log;
import android.widget.VideoView;

import com.example.bytedancehomework.data.DBHelper.DatabaseHelper;
import com.example.bytedancehomework.data.Item.FeedItem;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

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
    private Runnable timeoutRunnable;
    private boolean isPrepared = false;
    private final String TAG = "VideoPlayManager";

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
        isPrepared=false;

        startTimeoutCheck(15000);

        try {
            clearListeners();

            Log.d(TAG, "setupVideoPlayback: 视频URL = " + item.getVideoUrl());

            // 验证URL是否有效
            String videoUrl = item.getVideoUrl();
            if (videoUrl == null || videoUrl.trim().isEmpty()) {
                Log.e(TAG, "视频URL为空");
                if (playbackStateListener != null) {
                    playbackStateListener.onPlaybackError(item, "视频地址为空");
                }
                return;
            }

            currentVideoView.setVideoPath(item.getVideoUrl());
            Log.d(TAG, "setupVideoPlayback: 设置播放路径完成");

            currentVideoView.setOnPreparedListener(mp->{
                Log.d(TAG, "setupVideoPlayback: 准备完成");
                isPrepared=true;
                cancleTimeoutCheck();

                if (item.getLastPlayPosition() > 0) {
                    currentVideoView.seekTo((int) item.getLastPlayPosition());
                }

                startPlayback();
            });

            currentVideoView.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "setupVideoPlayback: 播放错误，what=" + what + ", extra=" + extra);
                cancleTimeoutCheck(); // 错误时也取消超时检测
                String errorMsg = "播放错误，错误码: " + what + ", " + extra;
                if (playbackStateListener != null) {
                    playbackStateListener.onPlaybackError(item, errorMsg);
                }
                resetPlaybackState(); // 重置状态
                return true;
            });

            // 统一在 VideoPlayManager 中设置完成监听器
            currentVideoView.setOnCompletionListener(mp -> {
                FeedItem completedItem =currentPlayingItem;
                stopPlayback();
                // 通知监听器播放完成，由监听器处理UI更新
                if (playbackStateListener != null) {
                    playbackStateListener.onPlaybackCompleted(completedItem);
                }
            });

        } catch (Exception e) {
            if (playbackStateListener != null) {
                playbackStateListener.onPlaybackError(item, e.getMessage());
            }
            resetPlaybackState();
        }
    }

    private void startTimeoutCheck(int timeoutMillis) {
        cancleTimeoutCheck();

        timeoutRunnable=new Runnable() {
            @Override
            public void run() {
                if(!isPrepared)
                {
                    onPrepareTimeout();
                }
            }
        };

        currentVideoView.postDelayed(timeoutRunnable,timeoutMillis);
    }

    private void onPrepareTimeout() {
        pauseCurrentPlayback();

        if(playbackStateListener!=null)
            playbackStateListener.onPlaybackError(currentPlayingItem,"onPrepareTimeout");

        resetPlaybackState();
    }

    private void cancleTimeoutCheck() {
        Log.d(TAG, "cancleTimeoutCheck: 取消超时检测");
        if(currentVideoView!=null&&timeoutRunnable!=null)
        {
            currentVideoView.removeCallbacks(timeoutRunnable);
            timeoutRunnable=null;
        }
    }

    private void clearListeners() {
        currentVideoView.setOnPreparedListener(null);
        currentVideoView.setOnCompletionListener(null);
        currentVideoView.setOnErrorListener(null);
    }

    public void startPlayback()
    {
        if(currentVideoView!=null) {
            Log.d(TAG, "startPlayback: 开始播放");
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

    private void resetPlaybackState() {
        cancleTimeoutCheck();

        currentPlayingItem = null;
        currentVideoView = null;
        isPrepared = false;
    }

    public void release()
    {
        stopPlayback();
        resetPlaybackState();
    }
}
