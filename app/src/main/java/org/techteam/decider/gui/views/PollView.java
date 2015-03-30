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
import android.widget.TextView;

import org.techteam.decider.R;


public class PollView extends FrameLayout {
//
//    public interface Listener {
//        void ratingViewStateChanged(PollView pollView, State oldState, State newState);
//    }
//
//    private Listener listener;
//
//    public enum State {IDLE, LIKED, DISLIKED}
//
//    private String value = "0";
//    private State state = State.IDLE;
//
//    private ImageButton likeButton;
//    private TextView ratingText;
//    private ImageButton dislikeButton;
//
//    PorterDuffColorFilter pressedStateFilter =
//            new PorterDuffColorFilter(Color.BLUE, PorterDuff.Mode.SRC_ATOP);
//
//
    public PollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
//
//        init(context);
//
//        TypedArray a = context.getTheme().obtainStyledAttributes(
//                attrs,
//                R.styleable.RatingView,
//                0, 0);
//
//        try {
//            setValue(a.getText(R.styleable.RatingView_value).toString());
//            int stateOrdinal = a.getInteger(R.styleable.RatingView_state, state.ordinal());
//            _setState(State.values()[stateOrdinal]);
//        } finally {
//            a.recycle();
//        }
//    }
//
//
//    private void init(Context context) {
//        View.inflate(context, R.layout.rating_view, this);
//
//        likeButton = (ImageButton) findViewById(R.id.post_like);
//        dislikeButton = (ImageButton) findViewById(R.id.post_dislike);
//
//        ratingText = (TextView) findViewById(R.id.post_rating);
//
//        likeButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (state == State.IDLE)
//                    setState(State.LIKED);
//            }
//        });
//
//        dislikeButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (state == State.IDLE)
//                    setState(State.DISLIKED);
//            }
//        });
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//    }
//
//
//    public String getValue() {
//        return value;
//    }
//
//    public void setValue(String value) {
//        this.value = value;
//
//        ratingText.setText(this.value);
//
//        invalidate();
//        requestLayout();
//    }
//
//    public State getState() {
//        return state;
//    }
//
//    /**
//     * Changes current control state with calling listener
//     * @param state new state
//     */
//    public void setState(State state) {
//        State oldState = this.state;
//        _setState(state);
//
//        if (listener != null)
//            listener.ratingViewStateChanged(this, oldState, state);
//    }
//
//    /**
//     * Changes current control state without calling listener
//     * @param state new state
//     */
//    public void _setState(State state) {
//        this.state = state;
//
//        likeButton.setEnabled(state == State.IDLE);
//        dislikeButton.setEnabled(state == State.IDLE);
//
//        likeButton.setColorFilter(state == State.LIKED ?
//                pressedStateFilter : null);
//
//        dislikeButton.setColorFilter(state == State.DISLIKED ?
//                pressedStateFilter : null);
//
//        invalidate();
//    }
//
//    public Listener getListener() {
//        return listener;
//    }
//
//    public void setListener(Listener listener) {
//        this.listener = listener;
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int l, int t, int r, int b) {
//        for(int i = 0 ; i < getChildCount() ; i++){
//            getChildAt(i).layout(0, 0, r - l, b - t);
//        }
//    }
}
