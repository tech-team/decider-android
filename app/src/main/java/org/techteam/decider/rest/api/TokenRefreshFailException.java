package org.techteam.decider.rest.api;

public class TokenRefreshFailException extends Exception {
    public TokenRefreshFailException() {
        super("Token refresh failed");
    }

    public TokenRefreshFailException(String detailMessage) {
        super(detailMessage);
    }

    public TokenRefreshFailException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public TokenRefreshFailException(Throwable throwable) {
        super(throwable);
    }
}
