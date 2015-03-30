package org.techteam.decider.gui.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

import org.techteam.decider.content.Category;
import org.techteam.decider.content.Section;
import org.techteam.decider.content.ContentProvider;

import java.util.List;

public class ContentLoader extends CursorLoader {

    private Section section;
    private final List<Category> categories;
    private Integer entryPosition;
    private Integer insertedCount;
    private int loadIntention;

    public Integer getInsertedCount() {
        return insertedCount;
    }

    public abstract class BundleKeys {
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String INSERTED_COUNT = "INSERTED_COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
    }

    public ContentLoader(Context context, Section section, List<Category> categories, Integer entryPosition, Integer insertedCount, int loadIntention) {
        super(context);
        this.section = section;
        this.categories = categories;
        this.entryPosition = entryPosition;
        this.insertedCount = insertedCount;
        this.loadIntention = loadIntention;
    }

    @Override
    public Cursor loadInBackground() {
        return ContentProvider.getCursor(section, categories, getContext());
    }

    public Integer getEntryPosition() {
        return entryPosition;
    }

    public int getLoadIntention() {
        return loadIntention;
    }
}
