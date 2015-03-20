package org.techteam.decider.gui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import org.techteam.decider.R;


public class PostToolbarView extends FrameLayout {

    public interface Listener {
        void likePressed(PostToolbarView view);
        void commentPressed(PostToolbarView view);
    }

    private Listener listener;

    private ImageButton likeButton;
    private ImageButton commentButton;

    private boolean liked;

    PorterDuffColorFilter pressedStateFilter =
            new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);


    public PostToolbarView(Context context, AttributeSet attrs) {
        super(context, attrs);

        init(context);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.PostToolbarView,
                0, 0);

        try {
//            String rating = a.getString(R.styleable.PostToolbarView_rating);
//            pollView.setValue(rating);
//
//            boolean liked = a.getBoolean(R.styleable.PostToolbarView_liked, false);
//            if (liked)
//                pollView._setState(PollView.State.LIKED);
//
//            boolean disliked = a.getBoolean(R.styleable.PostToolbarView_disliked, false);
//            if (disliked)
//                pollView._setState(PollView.State.DISLIKED);
//
//            _setBayaned(a.getBoolean(R.styleable.PostToolbarView_bayaned, false));
//            _setFaved(a.getBoolean(R.styleable.PostToolbarView_faved, false));
        } finally {
            a.recycle();
        }
    }


    private void init(Context context) {
        View.inflate(context, R.layout.post_toolbar_view, this);

        likeButton = (ImageButton) findViewById(R.id.post_like);
        commentButton = (ImageButton) findViewById(R.id.post_comments);

        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setLiked(!liked);
            }
        });
//        commentButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                setFaved(!faved);
//            }
//        });
    }


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(int i = 0 ; i < getChildCount() ; i++){
            getChildAt(i).layout(0, 0, r - l, b - t);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {

        if (listener != null)
            listener.likePressed(PostToolbarView.this);
    }
}

