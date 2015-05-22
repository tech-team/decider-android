package org.techteam.decider.content.entities;


import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "QuestionsNew", id = BaseColumns._ID)
public class QuestionNewEntry extends Model {

    @Column(name="question", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public QuestionEntry question;

    public QuestionNewEntry() {
        super();
    }

    public Long saveTotal() {
        question.save();
        return save();
    }

    public static QuestionEntry fromCursor(Cursor cursor) {
        QuestionNewEntry entry = new QuestionNewEntry();
        entry.loadFromCursor(cursor);
        return entry.question;
    }

    public static void deleteAll() {
        new Delete().from(QuestionNewEntry.class).execute();
    }
}
