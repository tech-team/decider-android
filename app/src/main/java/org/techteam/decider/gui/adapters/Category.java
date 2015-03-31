package org.techteam.decider.gui.adapters;

import org.techteam.decider.content.ContentCategory;

public class Category {
    private ContentCategory category;
    private boolean selected;

    public Category(ContentCategory category, boolean selected) {
        this.category = category;
        this.selected = selected;
    }

    public ContentCategory getCategory() {
        return category;
    }

    public void setCategory(ContentCategory category) {
        this.category = category;
    }

    public String getLabel() {
        return category.getLocalizedLabel();
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}