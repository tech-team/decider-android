package org.techteam.decider.rest;

public enum OperationType {
    GET_QUESETIONS;

    public static OperationType fromInt(int status) {
        switch (status) {
            case 0:
                return GET_QUESETIONS;
            default:
                throw new IllegalArgumentException("Unknown status");
        }
    }

    public int toInt() {
        if (this == GET_QUESETIONS) {
            return 0;
        } else {
            return 2;
        }
    }
}
