package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

import org.techteam.decider.rest.OperationType;

@Table(name="Transactions", id = BaseColumns._ID)
public class TransactionEntry extends Model {

    @Column(name="requestId", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private String requestId;

    @Column(name="status")
    private int status;

    @Column(name="operationType")
    private int operationType;

    public TransactionEntry() {
        super();
    }

    public TransactionEntry(String requestId, TransactionStatus status, OperationType operationType) {
        super();
        this.requestId = requestId;
        this.status = status.toInt();
        this.operationType = operationType.toInt();
    }

    public String getRequestId() {
        return requestId;
    }

    public int getStatus() {
        return status;
    }

    public int getOperationType() {
        return operationType;
    }

    public static void deleteAll() {
        new Delete().from(TransactionEntry.class).execute();
    }

}
