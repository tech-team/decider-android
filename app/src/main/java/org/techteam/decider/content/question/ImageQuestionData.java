package org.techteam.decider.content.question;


import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.ImageData;

public class ImageQuestionData extends QuestionData implements Parcelable {

    private ImageData picture1;
    private ImageData picture2;

    public ImageQuestionData() {
    }

    public ImageQuestionData(String text, int categoryEntryUid, boolean anonymous, ImageData picture1, ImageData picture2) {
        super(text, categoryEntryUid, anonymous);
        this.picture1 = picture1;
        this.picture2 = picture2;
    }

    public ImageQuestionData(Parcel in) {
        super(in);
        picture1 = in.readParcelable(ImageData.class.getClassLoader());
        picture2 = in.readParcelable(ImageData.class.getClassLoader());
    }

    public ImageData getPicture1() {
        return picture1;
    }

    public ImageData getPicture2() {
        return picture2;
    }

    public void setPicture1(ImageData picture1) {
        this.picture1 = picture1;
    }

    public void setPicture2(ImageData picture2) {
        this.picture2 = picture2;
    }

    @Override
    public JSONObject toJson() throws JSONException {
//        JSONObject obj = super.toJson();
//        JSONArray poll = new JSONArray();
//        String[] pictures = new String[] { picture1Id, picture2Id };
//        for (String p : pictures) {
//            JSONObject pollItem = new JSONObject();
//            pollItem.put("text", "STUB");
//            pollItem.put("image_uid", p);
//            poll.put(pollItem);
//        }
//        obj.put("poll", poll);
//        return obj;
        return null;
    }

    @Override
    public String createFingerprint() {
        String fp = super.createFingerprint();
        fp += "__" + picture1.getOriginalFilename().replace("__", "") + "__" + picture2.getOriginalFilename().replace("__", "");
        return fp;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(picture1, flags);
        dest.writeParcelable(picture2, flags);
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
