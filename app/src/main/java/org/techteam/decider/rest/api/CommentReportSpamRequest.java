package org.techteam.decider.rest.api;

import android.os.Bundle;

public class CommentReportSpamRequest extends ReportSpamRequest {
    private static final String ENTITY_TYPE = "comment";

    public CommentReportSpamRequest(int entryPosition, int entityId) {
        super(ENTITY_TYPE, entryPosition, entityId);
    }

    public static CommentReportSpamRequest fromBundle(Bundle bundle) {
        int entryPosition = bundle.getInt(IntentExtras.ENTRY_POSITION, -1);
        int entityId = bundle.getInt(IntentExtras.ENTITY_ID, -1);
        return new CommentReportSpamRequest(entryPosition, entityId);
    }
}
