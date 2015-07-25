package org.techteam.decider.gui.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.activities.ProfileActivity;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.util.ImageLoaderInitializer;


public class QuestionView extends PostView {

    private static final String TAG = QuestionView.class.getName();

    public interface EventListener {
        void onLikeClick(QuestionEntry post);
        void onVoteClick(QuestionEntry post, int voteId);
        void onCommentsClick(QuestionEntry post);
        void onReportSpamClick(QuestionEntry post);
    }

    // data
    private QuestionEntry entry;
    private EventListener eventListener;

    private static final int POST_TEXT_MAX_LINES = 5;
    // children
    // header
    private TextView authorText;
    private TextView dateText;
    private ImageView avatarImage;

    private ImageButton overflowButton;
    private ImageView anonBadge;
    private TextView categoryBadge;

    // content
    private EllipsizingTextView postText;
    private TextView ellipsizeHintText;

    private PollView pollView;
    // footer
    private ImageButton shareButton;
    private Button likeButton;

    private Button commentsButton;

    private ImageLoader imageLoader;

    private PorterDuffColorFilter pressedStateFilter =
            new PorterDuffColorFilter(getContext().getResources().getColor(R.color.accent_dark), PorterDuff.Mode.SRC_ATOP);

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
        View v = View.inflate(context, R.layout.view_question_entry, this);

        imageLoader = ImageLoaderInitializer.getImageLoader(context);

        // find children
        // header
        authorText = (TextView) v.findViewById(R.id.author_text);
        dateText = (TextView) v.findViewById(R.id.date_text);
        avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
        overflowButton = (ImageButton) v.findViewById(R.id.overflow_button);
        anonBadge = (ImageView) v.findViewById(R.id.anon_badge);
        categoryBadge = (TextView) v.findViewById(R.id.category_badge);

        // content
        postText = (EllipsizingTextView) v.findViewById(R.id.post_text);
        ellipsizeHintText = (TextView) v.findViewById(R.id.post_ellipsize_hint);
        pollView = (PollView) v.findViewById(R.id.poll_view);

        // footer
        shareButton = (ImageButton) v.findViewById(R.id.share_button);
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
    public void reuse(QuestionEntry entry, EventListener eventListener) {
        this.entry = entry;
        this.eventListener = eventListener;
        fillFields();
    }

    public void update(QuestionEntry entry) {
        this.entry = entry;
        fillFields();
    }

    public QuestionEntry getEntry() {
        return entry;
    }

    protected void fillFields() {
        final Context context = getContext();

        if (entry == null) {
            Log.w(TAG, "QuestionEntry is null");
            return;
        }

        String avatar = entry.getAuthor().getAvatar();
        imageLoader.cancelDisplayTask(avatarImage);
        if (avatar != null)
            imageLoader.displayImage(ApiUI.resolveUrl(avatar), avatarImage);
        else
            avatarImage.setImageDrawable(context.getResources().getDrawable(R.drawable.profile));

        authorText.setText(entry.getAuthor().getUsername());
        dateText.setText(getDateString(entry.getCreationDate()));
        postText.setText(entry.getText());
        likeButton.setText("+" + entry.getLikesCount());
        final int LEFT = 0;
        // Order: Left, top, right, and bottom
        likeButton.getCompoundDrawables()[LEFT].setColorFilter(entry.isVoted() ? pressedStateFilter : null);

        commentsButton.setText(Integer.toString(entry.getCommentsCount()));

        String currentUserId = ApiUI.getCurrentUserId(getContext());
        anonBadge.setVisibility(entry.isAnonymous() && entry.getAuthor().getUid().equals(currentUserId) ? VISIBLE : GONE);
        anonBadge.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.anonymously)
                        .setMessage(R.string.anon_explanation)
                        .setIcon(R.drawable.logo)
                        .setCancelable(false)
                        .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
            }
        });
        CategoryEntry categoryEntry = CategoryEntry.byUid(entry.getCategoryId());
        if (categoryEntry != null)
            categoryBadge.setText(categoryEntry.getLocalizedLabel());

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

        pollView.setItems(new PollItemEntry[]{
                entry.getPollItem1(),
                entry.getPollItem2()
        });
        pollView.setListener(new PollView.Listener() {
            @Override
            public void polled(int pollItemId) {
                if (eventListener != null) {
                    eventListener.onVoteClick(entry, pollItemId);
                }
            }
        });

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
                            case R.id.report_spam:
                                eventListener.onReportSpamClick(entry);
                                return true;
                            default:
                                return false;
                        }
                    }
                });

                menu.show();
            }
        });

        if (entry.isAnonymous()) {

        }
    }

    protected void attachCallbacks() {
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Context context = getContext();
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.putExtra(Intent.EXTRA_TEXT, formatEntryForSharing(context));
                sendIntent.setType("text/plain");
                context.startActivity(sendIntent);
            }
        });

        commentsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventListener != null) {
                    eventListener.onCommentsClick(entry);
                }
            }
        });

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventListener != null) {
                    eventListener.onCommentsClick(entry);
                }
            }
        });

        likeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (eventListener != null) {
                    eventListener.onLikeClick(entry);
                }
            }
        });

        avatarImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ProfileActivity.class);
                String uid = entry.getAuthor().getUid();
                intent.putExtra(ProfileActivity.USER_ID, uid);
                getContext().startActivity(intent);
            }
        });
    }

    private String formatEntryForSharing(Context context) {
        StringBuilder sb = new StringBuilder();

        String endLine = "\n";
        String hashTag = "#" + context.getString(R.string.app_name).toLowerCase();

        sb.append(context.getString(R.string.shared_by));
        sb.append(endLine);
        sb.append(hashTag);
        sb.append(endLine);
        sb.append(ApiUI.resolveShareImageUrl(entry));

        return sb.toString();
    }

}
