package org.techteam.decider.gcm.data;

public enum PushCode {
    NEW_COMMENT(1000),
    NEW_COMMENT_LIKE(1001),
    NEW_VOTE(1002),
    MORE_COMMENTS(1003),
    MORE_VOTES(1004);

    private static PushCode[] cachedValues = values();
    int code;

    PushCode(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static PushCode fromCode(int code) {
        for (PushCode pushCode : cachedValues) {
            if (pushCode.getCode() == code) {
                return pushCode;
            }
        }
        throw new RuntimeException("Unknown push code: " + code);
    }
}
