package org.techteam.decider.content;

public class ContentCategory {
    private String localizedLabel;
    private int uid;

    public ContentCategory(String localizedLabel, int uid) {
        this.localizedLabel = localizedLabel;
        this.uid = uid;
    }

    public String getLocalizedLabel() {
        return localizedLabel;
    }

    public int getUid() {
        return uid;
    }
}
