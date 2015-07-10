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
import android.support.v4.content.FileProvider;
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
import org.techteam.decider.gui.activities.MainActivity;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.fragments.ProfileFragment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


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
    private PollView pollView;

    // footer
    private ImageButton shareButton;
    private Button likeButton;
    private Button commentsButton;

    private OnQuestionEventCallback onQuestionEventCallback;

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
    public void reuse(QuestionEntry entry, QuestionInteractor questionInteractor) {
        this.entry = entry;
        this.questionInteractor = questionInteractor;

        fillFields();
    }

    protected void fillFields() {
        final Context context = getContext();

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
                if (onQuestionEventCallback != null) {
                    onQuestionEventCallback.onVote(entry, pollItemId);
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

    //TODO: govnokod
    private Uri leftImage;
    private Uri rightImage;

    protected void attachCallbacks() {
        shareButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final Context context = v.getContext();

                ImageLoader.getInstance().loadImage(
                        "http://img3.wikia.nocookie.net/__cb20121227201208/jamesbond/images/6/61/Generic_Placeholder_-_Profile.jpg",
                        new ImageLoadingListener() {
                            @Override
                            public void onLoadingStarted(String imageUri, View view) {

                            }

                            @Override
                            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {

                            }

                            @Override
                            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                                File imagePath = new File(context.getFilesDir(), "images");
                                imagePath.mkdir();

                                File file = new File(imagePath, "default_image.jpg");

                                if (file.exists()) {
                                    file.delete();
                                }

                                FileOutputStream out = null;
                                try {
                                    out = new FileOutputStream(file);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                loadedImage.compress(Bitmap.CompressFormat.JPEG, 100, out);
                                try {
                                    out.close();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                Uri uri = WorkingFileProvider.getUriForFile(context, "org.techteam.decider.lol", file);

                                ArrayList<Uri> images = new ArrayList<>();
                                images.add(uri);
                                images.add(uri);

                                Intent sendIntent = new Intent();
                                sendIntent.setAction(Intent.ACTION_SEND_MULTIPLE);
                                sendIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                sendIntent.setType("image/*");
                                sendIntent.putExtra(Intent.EXTRA_TEXT, "test");
                                sendIntent.putExtra(Intent.EXTRA_STREAM, images);

                                //grant permisions for all apps that can handle given intent
                                List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(sendIntent, PackageManager.MATCH_DEFAULT_ONLY);
                                for (ResolveInfo resolveInfo : resInfoList) {
                                    String packageName = resolveInfo.activityInfo.packageName;
                                    context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                }

                                context.startActivity(Intent.createChooser(sendIntent, "Select app"));
                            }

                            @Override
                            public void onLoadingCancelled(String imageUri, View view) {

                            }
                        });
            }
        });

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

        avatarImage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                //TODO: evil things
                MainActivity activity = (MainActivity) getContext();
                ProfileFragment.create(activity, null);
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

    public void setOnQuestionEventCallback(OnQuestionEventCallback cb) {
        onQuestionEventCallback = cb;
    }
}
