package org.techteam.decider.gui.loaders;

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;

import com.activeandroid.content.ContentProvider;

import org.techteam.decider.content.entities.CommentEntry;

public class CommentsLoader extends CursorLoader {

    private Integer questionId;
    private Integer entryPosition;
    private Integer insertedCount;
    private int loadIntention;
    private boolean prepend;

    public Integer getInsertedCount() {
        return insertedCount;
    }

    public abstract class BundleKeys {
        public static final String QUESTION_ID = "QUESTION_ID";
        public static final String ENTRY_POSITION = "ENTRY_POSITION";
        public static final String INSERTED_COUNT = "INSERTED_COUNT";
        public static final String LOAD_INTENTION = "LOAD_INTENTION";
        public static final String PREPEND = "PREPEND";
    }

    public CommentsLoader(Context context, Integer questionId, Integer entryPosition, Integer insertedCount, int loadIntention, boolean prepend) {
        super(context);
        this.questionId = questionId;
        this.entryPosition = entryPosition;
        this.insertedCount = insertedCount;
        this.loadIntention = loadIntention;
        this.prepend = prepend;
    }

    @Override
    public Cursor loadInBackground() {
        Uri uri = ContentProvider.createUri(CommentEntry.class, null);
        return getContext().getContentResolver().query(uri, null, "question_id = ?", new String[] { Integer.toString(questionId) }, "cid ASC");
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public Integer getEntryPosition() {
        return entryPosition;
    }

    public int getLoadIntention() {
        return loadIntention;
    }

    public boolean isPrepend() {
        return prepend;
    }
}
