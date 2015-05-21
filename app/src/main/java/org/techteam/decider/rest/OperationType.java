package org.techteam.decider.rest;

public enum OperationType {
    GET_QUESTIONS,
    GET_CATEGORIES,
    LOGIN_REGISTER,
    CREATE_QUESTION,
    UPLOAD_IMAGE,
    POLL_VOTE,
    GET_COMMENTS;

    private static OperationType[] cachedValues = values();

    public static OperationType fromInt(int i) {
        return cachedValues[i];
    }

    public int toInt() {
        return ordinal();
    }
}
