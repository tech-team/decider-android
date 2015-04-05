package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import org.techteam.decider.db.DatabaseHelper;

public class QuestionsNewTable extends QuestionsAbstractTable {
    public static final String TABLE_NAME = "questions_new";

    public static final Uri CONTENT_ID_URI_BASE = generateContentIdUriBase(TABLE_NAME);
    public static final String CONTENT_TYPE = generateContentType(TABLE_NAME);

    public QuestionsNewTable() {
        super(TABLE_NAME);
    }
}
