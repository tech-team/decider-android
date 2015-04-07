package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.entities.QuestionEntry;

public interface OnPostEventCallback {
    void onLike(QuestionEntry post);
    void onVote(QuestionEntry post, int voteId);
}