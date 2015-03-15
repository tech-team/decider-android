package org.techteam.decider.rest.service_helper;

public class ServiceHelperNotInitializedException extends RuntimeException {
    public ServiceHelperNotInitializedException() {
        super("ServiceHelper is not initialized. Use init() method");
    }

    public ServiceHelperNotInitializedException(String detailMessage) {
        super(detailMessage);
    }

    public ServiceHelperNotInitializedException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public ServiceHelperNotInitializedException(Throwable throwable) {
        super(throwable);
    }
}
