package org.techteam.decider.gui.views;

import org.techteam.decider.content.entities.QuestionEntry;

public interface PostInteractor {
    void onCommentsClick(QuestionEntry entry);
    void onLikeClick(QuestionEntry entry);
}
