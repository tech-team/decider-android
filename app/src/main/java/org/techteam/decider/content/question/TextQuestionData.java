package org.techteam.decider.content.question;

import org.techteam.decider.content.entities.CategoryEntry;

public class TextQuestionData extends QuestionData {
    private String option1;
    private String option2;

    public TextQuestionData(String text, CategoryEntry categoryEntry, boolean anonymous, String option1, String option2) {
        super(text, categoryEntry, anonymous);
        this.option1 = option1;
        this.option2 = option2;
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    @Override
    public String toJson() {
        return null;
    }

    @Override
    public String createFingerprint() {
        throw new RuntimeException("Unimplemented method");
//        return null;
    }
}
