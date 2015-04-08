package org.techteam.decider.gui.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.util.Toaster;


//TODO: privacy via reuse
public class PostView extends FrameLayout {
    // data
    private QuestionEntry entry;
    private PostInteractor postInteractor;

    // children
    // header
    public TextView authorText;
    public TextView dateText;
    public ImageView avatarImage;
    public ImageButton overflowButton;

    // content
    public EllipsizingTextView postText;
    public TextView ellipsizeHintText;

    // footer
    public Button likeButton;
    public Button commentsButton;

    public PostView(Context context) {
        super(context);
        init(context);
    }

    public PostView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public PostView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        View v = View.inflate(context, R.layout.fragment_post_entry, this);

        // find children
        // header
        authorText = (TextView) v.findViewById(R.id.author_text);
        dateText = (TextView) v.findViewById(R.id.date_text);
        avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
        overflowButton = (ImageButton) v.findViewById(R.id.overflow_button);

        // content
        postText = (EllipsizingTextView) v.findViewById(R.id.post_text);
        ellipsizeHintText = (TextView) v.findViewById(R.id.post_ellipsize_hint);

        //TODO: images
        //TODO: poll

        // footer
        likeButton = (Button) v.findViewById(R.id.like_button);
        commentsButton = (Button) v.findViewById(R.id.comments_button);

        // attach callbacks
        attachCallbacks();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for(int i = 0 ; i < getChildCount() ; i++){
            getChildAt(i).layout(0, 0, r - l, b - t);
        }
    }

    /**
     * Call this from onBindViewHolder
     * @param entry data source
     */
    public void reuse(QuestionEntry entry, PostInteractor postInteractor) {
        this.entry = entry;
        this.postInteractor = postInteractor;

        fillFields();
    }

    protected void fillFields() {
        Toaster.toast(getContext(), "TODO: reuse()");
    }

    protected void attachCallbacks() {
        commentsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postInteractor != null)
                    postInteractor.onCommentsClick(entry);
            }
        });

        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (postInteractor != null)
                    postInteractor.onLikeClick(entry);
            }
        });
    }
}
