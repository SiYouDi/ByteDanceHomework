package com.example.bytedancehomework.ui.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.data.Item.FeedItem;
import com.example.bytedancehomework.manager.VideoPlayManager;

public class VideoViewHolder extends BaseViewHolder {
    private VideoView videoView;
    private ImageView videoCoverView;
    private ImageView playIcon;
    private TextView textTitle;
    private TextView textContent;
    private FeedItem currentItem;
    private VideoPlayManager videoPlayManager;


    public VideoViewHolder(@NonNull View itemView,VideoPlayManager videoPlayManager) {
        super(itemView);
        this.videoPlayManager = videoPlayManager;

        videoView = itemView.findViewById(R.id.videoView);
        videoCoverView = itemView.findViewById(R.id.videoCoverView);
        playIcon = itemView.findViewById(R.id.playIcon);
        textTitle = itemView.findViewById(R.id.textTitle);
        textContent = itemView.findViewById(R.id.textContent);

        setupVideoClickListeners();
    }

    @Override
    public void bind(FeedItem item) {
        this.currentItem = item;
        textTitle.setText(item.getTitle());
        textContent.setText(item.getContent());

        if (item.getVideoCoverUrl() != null && !item.getVideoCoverUrl().isEmpty()) {
            Glide.with(itemView.getContext())
                    .load(item.getVideoCoverUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.video_placeholder)
                            .error(R.drawable.video_error)
                            .centerCrop())
                    .into(videoCoverView);
        } else {
            videoCoverView.setImageResource(R.drawable.video_placeholder);
        }

        if (videoPlayManager.isPlaying(item)) {
            hidePlayState();
        } else {
            showPlayState();
        }
    }
    private void setupVideoClickListeners() {
        videoView.setOnClickListener(v->{
            handleClick();
        });
        playIcon.setOnClickListener(v->{
            handleClick();
        });
        videoCoverView.setOnClickListener(v->{
            handleClick();
        });
    }

    public void handleClick() {
        if(currentItem==null)
            return;

        if(videoPlayManager.isPlaying(currentItem))
        {
            videoPlayManager.pausePlayback();
            showPlayState();
        }
        else
        {
            videoPlayManager.setupVideoPlayback(currentItem,videoView);
            videoPlayManager.startPlayback();
            hidePlayState();
        }
    }


    public void playVideo() {
        if(currentItem != null) {
            videoPlayManager.setupVideoPlayback(currentItem, videoView);
            videoPlayManager.startPlayback();
            hidePlayState();
        }
    }

    public void showPlayState() {
        videoCoverView.setVisibility(View.VISIBLE);
        playIcon.setVisibility(View.VISIBLE);
    }

    public void hidePlayState() {
        videoCoverView.setVisibility(View.GONE);
        playIcon.setVisibility(View.GONE);
    }

    @Override
    public FeedItem getCurrentItem() {
        return currentItem;
    }
}
