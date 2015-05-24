package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.QuestionHelper;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.views.QuestionInteractor;
import org.techteam.decider.gui.views.QuestionView;

import java.util.ArrayList;
import java.util.List;

public class PostsListAdapter
        extends CursorRecyclerViewAdapter<PostsListAdapter.ViewHolder> {
    private final ContentSection currentSection;
    private final OnQuestionEventCallback eventCallback;
    private final OnListScrolledDownCallback scrolledDownCallback;
    private final QuestionInteractor questionInteractor;
    private List<QuestionEntry> dataset = new ArrayList<>();

    private Context context;

    private OnQuestionEventCallback onQuestionEventCallback;

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

        public QuestionView questionView;

        public ViewHolder(View v) {
            super(v);

            questionView = (QuestionView) v.findViewById(R.id.post_view);
        }
    }

    public PostsListAdapter(Cursor contentCursor,
                            Context context,
                            ContentSection currentSection,
                            OnQuestionEventCallback eventCallback,
                            OnListScrolledDownCallback scrolledDownCallback,
                            QuestionInteractor questionInteractor) {
        super(contentCursor);
        this.context = context;
        this.currentSection = currentSection;
        this.eventCallback = eventCallback;
        this.scrolledDownCallback = scrolledDownCallback;
        this.questionInteractor = questionInteractor;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public PostsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v;
        if (viewType == VIEW_TYPE_ENTRY) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_question_card, parent, false);
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
        holder.questionView.setOnQuestionEventCallback(onQuestionEventCallback);

        QuestionEntry entry = QuestionHelper.fromCursor(currentSection, cursor);


        holder.questionView.reuse(entry, questionInteractor);
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

    public void setOnQuestionEventCallback(OnQuestionEventCallback cb) {
        onQuestionEventCallback = cb;
    }
}