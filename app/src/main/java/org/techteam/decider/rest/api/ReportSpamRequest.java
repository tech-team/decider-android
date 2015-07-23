package org.techteam.decider.rest.api;

public class ReportSpamRequest extends ApiRequest {
    public static final String PATH = "vote";

    private String entityType;
    private int entryPosition;
    private int entityId;

    public class IntentExtras {
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String ENTITY_ID = "ENTITY_ID";
    }

    public ReportSpamRequest(String entityType, int entryPosition, int entityId) {
        this.entityType = entityType;
        this.entryPosition = entryPosition;
        this.entityId = entityId;
    }

    public String getEntityType() {
        return entityType;
    }

    public int getEntryPosition() {
        return entryPosition;
    }

    public int getEntityId() {
        return entityId;
    }

    @Override
    public String getPath() {
        return PATH;
    }
}
