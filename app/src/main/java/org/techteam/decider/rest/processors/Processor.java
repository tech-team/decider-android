package org.techteam.decider.rest.processors;

import android.content.Context;

import org.techteam.decider.db.TransactionEntry;
import org.techteam.decider.db.TransactionStatus;
import org.techteam.decider.db.resolvers.AbstractContentResolver;
import org.techteam.decider.db.resolvers.ExtraResolver;
import org.techteam.decider.db.resolvers.TransactionsResolver;
import org.techteam.decider.rest.OperationType;

public abstract class Processor {

    private final Context context;
    private final TransactionsResolver transactioner;

    public Processor(Context context) {
        this.context = context;
        transactioner = (TransactionsResolver) AbstractContentResolver.getResolver(ExtraResolver.TRANSACTIONS);
    }

    public Context getContext() {
        return context;
    }

    public abstract void start(OperationType operationType, String requestId, ProcessorCallback cb);

    protected void transactionStarted(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry().setId(requestId)
                                                     .setOperationType(operationType)
                                                     .setStatus(TransactionStatus.STARTED);
        transactioner.insert(getContext(), trx);
    }

    protected void transactionFinished(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry().setId(requestId)
                                                     .setOperationType(operationType)
                                                     .setStatus(TransactionStatus.FINISHED);
        transactioner.insert(getContext(), trx);
    }

    protected void transactionError(OperationType operationType, String requestId) {
        TransactionEntry trx = new TransactionEntry().setId(requestId)
                                                     .setOperationType(operationType)
                                                     .setStatus(TransactionStatus.ERROR);
        transactioner.insert(getContext(), trx);
    }
}
