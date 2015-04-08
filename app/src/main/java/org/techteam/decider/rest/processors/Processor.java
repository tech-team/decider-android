package org.techteam.decider.rest.processors;

import android.content.Context;
import android.os.Bundle;

import org.techteam.decider.content.entities.TransactionEntry;
import org.techteam.decider.content.entities.TransactionStatus;
import org.techteam.decider.rest.OperationType;
import org.techteam.decider.rest.api.ApiUI;

public abstract class Processor {

    private final Context context;
    protected final ApiUI apiUI;

    public Processor(Context context) {
        this.context = context;
        apiUI = new ApiUI(context);
    }

    public Context getContext() {
        return context;
    }

    public abstract void start(OperationType operationType, String requestId, ProcessorCallback cb);

    protected void transactionStarted(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry(requestId, TransactionStatus.STARTED, operationType);
        trx.save();
    }

    protected void transactionFinished(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry(requestId, TransactionStatus.FINISHED, operationType);
        trx.save();
    }

    protected void transactionError(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry(requestId, TransactionStatus.ERROR, operationType);
        trx.save();
    }

    protected Bundle getInitialBundle() {
        return new Bundle();
    }
}
