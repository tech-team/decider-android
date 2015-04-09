package org.techteam.decider.gui.views;

import org.techteam.decider.content.entities.QuestionEntry;

public interface QuestionInteractor {
    void onCommentsClick(QuestionEntry entry);
    void onLikeClick(QuestionEntry entry);
}
