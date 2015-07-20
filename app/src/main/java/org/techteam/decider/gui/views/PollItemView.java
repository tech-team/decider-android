package org.techteam.decider.gui.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.gui.activities.PreviewImageActivity;
import org.techteam.decider.gui.activities.QuestionDetailsActivity;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.util.ImageLoaderInitializer;

public class PollItemView extends FrameLayout {
    // children
    protected CardView cardView;
    protected ImageView imageView;
    protected TextView ratingText;

    // listener
    public interface Listener {
        void polled(PollItemView pollitemView, PollItemEntry pollItemEntry);
    }

    protected Listener listener;

    // data
    protected PollItemEntry entry;

    public PollItemView(Context context) {
        super(context);
        init(context);
    }

    public PollItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PollItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        ViewGroup v = this;
        LayoutInflater.from(context).inflate(R.layout.view_poll_item, v);

        imageView = (ImageView) v.findViewById(R.id.poll_image);
        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                previewImage();
            }
        });
        imageView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                vote();
                return true;
            }
        });


        cardView = (CardView) v.findViewById(R.id.item_frame);
        setMarked(false);

        ratingText = (TextView) v.findViewById(R.id.poll_rating);
    }

    protected void vote() {
        if (listener != null) {
            listener.polled(this, this.entry);
//            setMarked(true);
        }
    }

    protected void previewImage() {
        Intent intent = new Intent(getContext(), PreviewImageActivity.class);
        intent.putExtra(PreviewImageActivity.IMAGE_URL, ApiUI.resolveUrl(entry.getImageUrl()));
        getContext().startActivity(intent);
    }

    public void setMarked(boolean marked) {
        int color = getResources().getColor(android.R.color.white);
        if (marked)
            color = getResources().getColor(android.R.color.holo_green_light);

        cardView.setCardBackgroundColor(color);
    }

    public void setEntry(PollItemEntry entry) {
        this.entry = entry;

        ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(getContext());
        if (entry.getPreviewUrl() != null) {
            imageLoader.displayImage(ApiUI.resolveUrl(entry.getPreviewUrl()), imageView, new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    // animation lags on QuestionDetailsActivity
                    // any way image is in the cache (it was clicked in the list)
                    // so animation is not needed
                    if (getContext().getClass() == QuestionDetailsActivity.class)
                        return;

                    Animation anim = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
                    imageView.setAnimation(anim);
                    anim.start();
                }
            });
        }
        ratingText.setText(Integer.toString(entry.getVotesCount()));
        setMarked(entry.isVoted());
    }

    public PollItemEntry getEntry() {
        return entry;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }


}
