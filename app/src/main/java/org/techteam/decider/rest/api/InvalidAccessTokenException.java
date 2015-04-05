package org.techteam.decider.rest.api;

public class InvalidAccessTokenException extends Exception {
    public InvalidAccessTokenException() {
        super("Access token is invalid");
    }

    public InvalidAccessTokenException(String detailMessage) {
        super(detailMessage);
    }

    public InvalidAccessTokenException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public InvalidAccessTokenException(Throwable throwable) {
        super(throwable);
    }
}
