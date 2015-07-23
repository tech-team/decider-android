package org.techteam.decider.rest;

public enum OperationType {
    CATEGORIES_GET,
    COMMENT_CREATE,
    COMMENTS_GET(true),
    COMMENT_REPORT_SPAM,
    IMAGE_UPLOAD,
    LOGIN,
    REGISTER,
    POLL_VOTE,
    QUESTION_CREATE,
    QUESTIONS_GET(true),
    QUESTION_LIKE,
    QUESTION_REPORT_SPAM,
    USER_GET,
    USER_EDIT,
    PUSH_AUTH;

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
