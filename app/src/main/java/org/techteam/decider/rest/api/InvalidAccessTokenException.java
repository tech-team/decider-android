package org.techteam.decider.rest.api;

public class InvalidAccessTokenException extends Exception {

    public static final String ACCESS_TOKEN_IS_INVALID = "Access token is invalid.";

    public InvalidAccessTokenException() {
        super(ACCESS_TOKEN_IS_INVALID);
    }

    public InvalidAccessTokenException(String detailMessage) {
        super(ACCESS_TOKEN_IS_INVALID + detailMessage);
    }

    public InvalidAccessTokenException(String detailMessage, Throwable throwable) {
        super(ACCESS_TOKEN_IS_INVALID + detailMessage, throwable);
    }

    public InvalidAccessTokenException(Throwable throwable) {
        super(throwable);
    }
}
