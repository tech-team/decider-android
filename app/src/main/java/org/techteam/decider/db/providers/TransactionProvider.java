package org.techteam.decider.db.providers;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.techteam.decider.db.tables.Transactions;

public class TransactionProvider extends DbProvider {

    private static final int TRANSACTIONS = 101;

    public static final String AUTHORITY = DbProvider.AUTHORITY + "TransactionProvider";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);

    public TransactionProvider() {
        super();

        mUriMatcher.addURI(AUTHORITY, Transactions.TABLE_NAME, TRANSACTIONS);

        for (String item : new String[] {Transactions.ID, Transactions.STATUS, Transactions.TYPE}) {
            mProjectionMap.put(item, item);
        }
    }

    @Override
    protected void queryUriMatch(Uri uri, SQLiteQueryBuilder qb) {
        switch(mUriMatcher.match(uri)) {
            case TRANSACTIONS:
                qb.setTables(Transactions.TABLE_NAME);
                return;
            default:
                throw new IllegalArgumentException("Unknown URI" + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (mUriMatcher.match(uri)) {
            case TRANSACTIONS:
                return Transactions.CONTENT_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    protected Uri performInsert(Uri uri, SQLiteDatabase db, ContentValues contentValues) {
        switch (mUriMatcher.match(uri)) {
            case TRANSACTIONS:
                return _insert(db, Transactions.TABLE_NAME, Transactions.CONTENT_ID_URI_BASE, contentValues);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    protected int performDelete(Uri uri, SQLiteDatabase db, String where, String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case TRANSACTIONS:
                return db.delete(Transactions.TABLE_NAME, where, whereArgs);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    protected int performUpdate(Uri uri, SQLiteDatabase db, ContentValues values, String where, String[] whereArgs) {
        switch (mUriMatcher.match(uri)) {
            case TRANSACTIONS:
                return db.update(Transactions.TABLE_NAME, values, where, whereArgs);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }
}
