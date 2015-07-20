package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import org.techteam.decider.content.entities.TransactionEntry;
import org.techteam.decider.content.entities.TransactionStatus;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;

public abstract class RequestProcessor<T> extends Processor {

    private final T request;

    public RequestProcessor(Context context, T request) {
        super(context);
        this.request = request;
    }

    public T getRequest() {
        return request;
    }
}
