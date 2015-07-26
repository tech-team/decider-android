package org.techteam.decider.rest.api.CommentCreate;

import android.os.Parcel;

import org.techteam.decider.rest.api.responses.ApiErrorResponse;

public class CommentCreateErrorResponse extends ApiErrorResponse {

    public CommentCreateErrorResponse(String errorMsg) {
        super(errorMsg);
    }

    public CommentCreateErrorResponse(GenericErrorCode code) {
        super(code);
    }

    public CommentCreateErrorResponse(ServerErrorCode code) {
        super(code);
    }

    protected CommentCreateErrorResponse(Parcel in) {
        super(in);
    }

    public static final Creator<CommentCreateErrorResponse> CREATOR
            = new Creator<CommentCreateErrorResponse>() {
        public CommentCreateErrorResponse createFromParcel(Parcel in) {
            return new CommentCreateErrorResponse(in);
        }

        public CommentCreateErrorResponse[] newArray(int size) {
            return new CommentCreateErrorResponse[size];
        }
    };
}
