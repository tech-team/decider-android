package org.techteam.decider.rest.api.responses;

import android.os.Parcel;
import android.os.Parcelable;

public abstract class ApiResponse implements Parcelable {

    private boolean error = false;
    private String errorMsg = null;

    protected ApiResponse(boolean error, String errorMsg) {
        this.error = error;
        this.errorMsg = errorMsg;
    }

    protected ApiResponse(Parcel in) {
        boolean[] b = new boolean[1];
        in.readBooleanArray(b);
        this.error = b[0];
        this.errorMsg = in.readString();
    }

    public boolean isError() {
        return error;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeBooleanArray(new boolean[] {error});
        dest.writeString(errorMsg);
    }
}
