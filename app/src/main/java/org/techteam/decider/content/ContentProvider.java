package org.techteam.decider.content;

import android.content.Context;
import android.database.Cursor;

import org.techteam.decider.content.entities.ContentCategory;
import org.techteam.decider.content.entities.QuestionEntry;

import java.util.List;

//TODO: ContentProvider
public class ContentProvider {
    private static ContentSection contentSection;
    private static List<ContentCategory> categories;

    public static QuestionEntry getCurrentEntry(Cursor cursor) {
        return new QuestionEntry() {

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
