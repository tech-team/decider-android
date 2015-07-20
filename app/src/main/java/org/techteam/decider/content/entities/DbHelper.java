package org.techteam.decider.content.entities;

public class DbHelper {
    public static void cleanDb() {
        CategoryEntry.deleteAll();
        CommentEntry.deleteAll();
        PollItemEntry.deleteAll();
        QuestionEntry.deleteAll();
        QuestionNewEntry.deleteAll();
        QuestionPopularEntry.deleteAll();
        QuestionMyEntry.deleteAll();
        UserEntry.deleteAll();
        TransactionEntry.deleteAll();
    }
}
