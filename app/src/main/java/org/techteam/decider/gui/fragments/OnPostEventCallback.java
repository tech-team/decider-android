package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.PostEntry;

public interface OnPostEventCallback {
    void onLike(PostEntry post);
    void onVote(PostEntry post, int voteId);
}