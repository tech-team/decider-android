package org.techteam.decider.rest.api;

public class ServerErrorException extends Exception {
    public static final String DEFAULT_MESSAGE = "Server error";

    private int code;

    public ServerErrorException() {
        super(DEFAULT_MESSAGE);
    }

    public ServerErrorException(int code) {
        super(DEFAULT_MESSAGE + ". code = " + Integer.toString(code));
        this.code = code;
    }

    public ServerErrorException(String detailMessage) {
        super(detailMessage);
    }

    public ServerErrorException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServerErrorException(Throwable throwable) {
        super(throwable);
    }

    public int getCode() {
        return code;
    }
}
