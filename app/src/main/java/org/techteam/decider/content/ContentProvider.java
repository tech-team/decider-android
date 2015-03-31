package org.techteam.decider.content;

import android.content.Context;
import android.database.Cursor;

import java.util.List;

//TODO: ContentProvider
public class ContentProvider {
    private static ContentSection contentSection;
    private static List<ContentCategory> categories;

    public static PostEntry getCurrentEntry(Cursor cursor) {
        return new PostEntry() {

        };
    }

    public static Cursor getCursor(ContentSection contentSection, List<ContentCategory> categories, Context context) {
        return null;
    }

    public static ContentSection getContentSection() {
        return contentSection;
    }

    public static List<ContentCategory> getCategories() {
        return categories;
    }
}
