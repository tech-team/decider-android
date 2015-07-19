package org.techteam.decider.rest.api;

import android.os.Bundle;

import org.techteam.decider.content.ImageData;

public class ImageUploadRequest {
        public static final String URL = "images";
    private final ImageData image;
    private final int imageOrdinalId;

    public class IntentExtras {
        public static final String ORIGINAL_IMAGE = "ORIGINAL_IMAGE";
        public static final String PREVIEW_IMAGE = "PREVIEW_IMAGE";
        public static final String IMAGE_ORDINAL_ID = "IMAGE_ORDINAL_ID";
    }

    public ImageUploadRequest(ImageData image, int imageOrdinalId) {
        this.image = image;
        this.imageOrdinalId = imageOrdinalId;
    }

    public static ImageUploadRequest fromBundle(Bundle bundle) {
        String originalImage = bundle.getString(IntentExtras.ORIGINAL_IMAGE);
        String previewImage = bundle.getString(IntentExtras.PREVIEW_IMAGE);
        int imageOrdinalId = bundle.getInt(IntentExtras.IMAGE_ORDINAL_ID);
        return new ImageUploadRequest(new ImageData(originalImage, previewImage), imageOrdinalId);
    }

    public ImageData getImage() {
        return image;
    }

    public int getImageOrdinalId() {
        return imageOrdinalId;
    }
}
