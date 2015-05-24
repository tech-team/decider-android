package org.techteam.decider.content.entities;


import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

@Table(name = "QuestionsMy", id = BaseColumns._ID)
public class QuestionMyEntry extends Model {

    @Column(name="question", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public QuestionEntry question;

    public QuestionMyEntry() {
        super();
    }

    public QuestionMyEntry(QuestionEntry question) {
        super();
        this.question = question;
    }

    public Long saveTotal() {
        question.saveTotal();
        return save();
    }

    public static QuestionEntry fromCursor(Cursor cursor) {
        QuestionMyEntry entry = new QuestionMyEntry();
        entry.loadFromCursor(cursor);
        return entry.question;
    }

    public static void deleteAll() {
        new Delete().from(QuestionMyEntry.class).execute();
    }
}
