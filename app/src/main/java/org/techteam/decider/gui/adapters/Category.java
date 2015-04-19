package org.techteam.decider.gui.adapters;

import org.techteam.decider.content.entities.CategoryEntry;

public class Category {
    private CategoryEntry category;
    private boolean selected;

    public Category(CategoryEntry category, boolean selected) {
        this.category = category;
        this.selected = selected;
    }

    public CategoryEntry getCategory() {
        return category;
    }

    public void setCategory(CategoryEntry category) {
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