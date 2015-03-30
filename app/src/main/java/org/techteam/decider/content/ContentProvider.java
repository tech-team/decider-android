package org.techteam.decider.content;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

//TODO: ContentProvider
public class ContentProvider {
    private static Section section;
    private static List<Category> categories;

    public static PostEntry getCurrentEntry(Cursor cursor) {
        return new PostEntry() {

        };
    }

    public static Cursor getCursor(Section section, List<Category> categories, Context context) {
        return null;
    }

    public static Section getSection() {
        return section;
    }

    public static List<Category> getCategories() {
        return categories;
    }
}
