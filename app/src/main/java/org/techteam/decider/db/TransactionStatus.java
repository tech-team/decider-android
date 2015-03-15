package org.techteam.decider.db;

public enum TransactionStatus {
    STARTED,
    FINISHED,
    ERROR;

    public static TransactionStatus toStatus(int status) {
        switch (status) {
            case 0:
                return STARTED;
            case 1:
                return FINISHED;
            default:
                return ERROR;
        }
    }

    public int toInt() {
        if (this == STARTED) {
            return 0;
        } else if (this == FINISHED) {
            return 1;
        } else {
            return 2;
        }
    }
}
