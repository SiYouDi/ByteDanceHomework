package com.example.bytedancehomework.ui.Adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.example.bytedancehomework.R;
import com.example.bytedancehomework.data.Item.FeedItem;

public class ImageViewHolder extends BaseViewHolder {
    ImageView imageView;
    TextView textTitle;
    TextView textContent;
    FeedItem currentItem;
    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.imageView);
        textTitle = itemView.findViewById(R.id.textTitle);
        textContent = itemView.findViewById(R.id.textContent);
    }

    @Override
    public void bind(FeedItem item) {
        currentItem = item;
        textTitle.setText(item.getTitle());
        textContent.setText(item.getContent());

        if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
            Glide.with(itemView.getContext())
                    .load(item.getImageUrl())
                    .apply(new RequestOptions()
                            .placeholder(R.drawable.placeholder_image)
                            .error(R.drawable.error_image)
                            .centerCrop())
                    .into(imageView);
        } else {
            imageView.setImageResource(R.drawable.placeholder_image);
        }
    }

    public FeedItem getCurrentItem()
    {
        return currentItem;
    }
}
