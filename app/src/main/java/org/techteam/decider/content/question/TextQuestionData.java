package org.techteam.decider.content.question;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONObject;

public class TextQuestionData extends QuestionData implements Parcelable {
    private String option1;
    private String option2;

    public TextQuestionData(String text, int categoryEntryUid, boolean anonymous, String option1, String option2) {
        super(text, categoryEntryUid, anonymous);
        this.option1 = option1;
        this.option2 = option2;
    }

    public TextQuestionData(Parcel in) {
        super(in);
        option1 = in.readString();
        option2 = in.readString();
    }

    public String getOption1() {
        return option1;
    }

    public String getOption2() {
        return option2;
    }

    @Override
    public JSONObject toJson() {
        return null;
    }

    @Override
    public String createFingerprint() {
        throw new RuntimeException("Unimplemented method");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(option1);
        dest.writeString(option2);
    }

    public static final Parcelable.Creator<TextQuestionData> CREATOR
            = new Parcelable.Creator<TextQuestionData>() {
        public TextQuestionData createFromParcel(Parcel in) {
            return new TextQuestionData(in);
        }

        public TextQuestionData[] newArray(int size) {
            return new TextQuestionData[size];
        }
    };
}
