package org.techteam.decider.rest.api.CommentCreate;

import android.os.Parcel;

import org.techteam.decider.rest.api.responses.ApiOkResponse;

public class CommentCreateOkResponse extends ApiOkResponse {

    private int questionId = -1;
    private int count = 0;

    public CommentCreateOkResponse() {
        super();
    }

    protected CommentCreateOkResponse(Parcel in) {
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

    public static final Creator<CommentCreateOkResponse> CREATOR
            = new Creator<CommentCreateOkResponse>() {
        public CommentCreateOkResponse createFromParcel(Parcel in) {
            return new CommentCreateOkResponse(in);
        }

        public CommentCreateOkResponse[] newArray(int size) {
            return new CommentCreateOkResponse[size];
        }
    };
}
