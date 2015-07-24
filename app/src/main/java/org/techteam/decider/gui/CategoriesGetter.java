package org.techteam.decider.gui;

import org.techteam.decider.content.entities.CategoryEntry;

import java.util.List;

public interface CategoriesGetter {
    List<CategoryEntry> getSelectedCategories();
}
