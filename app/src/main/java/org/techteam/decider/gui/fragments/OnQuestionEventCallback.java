package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.entities.QuestionEntry;

public interface OnQuestionEventCallback {
    void onLikeClick(int entryPosition, QuestionEntry post);
    void onVoteClick(int entryPosition, QuestionEntry post, int voteId);
    void onCommentsClick(int entryPosition, QuestionEntry post);
    void onReportSpam(int entryPosition, QuestionEntry post);
}