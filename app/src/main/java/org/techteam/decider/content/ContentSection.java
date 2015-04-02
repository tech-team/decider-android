package org.techteam.decider.content;

public enum ContentSection {
    NEW, POPULAR, MY;

    public int toInt() {
        switch (this) {
            case NEW:
                return 0;
            case POPULAR:
                return 1;
            case MY:
                return 2;
        }
        return -1;
    }

    public static ContentSection fromInt(int v) {
        switch (v) {
            case 0:
                return NEW;
            case 1:
                return POPULAR;
            case 2:
                return MY;
            default:
                throw new IllegalArgumentException("Unknown value for section");
        }
    }
}
