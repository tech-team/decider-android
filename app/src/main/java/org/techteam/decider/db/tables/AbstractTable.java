package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import org.techteam.decider.db.ITable;

import static org.techteam.decider.db.DataTypes.COLUMN_ID;
import static org.techteam.decider.db.DataTypes.SEPARATOR;
import static org.techteam.decider.db.DataTypes.TYPE_SERIAL;
import static org.techteam.decider.db.DataTypes.TYPE_TEXT;

public abstract class AbstractTable implements BaseColumns, ITable {
    public static final String ID = "id";
    public static final String TEXT = "text";
    public static final String RATING = "rating";
    public static final String DATE = "date";
    public static final String DEFAULT_SORT_ORDER = "id DESC";

    protected static String createTable(String tableName) {
        return "CREATE TABLE "
                + tableName + "(" + COLUMN_ID + TYPE_SERIAL + SEPARATOR
                + ID + TYPE_TEXT + SEPARATOR
                + TEXT + TYPE_TEXT + SEPARATOR
                + RATING + TYPE_TEXT + SEPARATOR
                + DATE + TYPE_TEXT + ");";
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion, String tableName) {
        Log.w(this.getClass().getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(database);
    }
}
