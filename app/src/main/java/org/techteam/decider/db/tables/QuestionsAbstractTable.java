package org.techteam.decider.db.tables;

import android.database.sqlite.SQLiteDatabase;

import java.lang.reflect.Field;
import java.util.ArrayList;

import static java.lang.reflect.Modifier.isFinal;
import static java.lang.reflect.Modifier.isStatic;
import static org.techteam.decider.db.DataTypes.*;

public abstract class QuestionsAbstractTable extends AbstractTable {
    public static final TableTuple ID               = new TableTuple("id", TYPE_INTEGER);
    public static final TableTuple TEXT             = new TableTuple("text", TYPE_TEXT);
    public static final TableTuple CREATION_DATE    = new TableTuple("creation_date", TYPE_TEXT);
    public static final TableTuple CATEGORY_ID      = new TableTuple("category_id", TYPE_INTEGER);
    public static final TableTuple AUTHOR_ID        = new TableTuple("author_id", TYPE_INTEGER);
    public static final TableTuple AUTHOR_USERNAME  = new TableTuple("author_username", TYPE_TEXT);
    public static final TableTuple AUTHOR_NAME      = new TableTuple("author_name", TYPE_TEXT);
    public static final TableTuple AUTHOR_AVATAR    = new TableTuple("author_name", TYPE_TEXT);


    protected QuestionsAbstractTable(String tableName) {
        super(tableName);
    }

    protected String createTableQuery(String tableName) {
        ArrayList<TableTuple> tuples = new ArrayList<>();
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (isFinal(field.getModifiers()) && isStatic(field.getModifiers()) && field.getType().isAssignableFrom(TableTuple.class)) {
                try {
                    TableTuple t = (TableTuple) field.get(null);
                    tuples.add(t);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.createTableQuery(tableName, tuples);
    }
}
