package org.techteam.decider.db.resolvers.questions;

import android.content.ContentValues;

import org.techteam.decider.content.Entry;
import org.techteam.decider.content.entities.QuestionEntry;
import org.techteam.decider.db.resolvers.ContentResolver;
import org.techteam.decider.db.tables.QuestionsAbstractTable;

public abstract class QuestionsResolver extends ContentResolver {

    @Override
    protected ContentValues convertToContentValues(Entry entry) {
        ContentValues values = new ContentValues();

        return values;
    }

    @Override
    protected QueryField getUpdateField(Entry entry) {
        return byId(entry);
    }

    @Override
    protected QueryField getQueryField(Entry entry) {
        return byId(entry);
    }

    @Override
    protected QueryField getDeletionField(Entry entry) {
        return byId(entry);
    }

    @Override
    protected String[] getProjection() {
        return new String[] {};
    }

    private QueryField byId(Entry entry) {
        return new QueryField(QuestionsAbstractTable.ID.name, new String[]{ Integer.toString(((QuestionEntry) entry).getQId()) });
    }
}
