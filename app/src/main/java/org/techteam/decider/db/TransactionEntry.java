package org.techteam.decider.db;

import org.techteam.decider.content.Entry;
import org.techteam.decider.rest.OperationType;

public class TransactionEntry extends Entry {
    private String id;
    private OperationType operationType;
    private TransactionStatus status;

    public TransactionEntry(String id, OperationType operationType, TransactionStatus status) {
        this.id = id;
        this.operationType = operationType;
        this.status = status;
    }

    public TransactionEntry() {
    }

    public String getId() {
        return id;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public TransactionEntry setId(String id) {
        this.id = id;
        return this;
    }

    public TransactionEntry setOperationType(int operationType) {
        this.operationType = OperationType.fromInt(operationType);
        return this;
    }
    public TransactionEntry setOperationType(OperationType operationType) {
        this.operationType = operationType;
        return this;
    }

    public TransactionEntry setStatus(int status) {
        this.status = TransactionStatus.toStatus(status);
        return this;
    }
    public TransactionEntry setStatus(TransactionStatus status) {
        this.status = status;
        return this;
    }
}
