package org.techteam.decider.gui.views;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.gui.activities.ProfileActivity;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.util.ImageLoaderInitializer;


public class CommentView extends PostView {

    public interface EventListener {
        void onReportSpamClick(CommentEntry entry);
    }

    // data
    private CommentEntry entry;
    private EventListener eventListener;
    private ImageLoader imageLoader;


    private static final int POST_TEXT_MAX_LINES = 5;

    // children
    // header
    private TextView authorText;
    private TextView dateText;
    private ImageView avatarImage;
    private ImageView anonBadge;

    // content
    private TextView postText;

    public CommentView(Context context) {
        super(context);
        init(context);
    }

    public CommentView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public CommentView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    protected void init(Context context) {
        View v = View.inflate(context, R.layout.view_comment_entry, this);

        v.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
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
                return true;
            }
        });

        imageLoader = ImageLoaderInitializer.getImageLoader(context);

        // find children
        // header
        authorText = (TextView) v.findViewById(R.id.author_text);
        dateText = (TextView) v.findViewById(R.id.date_text);
        avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
        anonBadge = (ImageView) v.findViewById(R.id.anon_badge);

        // content
        postText = (TextView) v.findViewById(R.id.comment_text);

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
    public void reuse(CommentEntry entry, EventListener eventListener) {
        this.entry = entry;
        this.eventListener = eventListener;
        fillFields();
    }

    protected void fillFields() {
        Context context = getContext();

        String avatar = entry.getAuthor().getAvatar();
        imageLoader.cancelDisplayTask(avatarImage);
        if (avatar != null)
            imageLoader.displayImage(ApiUI.resolveUrl(avatar), avatarImage);
        else
            avatarImage.setImageDrawable(context.getResources().getDrawable(R.drawable.profile));

        authorText.setText(entry.getAuthor().getUsername());
        String currentUserId = ApiUI.getCurrentUserId(getContext());
        anonBadge.setVisibility(entry.isAnonymous() && entry.getAuthor().getUid().equals(currentUserId) ? VISIBLE : GONE);

        dateText.setText(getDateString(entry.getCreationDate()));
        postText.setText(entry.getText());

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
    }

    protected void attachCallbacks() {
        anonBadge.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (entry == null || !entry.isAnonymous())
                    return;

                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.anonymously)
                        .setMessage(R.string.anon_explanation)
                        .setIcon(R.drawable.logo)
                        .setCancelable(false)
                        .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();
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
}
