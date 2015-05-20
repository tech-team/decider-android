package org.techteam.decider.rest.api;

import android.graphics.Bitmap;
import android.os.Bundle;

public class UploadImageRequest {
    private final Bitmap image;

    public static final String URL = "questions";

    public class IntentExtras {
        public static final String IMAGE = "IMAGE";
    }

    public UploadImageRequest(Bitmap image) {
        this.image = image;
    }

    public static UploadImageRequest fromBundle(Bundle bundle) {
        Bitmap image = bundle.getParcelable(IntentExtras.IMAGE);
        return new UploadImageRequest(image);
    }

    public Bitmap getImage() {
        return image;
    }
}
