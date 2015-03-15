package org.techteam.decider.rest;


import org.techteam.decider.rest.service_helper.ServiceCallback;

import java.util.HashMap;
import java.util.Map;

public class CallbacksKeeper {
    private Map<OperationType, ServiceCallback> callbacks = new HashMap<>();

    public CallbacksKeeper() {
    }

    public CallbacksKeeper addCallback(OperationType operationType, ServiceCallback cb) {
        callbacks.put(operationType, cb);
        return this;
    }

    public ServiceCallback getCallback(OperationType operationType) {
        return callbacks.get(operationType);
    }
}
