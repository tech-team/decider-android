package org.techteam.decider.content.question;

import android.os.Parcel;
import android.os.Parcelable;

public class ImageData implements Parcelable {
    private final String originalFilename;
    private final String previewFilename;

    public ImageData(String originalFilename, String previewFilename) {
        this.originalFilename = originalFilename;
        this.previewFilename = previewFilename;
    }

    public ImageData(Parcel in) {
        originalFilename = in.readString();
        previewFilename = in.readString();
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getPreviewFilename() {
        return previewFilename;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalFilename);
        dest.writeString(previewFilename);
    }

    public static final Parcelable.Creator<ImageData> CREATOR
            = new Parcelable.Creator<ImageData>() {
        public ImageData createFromParcel(Parcel in) {
            return new ImageData(in);
        }

        public ImageData[] newArray(int size) {
            return new ImageData[size];
        }
    };
}