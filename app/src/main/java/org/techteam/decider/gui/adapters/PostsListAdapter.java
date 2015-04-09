package org.techteam.decider.gui.adapters;

import android.annotation.SuppressLint;
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
import android.widget.PopupMenu;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.fragments.OnPostEventCallback;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.views.PostInteractor;
import org.techteam.decider.gui.views.PostView;
import org.techteam.decider.gui.views.EllipsizingTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class PostsListAdapter
        extends CursorRecyclerViewAdapter<PostsListAdapter.ViewHolder> {
    private final OnPostEventCallback eventCallback;
    private final OnListScrolledDownCallback scrolledDownCallback;
    private final PostInteractor postInteractor;
    private List<QuestionEntry> dataset;

    private Context context;

    private static final int VIEW_TYPE_ENTRY = 0;
    private static final int VIEW_TYPE_FOOTER = 1;


    public void setAll(ArrayList<QuestionEntry> entries) {
        dataset.clear();
        dataset.addAll(entries);
    }

    public void addAll(ArrayList<QuestionEntry> entries) {
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

        public PostView postView;

        public ViewHolder(View v) {
            super(v);

            postView = (PostView) v.findViewById(R.id.post_view);
        }
    }

    public PostsListAdapter(Cursor contentCursor,
                            Context context,
                            OnPostEventCallback eventCallback,
                            OnListScrolledDownCallback scrolledDownCallback,
                            PostInteractor postInteractor) {
        super(contentCursor);
        this.context = context;
        this.eventCallback = eventCallback;
        this.scrolledDownCallback = scrolledDownCallback;
        this.postInteractor = postInteractor;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v;
        if (viewType == VIEW_TYPE_ENTRY) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_post_card, parent, false);
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

        QuestionEntry entry = QuestionEntry.fromCursor(cursor);


        holder.postView.reuse(entry, postInteractor);
    }

    private void share(Context context, QuestionEntry entry) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, formatEntryForSharing(context, entry));
        sendIntent.setType("text/plain");
        context.startActivity(sendIntent);
    }

    public QuestionEntry get(int position) {
        return dataset.get(position);
    }

    private String formatEntryForSharing(Context context, QuestionEntry entry) {
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