package org.techteam.decider.util.image_selector;

// value = |imageId|requestCode|
class CompoundRequestCode {
    private short imageId;
    private short requestCode;
    private int value;

    public CompoundRequestCode(short imageId, short requestCode) {
        this.imageId = imageId;
        this.requestCode = requestCode;

        this.value = imageId << 16;
        this.value |= requestCode;
    }

    public CompoundRequestCode(int value) {
        this.value = value;

        this.requestCode = (short) value;
        this.imageId = (short) (value >> 16);
    }

    public short getImageId() {
        return imageId;
    }

    public short getRequestCode() {
        return requestCode;
    }

    public int getValue() {
        return value;
    }
}
