package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techteam.decider.R;
import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.QuestionHelper;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.views.QuestionView;

import java.util.ArrayList;
import java.util.List;

public class QuestionsListAdapter
        extends CursorRecyclerViewAdapter<QuestionsListAdapter.ViewHolder> {
    private final ContentSection currentSection;
    private final OnQuestionEventCallback onQuestionEventCallback;
    private final OnListScrolledDownCallback scrolledDownCallback;
    private List<QuestionEntry> dataset = new ArrayList<>();

    private Context context;

    private static final int VIEW_TYPE_ENTRY = 0;
    private static final int VIEW_TYPE_FOOTER = 1;

    private boolean feedFinished;
    private View loadingView;

    public QuestionsListAdapter(Cursor contentCursor,
                                Context context,
                                ContentSection currentSection,
                                OnQuestionEventCallback eventCallback,
                                OnListScrolledDownCallback scrolledDownCallback) {
        super(contentCursor);
        this.context = context;
        this.currentSection = currentSection;
        this.onQuestionEventCallback = eventCallback;
        this.scrolledDownCallback = scrolledDownCallback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public QuestionsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v;
        if (viewType == VIEW_TYPE_ENTRY) {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_question_card, parent, false);
        } else {
            v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.list_loading_entry, parent, false);
            loadingView = v.findViewById(R.id.collapsible_view);
            updateLoadingEntry();
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_FOOTER) {
            scrolledDownCallback.onScrolledDown();
            return;
        }

        QuestionEntry entry = QuestionHelper.fromCursor(currentSection, cursor);
        holder.questionView.reuse(entry, new QuestionView.EventListener() {
            @Override
            public void onLikeClick(QuestionEntry post) {
                onQuestionEventCallback.onLikeClick(position, post);
            }

            @Override
            public void onVoteClick(QuestionEntry post, int voteId) {
                onQuestionEventCallback.onVoteClick(position, post, voteId);
            }

            @Override
            public void onCommentsClick(QuestionEntry post) {
                onQuestionEventCallback.onCommentsClick(position, post);
            }
        });
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
        if (position < QuestionsListAdapter.this.getItemCount() - 1)
            return VIEW_TYPE_ENTRY;
        else
            return VIEW_TYPE_FOOTER;
    }

    public void setFeedFinished(boolean feedFinished) {
        this.feedFinished = feedFinished;
        updateLoadingEntry();
    }

    private void updateLoadingEntry() {
        if (feedFinished)
            loadingView.setVisibility(View.GONE);
        else
            loadingView.setVisibility(View.VISIBLE);
    }


    public static class ViewHolder
            extends RecyclerView.ViewHolder {
        public QuestionView questionView;

        public ViewHolder(View v) {
            super(v);

            questionView = (QuestionView) v.findViewById(R.id.post_view);
        }
    }
}