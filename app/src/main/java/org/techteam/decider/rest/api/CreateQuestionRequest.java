package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.question.QuestionData;

public class CreateQuestionRequest {
    private final QuestionData questionData;

    public static final String URL = "questions";

    public class IntentExtras {
        public static final String QUESTION_DATA = "QUESTION_DATA";
    }

    public CreateQuestionRequest(QuestionData questionData) {
        this.questionData = questionData;
    }

    public static CreateQuestionRequest fromBundle(Bundle bundle) {
        QuestionData questionData = bundle.getParcelable(IntentExtras.QUESTION_DATA);
        return new CreateQuestionRequest(questionData);
    }

    public QuestionData getQuestionData() {
        return questionData;
    }
}
