package org.techteam.decider.content.question;

import org.techteam.decider.content.entities.CategoryEntry;

public abstract class QuestionData {
    private String text;
    private CategoryEntry categoryEntry;
    private boolean anonymous;

    public QuestionData(String text, CategoryEntry categoryEntry, boolean anonymous) {
        this.text = text;
        this.categoryEntry = categoryEntry;
        this.anonymous = anonymous;
    }

    public String getText() {
        return text;
    }

    public CategoryEntry getCategoryEntry() {
        return categoryEntry;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public abstract String toJson();

    public abstract String createFingerprint();
}
