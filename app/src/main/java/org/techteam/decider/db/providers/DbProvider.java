package org.techteam.decider.db.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import org.techteam.decider.db.DatabaseHelper;

import java.util.HashMap;

public abstract class DbProvider extends ContentProvider {

    protected DatabaseHelper databaseHelper;
    protected SQLiteDatabase database;
    protected UriMatcher mUriMatcher;
    protected HashMap<String, String> mProjectionMap;

    protected static final String AUTHORITY = "org.techteam.decider.db.providers.";

    protected DbProvider() {
        mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        mProjectionMap = new HashMap<>();
    }

    @Override
    public boolean onCreate() {
        databaseHelper = new DatabaseHelper(this.getContext());
        return false;
    }

    protected abstract void queryUriMatch(Uri uri, SQLiteQueryBuilder qb);

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setProjectionMap(mProjectionMap);

        queryUriMatch(uri, qb);

        database = databaseHelper.getReadableDatabase();

        Cursor cur = qb.query(database, projection, selection, selectionArgs,
                null, null, sortOrder);

        cur.setNotificationUri(getContext().getContentResolver(), uri);
        return cur;
    }

    @Override
    public abstract String getType(Uri uri);

    protected abstract Uri performInsert(Uri uri, SQLiteDatabase db, ContentValues contentValues);

    protected synchronized Uri _insert(SQLiteDatabase db, String tableName, Uri uriBase, ContentValues values) {
        long rowId = db.insertOrThrow(tableName, null, values);

        if (rowId > 0) {
            Uri noteUri = ContentUris.withAppendedId(uriBase, rowId);
            getContext().getContentResolver().notifyChange(noteUri, null);
            return noteUri;
        }

        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues rawValues) {
        ContentValues contentValues;

        if (rawValues != null) {
            contentValues = new ContentValues(rawValues);
        } else {
            contentValues = new ContentValues();
        }

        database = databaseHelper.getWritableDatabase();

        return performInsert(uri, database, contentValues);
    }

    protected abstract int performDelete(Uri uri, SQLiteDatabase db,
                                         String where, String[] whereArgs);

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        database = databaseHelper.getWritableDatabase();
        int count;

        count = performDelete(uri, database, where, whereArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    protected abstract int performUpdate(Uri uri, SQLiteDatabase db, ContentValues values,
                                         String where, String[] whereArgs);

    @Override
    public int update(Uri uri, ContentValues values, String where, String[] whereArgs) {
        database = databaseHelper.getWritableDatabase();
        int count;

        count = performUpdate(uri, database, values, where, whereArgs);

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}
