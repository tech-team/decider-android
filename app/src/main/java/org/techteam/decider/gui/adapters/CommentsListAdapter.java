package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.fragments.OnCommentEventCallback;
import org.techteam.decider.gui.fragments.OnListScrolledDownCallback;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.views.CommentInteractor;
import org.techteam.decider.gui.views.CommentView;
import org.techteam.decider.gui.views.QuestionView;

import java.util.ArrayList;
import java.util.List;

public class CommentsListAdapter
        extends CursorRecyclerViewAdapter<CommentsListAdapter.ViewHolder> {

    private QuestionEntry questionEntry;
    private final OnQuestionEventCallback onQuestionEventCallback;
    private final OnCommentEventCallback onCommentEventCallback;

    private final OnListScrolledDownCallback scrolledDownCallback;
    private List<CommentEntry> dataset = new ArrayList<>();

    private Context context;

    private static final int VIEW_TYPE_ENTRY = 0;
    private static final int VIEW_TYPE_FOOTER = 1;
    private static final int VIEW_TYPE_QUESTION = 2;


    public void setAll(ArrayList<CommentEntry> entries) {
        dataset.clear();
        dataset.addAll(entries);
    }

    public void addAll(ArrayList<CommentEntry> entries) {
        dataset.addAll(entries);
    }

    public boolean isEmpty() {
        return getItemCount() - 2 == 0; //-footer
    }

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        public CommentView commentView;

        public ViewHolder(View v) {
            super(v);

            commentView = (CommentView) v.findViewById(R.id.comment_view);
        }
    }

    public CommentsListAdapter(Cursor contentCursor,
                               Context context,
                               QuestionEntry questionEntry,
                               OnQuestionEventCallback onQuestionEventCallback,
                               OnCommentEventCallback onCommentEventCallback,
                               OnListScrolledDownCallback scrolledDownCallback) {
        super(contentCursor, true);
        this.context = context;
        this.questionEntry = questionEntry;
        this.onQuestionEventCallback = onQuestionEventCallback;
        this.onCommentEventCallback = onCommentEventCallback;
        this.scrolledDownCallback = scrolledDownCallback;
    }

    public void updateQuestionEntry(QuestionEntry questionEntry) {
        this.questionEntry = questionEntry;
        notifyItemChanged(0);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v;
        switch (viewType) {
            case VIEW_TYPE_QUESTION:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_question_card, parent, false);

                QuestionView questionView = (QuestionView) v.findViewById(R.id.post_view);
                questionView.reuse(questionEntry, new QuestionView.EventListener() {
                    @Override
                    public void onLikeClick(QuestionEntry post) {
                        onQuestionEventCallback.onLikeClick(-1, post);
                    }

                    @Override
                    public void onVoteClick(QuestionEntry post, int voteId) {
                        onQuestionEventCallback.onVoteClick(-1, post, voteId);
                    }

                    @Override
                    public void onCommentsClick(QuestionEntry post) {
                        onQuestionEventCallback.onCommentsClick(-1, post);
                    }
                });
                break;
            case VIEW_TYPE_ENTRY:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_comment_card, parent, false);
                break;
            default:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.list_loading_entry, parent, false);
                break;
        }
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_ENTRY) {
            cursor.moveToPosition(position - 1);
            CommentEntry entry = CommentEntry.fromCursor(cursor);
            holder.commentView.reuse(entry, new CommentView.EventListener() {});
        }

        //footer visible
        if (position > cursor.getCount() - 2) {
            scrolledDownCallback.onScrolledDown();
        }
    }

    public CommentEntry get(int position) {
        return dataset.get(position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Cursor cursor = getCursor();
        if (cursor == null)
            return 2;
        return cursor.getCount() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return VIEW_TYPE_QUESTION;
        else if (position < CommentsListAdapter.this.getItemCount() - 1)
            return VIEW_TYPE_ENTRY;
        else
            return VIEW_TYPE_FOOTER;
    }
}