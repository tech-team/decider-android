package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.entities.CommentEntry;

public interface OnCommentEventCallback {
    void onReportSpam(int entryPosition, CommentEntry entry);
}