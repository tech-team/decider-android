package org.techteam.decider.content.question;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

public abstract class QuestionData implements Parcelable {
    private String text;
    private int categoryEntryUid;
    private boolean anonymous;

    public QuestionData() {

    }

    public QuestionData(String text, int categoryEntryUid, boolean anonymous) {
        this.text = text;
        this.categoryEntryUid = categoryEntryUid;
        this.anonymous = anonymous;
    }

    public QuestionData(Parcel in) {
        text = in.readString();
        categoryEntryUid = in.readInt();
        boolean[] b = new boolean[1];
        in.readBooleanArray(b);
        anonymous = b[0];
    }

    public String getText() {
        return text;
    }

    public int getCategoryEntryUid() {
        return categoryEntryUid;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setCategoryEntryUid(int categoryEntryUid) {
        this.categoryEntryUid = categoryEntryUid;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("text", text.trim());
        obj.put("category_id", categoryEntryUid);
        obj.put("is_anonymous", anonymous);
        return obj;
    }

    public String createFingerprint() {
        return text.length() + "__" + Integer.toString(categoryEntryUid) + "__" + Boolean.toString(anonymous);
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(text);
        dest.writeInt(categoryEntryUid);
        dest.writeBooleanArray(new boolean[] {anonymous});
    }
}
