package org.techteam.decider.rest.api;

import android.os.Bundle;

public class QuestionReportSpamRequest extends ReportSpamRequest {
    private static final String ENTITY_TYPE = "question";

    public QuestionReportSpamRequest(int entryPosition, int entityId) {
        super(ENTITY_TYPE, entryPosition, entityId);
    }

    public static QuestionReportSpamRequest fromBundle(Bundle bundle) {
        int entryPosition = bundle.getInt(ReportSpamRequest.IntentExtras.ENTRY_POSITION, -1);
        int entityId = bundle.getInt(ReportSpamRequest.IntentExtras.ENTITY_ID, -1);
        return new QuestionReportSpamRequest(entryPosition, entityId);
    }
}
