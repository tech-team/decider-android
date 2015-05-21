package org.techteam.decider.rest.api;

import android.os.Bundle;

public class PollVoteRequest {
    public static final String URL = "poll";
    private final int questionId;
    private final int pollItemId;

    public class IntentExtras {
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String POLL_ITEM_ID = "POLL_ITEM_ID";
    }

    public PollVoteRequest(int questionId, int pollItemId) {
        this.questionId = questionId;
        this.pollItemId = pollItemId;
    }

    public static PollVoteRequest fromBundle(Bundle bundle) {
        int questionId = bundle.getInt(IntentExtras.QUESTION_ID, -1);
        int pollItemId = bundle.getInt(IntentExtras.POLL_ITEM_ID, -1);
        return new PollVoteRequest(questionId, pollItemId);
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getPollItemId() {
        return pollItemId;
    }
}
