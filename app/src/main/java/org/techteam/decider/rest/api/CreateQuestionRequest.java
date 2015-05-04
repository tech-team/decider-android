package org.techteam.decider.rest.api;

import android.os.Bundle;

public class CreateQuestionRequest {
    private final String questionDataJson;

    public static final String URL = "questions";

    public class IntentExtras {
        public static final String QUESTION_DATA_JSON = "QUESTION_DATA_JSON";
    }

    public CreateQuestionRequest(String questionDataJson) {
        this.questionDataJson = questionDataJson;
    }

    public static CreateQuestionRequest fromBundle(Bundle bundle) {
        String questionDataJson = bundle.getString(IntentExtras.QUESTION_DATA_JSON, null);
        return new CreateQuestionRequest(questionDataJson);
    }

    public String getQuestionDataJson() {
        return questionDataJson;
    }
}
