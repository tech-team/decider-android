package org.techteam.decider.gui.views;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class QuestionView extends PostView {
    // data
    private QuestionEntry entry;
    private QuestionInteractor questionInteractor;

    private static final int POST_TEXT_MAX_LINES = 5;

    // children
    // header
    private TextView authorText;
    private TextView dateText;
    private ImageView avatarImage;
    private ImageButton overflowButton;

    // content
    private EllipsizingTextView postText;
    private TextView ellipsizeHintText;

    // footer
    private Button likeButton;
    private Button commentsButton;

    public QuestionView(Context context) {
        super(context);
        init(context);
    }

    public QuestionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public QuestionView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        View v = View.inflate(context, R.layout.fragment_question_entry, this);

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
        for(int i = 0 ; i < getChildCount() ; i++) {
            getChildAt(i).layout(0, 0, r - l, b - t);
        }
    }

    /**
     * Call this from onBindViewHolder
     * @param entry data source
     */
    public void reuse(QuestionEntry entry, QuestionInteractor questionInteractor) {
        this.entry = entry;
        this.questionInteractor = questionInteractor;

        fillFields();
    }

    protected void fillFields() {
        Context context = getContext();

        authorText.setText(entry.getAuthor().getUsername());
        dateText.setText(getDateString(entry.getCreationDate()));
        postText.setText(entry.getText());
        likeButton.setText("+" + entry.getLikesCount());
        commentsButton.setText(Integer.toString(entry.getCommentsCount()));

        //configure according to SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(context.getString(R.string.pref_shorten_long_posts_key), true))
            postText.setMaxLines(POST_TEXT_MAX_LINES);
        else
            postText.setMaxLines(Integer.MAX_VALUE);

        String textSize = prefs.getString(context.getString(R.string.pref_text_size_key), "small");
        assert textSize != null;  // suppress inspection
        switch (textSize) {
            case "small":
                postText.setTextAppearance(context, android.R.style.TextAppearance_Small);
                break;

            case "medium":
                postText.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                break;

            case "large":
                postText.setTextAppearance(context, android.R.style.TextAppearance_Large);
                break;
        }
        
        postText.setTextColor(context.getResources().getColor(android.R.color.black));


        //TODO: text justification, see:
        //http://stackoverflow.com/questions/1292575/android-textview-justify-text

        // TODO: mark liked and so on

//        toolbarView.setRating(entry.getRating());
//        toolbarView._setBayaned(entry.getIsBayan());
//        toolbarView._setFaved(entry.isFavorite());

        //TODO: set handlers

        postText.addEllipsizeListener(new EllipsizingTextView.EllipsizeListener() {
            @Override
            public void ellipsizeStateChanged(boolean ellipsized) {
                ellipsizeHintText.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
            }
        });

        //set expand function both for text and hint controls
        ellipsizeHintText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postText.setMaxLines(Integer.MAX_VALUE);
            }
        });

        postText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postText.setMaxLines(Integer.MAX_VALUE);
            }
        });

        overflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();

                PopupMenu menu = new PopupMenu(context, v);
                menu.inflate(R.menu.post_entry_context_menu);

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
                        switch (item.getItemId()) {
                            //TODO: context menu
                            default:
                                return false;
                        }
                    }
                });

                menu.show();
            }
        });
    }

    protected void attachCallbacks() {
        commentsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionInteractor != null)
                    questionInteractor.onCommentsClick(entry);
            }
        });

        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (questionInteractor != null)
                    questionInteractor.onLikeClick(entry);
            }
        });
    }
}
