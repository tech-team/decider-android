package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import org.techteam.decider.db.DatabaseHelper;
import org.techteam.decider.db.ITable;

import java.util.ArrayList;

import static org.techteam.decider.db.DataTypes.*;

public class Transactions extends AbstractTable {
    public static final String TABLE_NAME = "transactions";

    public static final String ID = "id";
    public static final String STATUS = "status";
    public static final String TYPE = "type";

    public static final Uri CONTENT_ID_URI_BASE = generateContentIdUriBase(TABLE_NAME);
    public static final String CONTENT_TYPE = generateContentType(TABLE_NAME);

    public Transactions() {
        super(TABLE_NAME);
    }

    @Override
    protected String createTableQuery(String tableName) {
        ArrayList<TableTuple> tuples = new ArrayList<>();
        tuples.add(new TableTuple(COLUMN_ID, TYPE_SERIAL));
        tuples.add(new TableTuple(ID, TYPE_TEXT));
        tuples.add(new TableTuple(STATUS, TYPE_INTEGER));
        tuples.add(new TableTuple(TYPE, TYPE_INTEGER));
        return super.createTableQuery(tableName, tuples);
    }
}
