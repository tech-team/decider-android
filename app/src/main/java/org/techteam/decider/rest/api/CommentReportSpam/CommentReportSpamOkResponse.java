package org.techteam.decider.rest.api.CommentReportSpam;

import android.os.Parcel;

import org.techteam.decider.rest.api.responses.ApiOkResponse;

public class CommentReportSpamOkResponse extends ApiOkResponse {

    private int questionId = -1;
    private int count = 0;

    public CommentReportSpamOkResponse() {
        super();
    }

    protected CommentReportSpamOkResponse(Parcel in) {
        super(in);
        questionId = in.readInt();
        count = in.readInt();
    }

    public int getQuestionId() {
        return questionId;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(questionId);
        dest.writeInt(count);
    }

    public static final Creator<CommentReportSpamOkResponse> CREATOR
            = new Creator<CommentReportSpamOkResponse>() {
        public CommentReportSpamOkResponse createFromParcel(Parcel in) {
            return new CommentReportSpamOkResponse(in);
        }

        public CommentReportSpamOkResponse[] newArray(int size) {
            return new CommentReportSpamOkResponse[size];
        }
    };
}
