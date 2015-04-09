package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.entities.ContentCategory;

public interface OnCategorySelectedListener {
    void categorySelected(ContentCategory category, boolean isChecked);
}
