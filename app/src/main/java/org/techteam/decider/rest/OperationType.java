package org.techteam.decider.rest;

public enum OperationType {
    GET_QUESETIONS,
    BASH_VOTE,
    IT_VOTE;

    public static OperationType fromInt(int status) {
        switch (status) {
            case 0:
                return GET_QUESETIONS;
            case 1:
                return BASH_VOTE;
            default:
                return IT_VOTE;
        }
    }

    public int toInt() {
        if (this == GET_QUESETIONS) {
            return 0;
        } else if (this == BASH_VOTE) {
            return 1;
        } else {
            return 2;
        }
    }
}
