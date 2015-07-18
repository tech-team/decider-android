package org.techteam.decider.rest;

public enum OperationType {
    GET_QUESTIONS(true),
    GET_CATEGORIES,
    LOGIN,
    REGISTER,
    CREATE_QUESTION,
    UPLOAD_IMAGE,
    POLL_VOTE,
    GET_COMMENTS(true),
    CREATE_COMMENT,
    GET_USER;

    private boolean canRefresh = false;

    OperationType(boolean canRefresh) {
        this.canRefresh = canRefresh;
    }

    OperationType() {
    }

    public boolean canRefresh() {
        return canRefresh;
    }

    private static OperationType[] cachedValues = values();

    public static OperationType fromInt(int i) {
        return cachedValues[i];
    }

    public int toInt() {
        return ordinal();
    }
}
