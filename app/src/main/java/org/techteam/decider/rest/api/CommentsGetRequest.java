package org.techteam.decider.rest.api;

import android.os.Bundle;

public class CommentsGetRequest {
    private final int questionId;
    private final int limit;
    private final int offset;
    private final int loadIntention;

    public static final String URL = "comments";

    public class IntentExtras {
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String LIMIT = "LIMIT";
        public static final String OFFSET = "OFFSET";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    public CommentsGetRequest(int questionId, int limit, int offset, int loadIntention) {
        this.questionId = questionId;
        this.limit = limit;
        this.offset = offset;
        this.loadIntention = loadIntention;
    }

    public static CommentsGetRequest fromBundle(Bundle bundle) {
        int questionId = bundle.getInt(IntentExtras.QUESTION_ID, -1);
        int limit = bundle.getInt(IntentExtras.LIMIT);
        int offset = bundle.getInt(IntentExtras.OFFSET);
        int loadIntention = bundle.getInt(IntentExtras.LOAD_INTENTION);

        return new CommentsGetRequest(questionId, limit, offset, loadIntention);
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getLimit() {
        return limit;
    }

    public int getOffset() {
        return offset;
    }

    public int getLoadIntention() {
        return loadIntention;
    }
}
