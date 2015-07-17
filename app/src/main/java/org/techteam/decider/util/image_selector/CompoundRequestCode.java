package org.techteam.decider.util.image_selector;

// value = |android_stuff|imageId|requestCode|
//         31            15      7           0

class CompoundRequestCode {
    private byte imageId;
    private byte requestCode;
    private int value;

    public CompoundRequestCode(byte imageId, byte requestCode) {
        this.imageId = imageId;
        this.requestCode = requestCode;

        this.value = imageId << 8;
        this.value |= requestCode;
    }

    public CompoundRequestCode(int value) {
        this.value = value;

        this.requestCode = (byte) value;
        this.imageId = (byte) (value >> 8);
    }

    public byte getImageId() {
        return imageId;
    }

    public byte getRequestCode() {
        return requestCode;
    }

    public int getValue() {
        return value;
    }
}
