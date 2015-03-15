package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.techteam.decider.db.DatabaseHelper;
import org.techteam.decider.db.ITable;

import static org.techteam.decider.db.DataTypes.*;

public class Transactions implements ITable {

    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String TYPE = "type";

    public static final String TABLE_NAME = "transactions";

    public static final Uri CONTENT_ID_URI_BASE
            = Uri.parse("content://" + DatabaseHelper.AUTHORITY + "/transactions/");
    public static final String CONTENT_TYPE
            = "org.techteam.bashhappens.db.tables.transactions/org.techteam.bashhappens.db";

    public Transactions() {}

    private static final String TABLE_CREATE = "CREATE TABLE "
            + TABLE_NAME + "(" + COLUMN_ID + TYPE_SERIAL + SEPARATOR
            + ID + TYPE_TEXT + SEPARATOR
            + STATUS + TYPE_INTEGER + SEPARATOR
            + TYPE + TYPE_INTEGER + ");";

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion) {
        Log.w(Transactions.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(database);
    }
}
