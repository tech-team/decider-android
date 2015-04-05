package org.techteam.decider.db.resolvers.questions;

import android.content.ContentValues;
import android.net.Uri;

import org.techteam.decider.content.Entry;
import org.techteam.decider.db.resolvers.AbstractContentResolver;
import org.techteam.decider.db.resolvers.ContentResolver;

public abstract class QuestionsResolver extends ContentResolver {

    @Override
    protected ContentValues convertToContentValues(Entry entry) {
        return null;
    }

    @Override
    protected QueryField getUpdateField(Entry entry) {
        return null;
    }

    @Override
    protected QueryField getQueryField(Entry entry) {
        return null;
    }

    @Override
    protected QueryField getDeletionField(Entry entry) {
        return null;
    }

    @Override
    protected String[] getProjection() {
        return new String[0];
    }
}
