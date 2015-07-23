package org.techteam.decider.gui.adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.techteam.decider.R;
import org.techteam.decider.content.entities.CommentEntry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.gui.fragments.OnCommentEventCallback;
import org.techteam.decider.gui.fragments.OnMoreCommentsRequestedCallback;
import org.techteam.decider.gui.fragments.OnQuestionEventCallback;
import org.techteam.decider.gui.views.CommentView;
import org.techteam.decider.gui.views.QuestionView;

public class CommentsListAdapter
        extends CursorRecyclerViewAdapter<CommentsListAdapter.ViewHolder> {

    private QuestionEntry questionEntry;
    private final OnQuestionEventCallback onQuestionEventCallback;
    private final OnCommentEventCallback onCommentEventCallback;

    private final OnMoreCommentsRequestedCallback onMoreCommentsRequestedCallback;

    private Context context;
    private Button moreCommentsButton;

    private static final int VIEW_TYPE_QUESTION = 0;
    private static final int VIEW_TYPE_MORE_COMMENTS_BUTTON = 1;
    private static final int VIEW_TYPE_COMMENT = 2;
    private boolean feedFinished;

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
                               OnMoreCommentsRequestedCallback onMoreCommentsRequestedCallback) {
        super(contentCursor, true);
        this.context = context;
        this.questionEntry = questionEntry;
        this.onQuestionEventCallback = onQuestionEventCallback;
        this.onCommentEventCallback = onCommentEventCallback;
        this.onMoreCommentsRequestedCallback = onMoreCommentsRequestedCallback;
    }

    public void updateQuestionEntry(QuestionEntry questionEntry) {
        this.questionEntry = questionEntry;
        notifyItemChanged(0);
    }

    @Override
    public CommentsListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                            int viewType) {
        View v = null;

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

                    @Override
                    public void onReportSpamClick(QuestionEntry post) {
                        onQuestionEventCallback.onReportSpam(-1, post);
                    }
                });
                break;
            case VIEW_TYPE_MORE_COMMENTS_BUTTON:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.more_comments_button, parent, false);
                moreCommentsButton = (Button) v.findViewById(R.id.more_comments_button);
                moreCommentsButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onMoreCommentsRequestedCallback.moreCommentsRequested();
                    }
                });

                break;
            case VIEW_TYPE_COMMENT:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.fragment_comment_card, parent, false);
                break;
        }

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, Cursor cursor, final int position) {
        if (getItemViewType(position) == VIEW_TYPE_COMMENT) {
            cursor.moveToPosition(position - 2);
            CommentEntry entry = CommentEntry.fromCursor(cursor);
            holder.commentView.reuse(entry, new CommentView.EventListener() {
                @Override
                public void onReportSpamClick(CommentEntry entry) {
                    onCommentEventCallback.onReportSpam(-1, entry);
                }
            });
        } else if (getItemViewType(position) == VIEW_TYPE_MORE_COMMENTS_BUTTON) {
            updateLoadingEntry();
        }
    }

    public void setFeedFinished(boolean feedFinished) {
        this.feedFinished = feedFinished;
        updateLoadingEntry();
    }

    private void updateLoadingEntry() {
        if (moreCommentsButton != null) {
            if (feedFinished)
                moreCommentsButton.setVisibility(View.GONE);
            else
                moreCommentsButton.setVisibility(View.VISIBLE);
        }
    }

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
        else if (position == 1)
            return VIEW_TYPE_MORE_COMMENTS_BUTTON;
        else
            return VIEW_TYPE_COMMENT;
    }

    public QuestionEntry getQuestionEntry() {
        return questionEntry;
    }
}