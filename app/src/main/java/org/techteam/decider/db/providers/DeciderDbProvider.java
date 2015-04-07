package org.techteam.decider.db.providers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.techteam.decider.db.tables.AbstractTable;
import org.techteam.decider.db.tables.QuestionsAbstractTable;
import org.techteam.decider.db.tables.QuestionsNewTable;

public class DeciderDbProvider extends DbProvider {

    private static final int QUESTIONS_NEW = 1;
    private static final int QUESTIONS_POPULAR = 2;
    private static final int QUESTIONS_MY = 3;

    public static final String AUTHORITY = DbProvider.AUTHORITY + "DeciderDbProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public DeciderDbProvider() {
        super();

        mUriMatcher.addURI(AUTHORITY, QuestionsNewTable.TABLE_NAME, QUESTIONS_NEW);

        for (String item : new String[] {AbstractTable._ID, QuestionsAbstractTable.TEXT.name}) {
            mProjectionMap.put(item, item);
        }
    }

    @Override
    protected void queryUriMatch(Uri uri, SQLiteQueryBuilder qb) {
        StringBuilder query = new StringBuilder();
        switch(mUriMatcher.match(uri)) {

            case QUESTIONS_NEW:
                query.append(QuestionsNewTable.TABLE_NAME);
                break;

//            case BASH_LIKES:
//                qb.setTables(BashLikes.TABLE_NAME);
//                return;
//            case BASH_BAYAN:
//                qb.setTables(BashBayan.TABLE_NAME);
//                return;

            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
//        query.append(" LEFT JOIN "
//                + BashLikes.TABLE_NAME + " ON "
//                + AbstractTable.ID
//                + " = " + BashLikes.TABLE_NAME + "." + BashLikes.ARTICLE_ID
//                + " LEFT JOIN " + BashBayan.TABLE_NAME + " ON "
//                + AbstractTable.ID
//                + " = " + BashBayan.TABLE_NAME + "." + BashBayan.ARTICLE_ID);
        qb.setTables(query.toString());
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case QUESTIONS_NEW:
                return QuestionsNewTable.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    protected Uri performInsert(Uri uri, SQLiteDatabase db, ContentValues contentValues) {
        switch (mUriMatcher.match(uri)) {
            case QUESTIONS_NEW:
                return _insert(db, QuestionsNewTable.TABLE_NAME, QuestionsNewTable.CONTENT_ID_URI_BASE, contentValues);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    protected synchronized int performDelete(Uri uri, SQLiteDatabase db,
                                             String where, String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case QUESTIONS_NEW:
                return db.delete(QuestionsNewTable.TABLE_NAME, where, whereArgs);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public synchronized int performUpdate(Uri uri, SQLiteDatabase db, ContentValues values,
                                          String where, String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case QUESTIONS_NEW:
                return db.update(QuestionsNewTable.TABLE_NAME, values, where, whereArgs);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
