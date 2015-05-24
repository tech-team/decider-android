package org.techteam.decider.content.entities;


import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;

@Table(name = "QuestionsPopular", id = BaseColumns._ID)
public class QuestionPopularEntry extends Model {

    @Column(name="question", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public QuestionEntry question;

    public QuestionPopularEntry() {
        super();
    }

    public QuestionPopularEntry(QuestionEntry question) {
        super();
        this.question = question;
    }

    public Long saveTotal() {
        question.saveTotal();
        return save();
    }

    public static QuestionEntry fromCursor(Cursor cursor) {
        QuestionPopularEntry entry = new QuestionPopularEntry();
        entry.loadFromCursor(cursor);
        return entry.question;
    }

    public static void deleteAll() {
        new Delete().from(QuestionPopularEntry.class).execute();
    }
}
