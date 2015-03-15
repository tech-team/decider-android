package org.techteam.decider.rest;

import android.os.Parcel;
import android.os.Parcelable;

public class PendingOperation implements Parcelable {
    private OperationType operationType;
    private String operationId;

    public PendingOperation(OperationType operationType, String operationId) {
        this.operationType = operationType;
        this.operationId = operationId;
    }

    public OperationType getOperationType() {
        return operationType;
    }

    public String getOperationId() {
        return operationId;
    }

    public PendingOperation(Parcel in) {
        String opId = in.readString();
        String opType = in.readString();

        this.operationId = opId;
        this.operationType = Enum.valueOf(OperationType.class, opType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(operationType.toString());
        parcel.writeString(operationId);
    }

    public static final Creator<PendingOperation> CREATOR
            = new Creator<PendingOperation>() {
        public PendingOperation createFromParcel(Parcel in) {
            return new PendingOperation(in);
        }

        public PendingOperation[] newArray(int size) {
            return new PendingOperation[size];
        }
    };
}
