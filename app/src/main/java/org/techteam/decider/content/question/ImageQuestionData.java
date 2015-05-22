package org.techteam.decider.content.question;


import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ImageQuestionData extends QuestionData implements Parcelable {

    private String picture1Id;
    private String picture2Id;

    public ImageQuestionData() {
    }

    public ImageQuestionData(String text, int categoryEntryUid, boolean anonymous, String picture1Id, String picture2Id) {
        super(text, categoryEntryUid, anonymous);
        this.picture1Id = picture1Id;
        this.picture2Id = picture2Id;
    }

    public ImageQuestionData(Parcel in) {
        super(in);
        picture1Id = in.readString();
        picture2Id = in.readString();
    }

    public String getPicture1() {
        return picture1Id;
    }

    public String getPicture2() {
        return picture2Id;
    }

    public void setPicture1(String picture1Id) {
        this.picture1Id = picture1Id;
    }

    public void setPicture2(String picture2Id) {
        this.picture2Id = picture2Id;
    }

    @Override
    public JSONObject toJson() throws JSONException {
        JSONObject obj = super.toJson();
        JSONArray poll = new JSONArray();
        String[] pictures = new String[] { picture1Id, picture2Id };
        for (String p : pictures) {
            JSONObject pollItem = new JSONObject();
            pollItem.put("text", "STUB");
            pollItem.put("image_uid", p);
            poll.put(pollItem);
        }
        obj.put("poll", poll);
        return obj;
    }

    @Override
    public String createFingerprint() {
        String fp = super.createFingerprint();
        fp += "_" + picture1Id + "_" + picture2Id;
        return fp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(picture1Id);
        dest.writeString(picture2Id);
    }

    public static final Parcelable.Creator<ImageQuestionData> CREATOR
            = new Parcelable.Creator<ImageQuestionData>() {
        public ImageQuestionData createFromParcel(Parcel in) {
            return new ImageQuestionData(in);
        }

        public ImageQuestionData[] newArray(int size) {
            return new ImageQuestionData[size];
        }
    };
}
