package org.techteam.decider.rest;


import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.util.HashMap;
import java.util.Map;

public class CallbacksKeeper {
    private Map<String, Map<OperationType, ServiceCallback>> callbacks = new HashMap<>();
    private static CallbacksKeeper instance = null;

    private CallbacksKeeper() {

    }

    public static CallbacksKeeper getInstance() {
        if (instance == null) {
            instance = new CallbacksKeeper();
        }
        return instance;
    }

    public CallbacksKeeper addCallback(String tag, OperationType operationType, ServiceCallback cb) {
        Map<OperationType, ServiceCallback> forTag = callbacks.get(tag);
        if (forTag == null) {
            forTag = new HashMap<>();
            callbacks.put(tag, forTag);
        }
        forTag.put(operationType, cb);
        return this;
    }

    public ServiceCallback getCallback(String tag, OperationType operationType) {
        Map<OperationType, ServiceCallback> forTag = callbacks.get(tag);
        if (forTag == null) {
            return null;
        }
        return forTag.get(operationType);
    }
}
