package org.techteam.decider.gui.views;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.PollItemEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.WorkingFileProvider;
import org.techteam.decider.gui.activities.ProfileActivity;
import org.techteam.decider.gui.fragments.ShareHelper;
import org.techteam.decider.rest.api.ApiUI;
import org.techteam.decider.util.ImageLoaderInitializer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class QuestionView extends PostView {

    public interface EventListener {
        void onLikeClick(QuestionEntry post);
        void onVoteClick(QuestionEntry post, int voteId);
        void onCommentsClick(QuestionEntry post);
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
    // content
    private EllipsizingTextView postText;
    private TextView ellipsizeHintText;

    private PollView pollView;
    // footer
    private ImageButton shareButton;
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
        View v = View.inflate(context, R.layout.view_question_entry, this);

        // find children
        // header
        authorText = (TextView) v.findViewById(R.id.author_text);
        dateText = (TextView) v.findViewById(R.id.date_text);
        avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
        overflowButton = (ImageButton) v.findViewById(R.id.overflow_button);

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
    public void reuse(QuestionEntry entry) {
        this.entry = entry;
        fillFields();
    }

    protected void fillFields() {
        final Context context = getContext();

        String avatar = entry.getAuthor().getAvatar();
        if (avatar != null) {
            ImageLoader imageLoader = ImageLoaderInitializer.getImageLoader(context);
            imageLoader.displayImage(ApiUI.resolveUrl(avatar), avatarImage);
        }

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
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ShareHelper.share(getContext(), entry);
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

    private Uri getImageContentUri(File imageFile) {
        Context context = getContext();

        String filePath = imageFile.getAbsolutePath();
        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new String[] { MediaStore.Images.Media._ID },
                MediaStore.Images.Media.DATA + "=? ",
                new String[] { filePath }, null);
        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor
                    .getColumnIndex(MediaStore.MediaColumns._ID));
            Uri baseUri = Uri.parse("content://media/external/images/media");
            return Uri.withAppendedPath(baseUri, "" + id);
        } else {
            if (imageFile.exists()) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DATA, filePath);
                return context.getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } else {
                return null;
            }
        }
    }

    public void setEventListener(EventListener cb) {
        eventListener = cb;
    }
}
