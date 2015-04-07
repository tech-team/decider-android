package org.techteam.decider.content;

import org.techteam.decider.R;

public enum ContentSection {
    NEW(R.string.new_tab, 0), POPULAR(R.string.popular_tab, 1), MY(R.string.my_tab, 2);

    int resId;
    int sectionId;

    ContentSection(int resId, int sectionId) {
        this.resId = resId;
        this.sectionId = sectionId;
    }

    public int getResId() {
        return resId;
    }

    public int toInt() {
        return this.sectionId;
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
