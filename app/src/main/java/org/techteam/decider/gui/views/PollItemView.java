package org.techteam.decider.gui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.rest.api.ApiUI;

public class PollItemView extends FrameLayout {
    // children
    protected ImageView imageView;
    protected ImageView selectedMarkView;
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
                onImageClick();
            }
        });

        selectedMarkView = (ImageView) v.findViewById(R.id.poll_selection_mark);
        selectedMarkView.setVisibility(INVISIBLE);

        ratingText = (TextView) v.findViewById(R.id.poll_rating);
    }

    protected void onImageClick() {
        if (listener != null) {
            listener.polled(this, this.entry);
            selectedMarkView.setVisibility(VISIBLE);
        }
    }

    public void setMarked(boolean marked) {
        selectedMarkView.setVisibility(marked ? VISIBLE : INVISIBLE);
    }

    public void setEntry(PollItemEntry entry) {
        this.entry = entry;

        ImageLoader imageLoader = ImageLoader.getInstance();
        if (!imageLoader.isInited())
            imageLoader.init(ImageLoaderConfiguration.createDefault(getContext()));

        if (entry.getPreviewUrl() != null) {
            imageLoader.displayImage(ApiUI.resolveUrl(entry.getPreviewUrl()), imageView);
        }
        ratingText.setText(Integer.toString(entry.getVotesCount()));
    }

    public PollItemEntry getEntry() {
        return entry;
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }
}
