package org.techteam.decider.gui.loaders;

import android.content.Context;
import android.support.v4.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

import com.activeandroid.content.ContentProvider;

import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.QuestionHelper;

public class QuestionsLoader extends CursorLoader {

    private ContentSection contentSection;
    private Integer entryPosition;
    private Integer insertedCount;
    private int loadIntention;
    private boolean feedFinished;

    public Integer getInsertedCount() {
        return insertedCount;
    }

    public abstract class BundleKeys {
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String INSERTED_COUNT = "INSERTED_COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
        public static final String SECTION = "SECTION";
        public static final String FEED_FINISHED = "FEED_FINISHED";
    }

    public QuestionsLoader(Context context, ContentSection contentSection, Integer entryPosition, Integer insertedCount, int loadIntention, boolean feedFinished) {
        super(context);
        this.contentSection = contentSection;
        this.entryPosition = entryPosition;
        this.insertedCount = insertedCount;
        this.loadIntention = loadIntention;
        this.feedFinished = feedFinished;
    }

    @Override
    public Cursor loadInBackground() {
        Uri uri = ContentProvider.createUri(QuestionHelper.getClass(contentSection), null);
        return getContext().getContentResolver().query(uri, null, null, null, "_id ASC");
    }

    public ContentSection getContentSection() {
        return contentSection;
    }

    public Integer getEntryPosition() {
        return entryPosition;
    }

    public int getLoadIntention() {
        return loadIntention;
    }

    public boolean isFeedFinished() {
        return feedFinished;
    }
}
