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
    private final int imageOrdinalId;

    public class IntentExtras {
        public static final String ORIGINAL_IMAGE = "ORIGINAL_IMAGE";
        public static final String PREVIEW_IMAGE = "PREVIEW_IMAGE";
        public static final String IMAGE_ORDINAL_ID = "IMAGE_ORDINAL_ID";
    }

    public UploadImageRequest(Image image, int imageOrdinalId) {
        this.image = image;
        this.imageOrdinalId = imageOrdinalId;
    }

    public static UploadImageRequest fromBundle(Bundle bundle) {
        String originalImage = bundle.getString(IntentExtras.ORIGINAL_IMAGE);
        String previewImage = bundle.getString(IntentExtras.PREVIEW_IMAGE);
        int imageOrdinalId = bundle.getInt(IntentExtras.IMAGE_ORDINAL_ID);
        return new UploadImageRequest(new Image(originalImage, previewImage), imageOrdinalId);
    }

    public Image getImage() {
        return image;
    }

    public int getImageOrdinalId() {
        return imageOrdinalId;
    }
}
