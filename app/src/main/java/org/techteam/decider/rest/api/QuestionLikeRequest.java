package org.techteam.decider.rest.api;

import android.os.Bundle;

public class QuestionLikeRequest extends EntityVoteRequest {
    private static final String ENTITY_TYPE = "question";

    public QuestionLikeRequest(int entryPosition, int entityId) {
        super(ENTITY_TYPE, entryPosition, entityId);
    }

    public static QuestionLikeRequest fromBundle(Bundle bundle) {
        int entryPosition = bundle.getInt(IntentExtras.ENTRY_POSITION, -1);
        int entityId = bundle.getInt(IntentExtras.ENTITY_ID, -1);
        return new QuestionLikeRequest(entryPosition, entityId);
    }
}
