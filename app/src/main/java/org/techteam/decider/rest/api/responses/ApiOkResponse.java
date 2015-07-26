package org.techteam.decider.rest.api.responses;

import android.os.Parcel;

public abstract class ApiOkResponse extends ApiResponse {

    public ApiOkResponse() {
        super(false, null);
    }

    protected ApiOkResponse(Parcel in) {
        super(in);
    }
}
