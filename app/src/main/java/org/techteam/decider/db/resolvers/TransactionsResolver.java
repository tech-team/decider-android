package org.techteam.decider.db.resolvers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.techteam.decider.content.Entry;
import org.techteam.decider.db.TransactionEntry;
import org.techteam.decider.content.entities.TransactionStatus;
import org.techteam.decider.db.providers.TransactionProvider;
import org.techteam.decider.db.tables.Transactions;
import org.techteam.decider.rest.OperationType;

import java.util.ArrayList;
import java.util.List;

public class TransactionsResolver extends AbstractContentResolver {
    @Override
    protected Uri _getUri() {
        return Uri.parse(TransactionProvider.CONTENT_URI + "/" + Transactions.TABLE_NAME);
    }

    @Override
    protected ContentValues convertToContentValues(Entry entry) {
        ContentValues values = new ContentValues();
        TransactionEntry trEntry = (TransactionEntry) entry;

        values.put(Transactions.ID, trEntry.getId());
        values.put(Transactions.STATUS, trEntry.getStatus().toInt());
        values.put(Transactions.TYPE, trEntry.getOperationType().toInt());
        return values;
    }

    @Override
    protected QueryField getQueryField(Entry entry) {
        return new QueryField(Transactions.ID, new String[]{((TransactionEntry) entry).getId()});
    }

    @Override
    protected QueryField getUpdateField(Entry entry) {
        return new QueryField(Transactions.ID, new String[]{((TransactionEntry) entry).getId()});
    }

    @Override
    protected QueryField getDeletionField(Entry entry) {
        return new QueryField(Transactions.ID, new String[]{((TransactionEntry) entry).getId()});
    }

    @Override
    protected String[] getProjection() {
        return new String[] {Transactions.ID, Transactions.STATUS, Transactions.TYPE};
    }


//    @Override
    public static TransactionEntry getCurrentEntry(Cursor cur) {
        return new TransactionEntry()
                .setId(cur.getString(cur.getColumnIndex(Transactions.ID)))
                .setStatus(cur.getInt(cur.getColumnIndex(Transactions.STATUS)))
                .setOperationType(cur.getInt(cur.getColumnIndex(Transactions.TYPE)));
    }

    public List<String> getEntriesByStatus(Context context, TransactionStatus status) {
        Cursor cur = getCursor(context, new String[]{Transactions.ID}, Transactions.STATUS + "= ?",
                               new String[]{ Integer.toString(status.toInt()) }, null);
        List<String> lst = new ArrayList<>();
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            lst.add(cur.getString(cur.getColumnIndex(Transactions.ID)));
            cur.moveToNext();
        }
        cur.close();
        return lst;
    }

    public List<String> getEntriesByStatusAndType(Context context, TransactionStatus status, OperationType type) {
        String where = Transactions.STATUS + "= ? and " + Transactions.TYPE + "= ?";
        String[] whereArgs = new String[] { Integer.toString(status.toInt()), Integer.toString(type.toInt()) };

        Cursor cur = getCursor(context, new String[]{Transactions.ID}, where, whereArgs, null);
        List<String> lst = new ArrayList<>();
        cur.moveToFirst();
        while (!cur.isAfterLast()) {
            lst.add(cur.getString(cur.getColumnIndex(Transactions.ID)));
            cur.moveToNext();
        }
        cur.close();
        return lst;
    }
}
