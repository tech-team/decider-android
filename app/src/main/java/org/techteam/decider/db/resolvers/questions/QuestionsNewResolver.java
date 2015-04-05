package org.techteam.decider.db.resolvers.questions;

import android.net.Uri;

import org.techteam.decider.db.providers.DeciderDbProvider;
import org.techteam.decider.db.tables.QuestionsNewTable;

public class QuestionsNewResolver extends QuestionsResolver {
    @Override
    protected Uri _getUri() {
        return Uri.parse(DeciderDbProvider.CONTENT_URI + "/" + QuestionsNewTable.TABLE_NAME);
    }
}
