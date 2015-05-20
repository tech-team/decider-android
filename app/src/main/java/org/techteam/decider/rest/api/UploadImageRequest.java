package org.techteam.decider.rest.api;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

public class UploadImageRequest {
    public static class Image {
        private final String originalFilename;
        private final String previewFilename;

        public Image(String originalFilename, String previewFilename) {
            this.originalFilename = originalFilename;
            this.previewFilename = previewFilename;
        }

        public String getOriginalFilename() {
            return originalFilename;
        }

        public String getPreviewFilename() {
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
        String originalImage = bundle.getString(IntentExtras.ORIGINAL_IMAGE);
        String previewImage = bundle.getString(IntentExtras.PREVIEW_IMAGE);
        return new UploadImageRequest(new Image(originalImage, previewImage));
    }

    public Image getImage() {
        return image;
    }
}
