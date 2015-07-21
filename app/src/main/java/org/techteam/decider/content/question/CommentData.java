package org.techteam.decider.content.question;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public class CommentData implements Parcelable {
    public static final int NO_LAST_COMMENT_ID = -1;

    private String text;
    private int questionId;
    private int lastCommentId;
    private boolean anonymous;

    public CommentData() {

    }

    public CommentData(String text, int questionId, int lastCommentId, boolean anonymous) {
        this.text = text;
        this.questionId = questionId;
        this.lastCommentId = lastCommentId;
        this.anonymous = anonymous;
    }

    public CommentData(Parcel in) {
        text = in.readString();
        questionId = in.readInt();
        lastCommentId = in.readInt();
        boolean[] b = new boolean[1];
        in.readBooleanArray(b);
        anonymous = b[0];
    }

    public String getText() {
        return text;
    }

    public int getQuestionId() {
        return questionId;
    }

    public int getLastCommentId() {
        return lastCommentId;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setQuestionId(int questionId) {
        this.questionId = questionId;
    }

    public void setLastCommentId(int lastCommentId) {
        this.lastCommentId = lastCommentId;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("text", text.trim());
        obj.put("question_id", questionId);
        obj.put("last_seen_id", lastCommentId);
        obj.put("is_anonymous", anonymous);
        return obj;
    }

    public String createFingerprint() {
        return text.length() + "_" + Integer.toString(questionId)
                + "_" + Integer.toString(lastCommentId)
                +  "_" + Boolean.toString(anonymous);
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(questionId);
        dest.writeInt(lastCommentId);
        dest.writeBooleanArray(new boolean[] {anonymous});
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<CommentData> CREATOR
            = new Parcelable.Creator<CommentData>() {
        public CommentData createFromParcel(Parcel in) {
            return new CommentData(in);
        }

        public CommentData[] newArray(int size) {
            return new CommentData[size];
        }
    };
}
