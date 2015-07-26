package org.techteam.decider.rest.api.CommentReportSpam;

import android.os.Parcel;

import org.techteam.decider.rest.api.responses.ApiErrorResponse;

public class CommentReportSpamErrorResponse extends ApiErrorResponse {

    public CommentReportSpamErrorResponse(String errorMsg) {
        super(errorMsg);
    }

    public CommentReportSpamErrorResponse(GenericErrorCode code) {
        super(code);
    }

    public CommentReportSpamErrorResponse(ServerErrorCode code) {
        super(code);
    }

    protected CommentReportSpamErrorResponse(Parcel in) {
        super(in);
    }

    public static final Creator<CommentReportSpamErrorResponse> CREATOR
            = new Creator<CommentReportSpamErrorResponse>() {
        public CommentReportSpamErrorResponse createFromParcel(Parcel in) {
            return new CommentReportSpamErrorResponse(in);
        }

        public CommentReportSpamErrorResponse[] newArray(int size) {
            return new CommentReportSpamErrorResponse[size];
        }
    };
}
