package org.techteam.decider.db.resolvers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import org.techteam.decider.content.ContentSection;
import org.techteam.decider.content.Entry;

public abstract class AbstractContentResolver {

    protected abstract Uri _getUri();
    protected abstract ContentValues convertToContentValues(Entry entry);
    protected abstract QueryField getUpdateField(Entry entry);
    protected abstract QueryField getQueryField(Entry entry);
    protected abstract QueryField getDeletionField(Entry entry);
    protected abstract String[] getProjection();

//    protected List<ContentValues> convertToContentValues(ContentList<?> list) {
//        List<ContentValues> contentValues = new ArrayList<>();
//        for (Entry entry: list) {
//            contentValues.add(convertToContentValues(entry));
//        }
//        return contentValues;
//    }
//
    public static ContentResolver getResolver(ContentSection section) {
        switch (section) {
            case NEW:
//                return new
            case POPULAR:
                break;
            case MY:
                break;
            default:
                return null;
        }
        return null;
    }

    public static AbstractContentResolver getResolver(ExtraResolver resolver) {
        switch (resolver) {
            case TRANSACTIONS:
                return new TransactionsResolver();
            default:
                return null;
        }
    }

    public Cursor getCursor(Context context) {
        return getCursor(context, getProjection(), null, null, null);
    }
    public Cursor getCursor(Context context, String[] projection,
                                     String selection, String[] selectionArgs,
                                     String sortOrder) {
        /*if (selection != null) {
            selection += " = ?";
        }*/
        if (projection == null) {
            projection = getProjection();
        }
        return context.getContentResolver().query(_getUri(),
                projection,
                selection,
                selectionArgs,
                sortOrder);
    }

    // Because it is needed in static context

//    public List<Integer> insertEntries(Context context, ContentList<?> list) {
//        List<Integer> insertedIds = new ArrayList<>();
//        for(ContentValues values : convertToContentValues(list)) {
//            insertedIds.add(Integer.valueOf(
//                            context
//                            .getContentResolver()
//                            .insert(_getUri(), values)
//                            .getLastPathSegment()));
//        }
//        return insertedIds;
//    }

    public int deleteAllEntries(Context context) {
        return context.getContentResolver().delete(_getUri(), null, null);
    }

    public int insert(Context context, Entry entry) {
        QueryField field = getQueryField(entry);
        Cursor cur = getCursor(context, null, field.where, field.whereArgs, null);
        int rows = cur.getCount();
        cur.close();
        if (rows != 0) {
            return updateEntry(context, entry);
        }
        else {
            return insertEntry(context, entry);
        }
    }

    public int insertEntry(Context context, Entry entry) {
        return Integer.valueOf(context
                               .getContentResolver()
                               .insert(_getUri(), convertToContentValues(entry))
                               .getLastPathSegment());
    }

    public int deleteEntry(Context context, Entry entry) {
        QueryField field = getDeletionField(entry);
        return (field == null)
                ? (-1)
                : context.getContentResolver()
                      .delete(_getUri(), field.where , field.whereArgs);
    }

    public int updateEntry(Context context, Entry entry) {
        QueryField field = getUpdateField(entry);
        return (field == null)
                ? (-1)
                : context.getContentResolver()
                    .update(_getUri(), this.convertToContentValues(entry), field.where, field.whereArgs);
    }

//    public static Map<String, Integer> truncateAll(Context context) {
//        Map<String, Integer> deletions = new HashMap<String, Integer>();
//        deletions.put(BashNewest.TABLE_NAME, new BashNewestResolver().deleteAllEntries(context));
//        deletions.put(BashFavs.TABLE_NAME, new BashFavsResolver().deleteAllEntries(context));
//        deletions.put(BashBest.TABLE_NAME, new BashBestResolver().deleteAllEntries(context));
//        deletions.put(BashLikes.TABLE_NAME, new BashLikesResolver().deleteAllEntries(context));
//        deletions.put(Transactions.TABLE_NAME, new TransactionsResolver().deleteAllEntries(context));
//        return deletions;
//    }

    protected class QueryField {
        public String where;
        public String[] whereArgs;

        public QueryField(String where, String[] whereArgs) {
            this.where = where + " = ?";
            this.whereArgs = whereArgs;
        }
    }

    //////////////////// Usage example //////////////////////
    /*BashOrgEntry entry = new BashOrgEntry().setId("1").setText("see?");
    BashOrgList list = new BashOrgList();
    list.add(entry);
    AbstractContentResolver resolver = AbstractContentResolver.getResolver(ContentSection.BASH_ORG_NEWEST);
    resolver.insertEntries(this, list);
    Cursor cur = resolver.getCursor(this);

    BashOrgList bashOrgEntryList = new BashOrgList();
    cur.moveToFirst();
    while (!cur.isAfterLast()) {
        bashOrgEntryList.add((BashOrgEntry)resolver.getCurrentEntry(cur));
        cur.moveToNext();
    }
    cur.close();


    BashTransactionsResolver resolver = (BashTransactionsResolver) AbstractContentResolver.getResolver(ContentSection.BASH_ORG_TRANSACTIONS);
    resolver.insert(this, new BashTransactionsEntry().setId("unsee").setStatus(TransactionStatus.ERROR).setOperationType(OperationType.IT_VOTE));
    resolver.insert(this, new BashTransactionsEntry().setId("see").setStatus(TransactionStatus.ERROR));
    List<String> lst1 = resolver.getEntriesByStatusAndType(this, TransactionStatus.ERROR, OperationType.IT_VOTE);
    List<String> lst2 = resolver.getEntriesByStatus(this, TransactionStatus.ERROR);
    AbstractContentResolver.truncateAll(this);
    */
}
