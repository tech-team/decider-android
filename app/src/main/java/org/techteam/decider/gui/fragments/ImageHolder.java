package org.techteam.decider.gui.fragments;

import android.net.Uri;
import android.widget.ImageView;

class ImageHolder {
    private Uri source;
    private Uri cropped;
    private ImageView imageView;

    public ImageHolder(ImageView imageView) {
        this.imageView = imageView;
    }

    public Uri getSource() {
        return source;
    }

    public void setSource(Uri source) {
        this.source = source;
    }

    public Uri getCropped() {
        return cropped;
    }

    public void setCropped(Uri cropped) {
        this.cropped = cropped;
    }

    public ImageView getImageView() {
        return imageView;
    }
}
