package org.techteam.decider.util.image_selector;

import android.net.Uri;
import android.widget.ImageView;

public class ImageHolder {
    private Uri source;
    private Uri preview;
    private int ordinal;  // left or right
    private String uid;
    private ImageView imageView;

    public ImageHolder(ImageView imageView) {
        this.imageView = imageView;
        this.ordinal = imageView.getId();
    }

    public Uri getSource() {
        return source;
    }

    public void setSource(Uri source) {
        this.source = source;
    }

    public Uri getPreview() {
        return preview;
    }

    public void setPreview(Uri preview) {
        this.preview = preview;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
