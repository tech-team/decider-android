package org.techteam.decider.gui.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

import com.activeandroid.content.ContentProvider;

import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.QuestionHelper;

public class ContentLoader extends CursorLoader {

    private ContentSection contentSection;
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
        public static final String SECTION = "SECTION";
    }

    public ContentLoader(Context context, ContentSection contentSection, Integer entryPosition, Integer insertedCount, int loadIntention) {
        super(context);
        this.contentSection = contentSection;
        this.entryPosition = entryPosition;
        this.insertedCount = insertedCount;
        this.loadIntention = loadIntention;
    }

    @Override
    public Cursor loadInBackground() {
        Uri uri = ContentProvider.createUri(QuestionHelper.getClass(contentSection), null);
        return getContext().getContentResolver().query(uri, null, null, null, null);
    }

    public Integer getEntryPosition() {
        return entryPosition;
    }

    public int getLoadIntention() {
        return loadIntention;
    }
}
