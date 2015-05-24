package org.techteam.decider.content;

import android.database.Cursor;

import com.activeandroid.Model;

import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.content.entities.QuestionMyEntry;
import org.techteam.decider.content.entities.QuestionNewEntry;
import org.techteam.decider.content.entities.QuestionPopularEntry;

public class QuestionHelper {
    public static void deleteAll(ContentSection section) {
        switch (section) {
            case NEW:
                QuestionNewEntry.deleteAll();
                break;
            case POPULAR:
                QuestionPopularEntry.deleteAll();
                break;
            case MY:
                QuestionMyEntry.deleteAll();
                break;
        }
    }

    public static void saveQuestion(ContentSection section, QuestionEntry questionEntry) {
        switch (section) {
            case NEW: {
                QuestionNewEntry e = new QuestionNewEntry(questionEntry);
                e.saveTotal();
                break;
            }
            case POPULAR: {
                QuestionPopularEntry e = new QuestionPopularEntry(questionEntry);
                e.saveTotal();
                break;
            }
            case MY: {
                QuestionMyEntry e = new QuestionMyEntry(questionEntry);
                e.saveTotal();
                break;
            }
        }
    }

    public static QuestionEntry fromCursor(ContentSection section, Cursor cursor) {
        switch (section) {
            case NEW:
                return QuestionNewEntry.fromCursor(cursor);
            case POPULAR:
                return QuestionPopularEntry.fromCursor(cursor);
            case MY:
                return QuestionMyEntry.fromCursor(cursor);
        }
        return null;
    }

    public static Class<? extends Model> getClass(ContentSection contentSection) {
        switch (contentSection) {
            case NEW:
                return QuestionNewEntry.class;
            case POPULAR:
                return QuestionPopularEntry.class;
            case MY:
                return QuestionMyEntry.class;
        }
        return null;
    }
}
