package org.techteam.decider.rest.api;

import android.net.Uri;
import android.os.Bundle;

public class UploadImageRequest {
    public static class Image {
        private final Uri originalFilename;
        private final Uri previewFilename;

        public Image(Uri originalFilename, Uri previewFilename) {
            this.originalFilename = originalFilename;
            this.previewFilename = previewFilename;
        }

        public Uri getOriginalFilename() {
            return originalFilename;
        }

        public Uri getPreviewFilename() {
            return previewFilename;
        }
    }

    public static final String URL = "images";
    private final Image image;

    public class IntentExtras {
        public static final String ORIGINAL_IMAGE = "ORIGINAL_IMAGE";
        public static final String PREVIEW_IMAGE = "PREVIEW_IMAGE";
    }

    public UploadImageRequest(Image image) {
        this.image = image;
    }

    public static UploadImageRequest fromBundle(Bundle bundle) {
        Uri originalImage = bundle.getParcelable(IntentExtras.ORIGINAL_IMAGE);
        Uri previewImage = bundle.getParcelable(IntentExtras.PREVIEW_IMAGE);
        return new UploadImageRequest(new Image(originalImage, previewImage));
    }

    public Image getImage() {
        return image;
    }
}
