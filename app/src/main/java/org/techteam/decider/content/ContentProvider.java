package org.techteam.decider.content;

import android.content.Context;
import android.database.Cursor;

import org.techteam.decider.content.entities.CategoryEntry;
import org.techteam.decider.content.entities.QuestionEntry;

import java.util.List;

//TODO: ContentProvider
public class ContentProvider {
    private static ContentSection contentSection;
    private static List<CategoryEntry> categories;

    public static QuestionEntry getCurrentEntry(Cursor cursor) {
        return new QuestionEntry() {

        };
    }

    public static Cursor getCursor(ContentSection contentSection, List<CategoryEntry> categories, Context context) {
        return null;
    }

    public static ContentSection getContentSection() {
        return contentSection;
    }

    public static List<CategoryEntry> getCategories() {
        return categories;
    }
}
