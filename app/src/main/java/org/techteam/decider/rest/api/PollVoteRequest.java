package org.techteam.decider.rest.api;

import android.os.Bundle;

public class PollVoteRequest {
    public static final String URL = "poll";

    private final int entryPosition;
    private final int questionId;
    private final int pollItemId;

    public class IntentExtras {
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String POLL_ITEM_ID = "POLL_ITEM_ID";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
    }

    public PollVoteRequest(int entryPosition, int questionId, int pollItemId) {
        this.entryPosition = entryPosition;
        this.questionId = questionId;
        this.pollItemId = pollItemId;
    }

    public static PollVoteRequest fromBundle(Bundle bundle) {
        int entryPosition = bundle.getInt(IntentExtras.ENTRY_POSITION, -1);
        int questionId = bundle.getInt(IntentExtras.QUESTION_ID, -1);
        int pollItemId = bundle.getInt(IntentExtras.POLL_ITEM_ID, -1);
        return new PollVoteRequest(entryPosition, questionId, pollItemId);
    }

    public int getEntryPosition() {
        return entryPosition;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getPollItemId() {
        return pollItemId;
    }
}
