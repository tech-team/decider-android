package org.techteam.decider.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String LOG = "DatabaseHelper";
    private static final int DATABASE_VERSION = 13;
    private static final String DATABASE_NAME = "bashhappens";

    public static final String AUTHORITY = "org.techteam.bashhappens.db.DatabaseHelper";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        for (ITable table: Tables.TABLES) {
            table.onCreate(sqLiteDatabase);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i2) {
        for (ITable table: Tables.TABLES) {
            table.onUpgrade(sqLiteDatabase, i, i2);
        }
    }

}
