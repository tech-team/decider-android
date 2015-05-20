package org.techteam.decider.rest;

public enum OperationType {
    GET_QUESTIONS,
    GET_CATEGORIES,
    REGISTER,
    LOGIN,
    CREATE_QUESTION;

    private static OperationType[] cachedValues = values();

    public static OperationType fromInt(int i) {
        return cachedValues[i];
    }

    public int toInt() {
        return ordinal();
    }
}
