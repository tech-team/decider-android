package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentProvider;
import org.techteam.decider.content.PostEntry;
import org.techteam.decider.gui.fragments.OnPostEventCallback;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.views.EllipsizingTextView;
import org.techteam.decider.gui.views.PostToolbarView;
import org.techteam.decider.gui.views.PollView;
import org.techteam.decider.util.Clipboard;
import org.techteam.decider.util.Toaster;
import org.techteam.decider.content.PostEntry;

import java.util.ArrayList;
import java.util.List;

public class PostsListAdapter
        extends CursorRecyclerViewAdapter<PostsListAdapter.ViewHolder> {
    private final OnPostEventCallback eventCallback;
    private final OnListScrolledDownCallback scrolledDownCallback;
    private List<PostEntry> dataset;

    private Context context;

    private int VIEW_TYPE_ENTRY = 0;
    private int VIEW_TYPE_FOOTER = 1;

    private static final int POST_TEXT_MAX_LINES = 5;

    public void setAll(ArrayList<PostEntry> entries) {
        dataset.clear();
        dataset.addAll(entries);
    }

    public void addAll(ArrayList<PostEntry> entries) {
        dataset.addAll(entries);
    }

    public boolean isEmpty() {
        return getItemCount() - 1 == 0; //-footer
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        //header
        public TextView id;
        public TextView date;
        public ImageButton overflow;

        //content
        public EllipsizingTextView text;

        public TextView ellipsizeHint;

        //bottom buttons panel
        public PostToolbarView toolbarView;


        public ViewHolder(View v) {
            super(v);

            id = (TextView) v.findViewById(R.id.post_id);
            date = (TextView) v.findViewById(R.id.post_date);
            overflow = (ImageButton) v.findViewById(R.id.overflow_button);

            text = (EllipsizingTextView) v.findViewById(R.id.post_text);

            ellipsizeHint = (TextView) v.findViewById(R.id.post_ellipsize_hint);

            toolbarView = (PostToolbarView) v.findViewById(R.id.post_toolbar_view);
        }
    }

    public PostsListAdapter(Cursor contentCursor,
                            Context context,
                            OnPostEventCallback eventCallback,
                            OnListScrolledDownCallback scrolledDownCallback) {
        super(contentCursor);
        this.context = context;
        this.eventCallback = eventCallback;
        this.scrolledDownCallback = scrolledDownCallback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v;
        if (viewType == VIEW_TYPE_ENTRY) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.post_entry, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_loading_entry, parent, false);
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor, final int position) {
        //footer visible
        if (position == cursor.getCount()) {
            scrolledDownCallback.onScrolledDown();
            return;
        }

        final PostEntry entry = ContentProvider.getCurrentEntry(cursor);

        //TODO: set data
//        holder.id.setText(entry.getId());
//        holder.date.setText(entry.getCreationDate());
//        holder.text.setText(entry.getText());

        //configure according to SharedPreferences
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getBoolean(context.getString(R.string.pref_shorten_long_posts_key), true))
            holder.text.setMaxLines(POST_TEXT_MAX_LINES);
        else
            holder.text.setMaxLines(Integer.MAX_VALUE);

        String textSize = prefs.getString(context.getString(R.string.pref_text_size_key), "small");
        switch (textSize) {
            case "small":
                holder.text.setTextAppearance(context, android.R.style.TextAppearance_Small);
                break;

            case "medium":
                holder.text.setTextAppearance(context, android.R.style.TextAppearance_Medium);
                break;

            case "large":
                holder.text.setTextAppearance(context, android.R.style.TextAppearance_Large);
                break;
        }
        holder.text.setTextColor(context.getResources().getColor(android.R.color.black));

        //TODO: text justification, see:
        //http://stackoverflow.com/questions/1292575/android-textview-justify-text

        // TODO: mark liked and so on

//        holder.toolbarView.setRating(entry.getRating());
//        holder.toolbarView._setBayaned(entry.getIsBayan());
//        holder.toolbarView._setFaved(entry.isFavorite());

        //TODO: set handlers

        holder.text.addEllipsizeListener(new EllipsizingTextView.EllipsizeListener() {
            @Override
            public void ellipsizeStateChanged(boolean ellipsized) {
                holder.ellipsizeHint.setVisibility(ellipsized ? View.VISIBLE : View.GONE);
            }
        });

        //set expand function both for text and hint controls
        holder.ellipsizeHint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.text.setMaxLines(Integer.MAX_VALUE);
            }
        });

        holder.text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.text.setMaxLines(Integer.MAX_VALUE);
            }
        });

        holder.overflow.setOnClickListener(new View.OnClickListener() {
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

    private void share(Context context, PostEntry entry) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, formatEntryForSharing(context, entry));
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public PostEntry get(int position) {
        return dataset.get(position);
    }

    private String formatEntryForSharing(Context context, PostEntry entry) {
        StringBuilder sb = new StringBuilder();

        String delimiter = "\n";
        String emptyLine = " \n";
        String hashTag = "#" + context.getString(R.string.app_name);

        sb.append(context.getString(R.string.app_name));
        sb.append(delimiter);

        //TODO: entry sharing

        sb.append(delimiter);
        sb.append(hashTag);

        return sb.toString();
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Cursor cursor = getCursor();
        if (cursor == null)
            return 1;
        return cursor.getCount() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < PostsListAdapter.this.getItemCount() - 1)
            return VIEW_TYPE_ENTRY;
        else
            return VIEW_TYPE_FOOTER;
    }
}