package org.techteam.decider.gui.fragments;

import org.techteam.decider.content.entities.CategoryEntry;

public interface OnCategorySelectedListener {
    void categorySelected(CategoryEntry category, boolean isChecked);
}
