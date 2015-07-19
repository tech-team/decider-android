package org.techteam.decider.content;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class ImageData implements Parcelable {
    private final String originalFilename;
    private final String previewFilename;

    private Uri originalUri;
    private Uri previewUri;

    public ImageData(String originalFilename, String previewFilename) {
        this.originalFilename = originalFilename;
        this.previewFilename = previewFilename;
    }

    public ImageData(Parcel in) {
        originalFilename = in.readString();
        previewFilename = in.readString();

        originalUri = in.readParcelable(Uri.class.getClassLoader());
        previewUri = in.readParcelable(Uri.class.getClassLoader());
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public String getPreviewFilename() {
        return previewFilename;
    }

    public Uri getOriginalUri() {
        return originalUri;
    }

    public Uri getPreviewUri() {
        return previewUri;
    }

    public void setOriginalUri(Uri originalUri) {
        this.originalUri = originalUri;
    }

    public void setPreviewUri(Uri previewUri) {
        this.previewUri = previewUri;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(originalFilename);
        dest.writeString(previewFilename);

        dest.writeParcelable(originalUri, flags);
        dest.writeParcelable(previewUri, flags);
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