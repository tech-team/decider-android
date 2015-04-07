package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import org.techteam.decider.db.DatabaseHelper;
import org.techteam.decider.db.ITable;

import java.util.ArrayList;

import static org.techteam.decider.db.DataTypes.COLUMN_ID;
import static org.techteam.decider.db.DataTypes.SEPARATOR;
import static org.techteam.decider.db.DataTypes.TYPE_SERIAL;
import static org.techteam.decider.db.DataTypes.TYPE_TEXT;

public abstract class AbstractTable implements BaseColumns, ITable {

    public static class TableTuple {
        public String name;
        public String type;

        TableTuple(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    private String tableName;

    public static final String CONTENT_TYPE_PREFIX = "org.techteam.decider.db.tables.";
    public static final String CONTENT_TYPE_POSTFIX = "/org.techteam.decider.db";

    protected AbstractTable(String tableName) {
        this.tableName = tableName;
    }

    public static String generateContentType(String tableName) {
        return CONTENT_TYPE_PREFIX + tableName + CONTENT_TYPE_POSTFIX;
    }

    public static Uri generateContentIdUriBase(String tableName) {
        return Uri.parse("content://" + DatabaseHelper.AUTHORITY + "/" + tableName + "/");
    }

    protected abstract String createTableQuery(String tableName);

    protected String createTableQuery(String tableName, ArrayList<TableTuple> definition) {
        String s = "CREATE TABLE " + tableName + "(";
        for (int i = 0; i < definition.size(); ++i) {
            TableTuple def = definition.get(i);
            s += def.name + " " + def.type;
            if (i < definition.size() - 1) {
                s += SEPARATOR;
            }
        }
        s += ")";
        return s;
    }

    public void onUpgrade(SQLiteDatabase database, int oldVersion,
                          int newVersion, String tableName) {
        Log.w(this.getClass().getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + tableName);
        onCreate(database);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(createTableQuery(getTableName()));
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        onUpgrade(database, oldVersion, newVersion, getTableName());
    }

    public String getTableName() {
        return tableName;
    }
}
