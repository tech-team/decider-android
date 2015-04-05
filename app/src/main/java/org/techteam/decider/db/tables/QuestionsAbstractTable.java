package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import static org.techteam.decider.db.DataTypes.*;

public abstract class QuestionsAbstractTable extends AbstractTable {
    public static final String ID = "id";
    public static final String TEXT = "text";
    public static final String RATING = "rating";
    public static final String DATE = "date";
    public static final String DEFAULT_SORT_ORDER = "id DESC";

    protected QuestionsAbstractTable(String tableName) {
        super(tableName);
    }

    protected String createTableQuery(String tableName) {
        ArrayList<TableTuple> tuples = new ArrayList<>();
        tuples.add(new TableTuple(COLUMN_ID, TYPE_SERIAL));
        tuples.add(new TableTuple(TEXT, TYPE_TEXT));
        return super.createTableQuery(tableName, tuples);
    }
}
