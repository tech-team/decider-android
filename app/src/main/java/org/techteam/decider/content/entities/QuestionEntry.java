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
import org.techteam.decider.content.Entry;

@Table(name = "Questions", id = BaseColumns._ID)
public class QuestionEntry extends Model {
    @Column(name="qid", unique=true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int qid;

    @Column(name="q_text")
    public String text;

    @Column(name="creation_date")
    public String creationDate; // TODO: change to DateTime

    @Column(name="category_id")
    public int categoryId;

    @Column(name="author", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public UserEntry author;

    @Column(name="likes_count")
    public int likesCount;

    @Column(name="poll_item_1", onUpdate=Column.ForeignKeyAction.CASCADE, onDelete=Column.ForeignKeyAction.CASCADE)
    public PollItemEntry pollItem1;

    @Column(name="poll_item_2", onUpdate=Column.ForeignKeyAction.CASCADE, onDelete=Column.ForeignKeyAction.CASCADE)
    public PollItemEntry pollItem2;

    @Column(name="comments_count")
    public int commentsCount;

    @Column(name="anonymous")
    public boolean anonymous;

    @Column(name="voted")
    public boolean voted;

    public QuestionEntry() {
        super();
    }

    public int getQId() {
        return qid;
    }

    public String getText() {
        return text;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public UserEntry getAuthor() {
        return author;
    }

    public int getLikesCount() {
        return likesCount;
    }

    public PollItemEntry getPollItem1() {
        return pollItem1;
    }

    public PollItemEntry getPollItem2() {
        return pollItem2;
    }

    public int getCommentsCount() {
        return commentsCount;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public boolean isVoted() {
        return voted;
    }

    public Long saveTotal() {
        author.save();
        if (pollItem1 != null)
            pollItem1.save();
        if (pollItem2 != null)
            pollItem2.save();
        return save();
    }

    public static QuestionEntry byQId(int qid) {
        return new Select().from(QuestionEntry.class).where("qid = ?", qid).executeSingle();
    }

    public static QuestionEntry fromJson(JSONObject obj) throws JSONException {
        int qid = obj.getInt("id");
        QuestionEntry entry = byQId(qid);
        if (entry == null) {
            entry = new QuestionEntry();
        }
        entry.qid = qid;
        entry.text = obj.getString("text");
        entry.creationDate = obj.getString("creation_date");
        entry.categoryId = obj.getInt("category_id");
        entry.author = UserEntry.fromJson(obj.getJSONObject("author"));
        entry.likesCount = obj.getInt("likes_count");
        entry.anonymous = obj.getBoolean("is_anonymous");
        entry.voted = obj.getBoolean("voted");

        if (!obj.isNull("poll")) {
            JSONArray pollItems = obj.getJSONArray("poll");

            if (pollItems != null && pollItems.length() == 2) {
                if (pollItems.length() == 2) {
                    entry.pollItem1 = PollItemEntry.fromJson(pollItems.getJSONObject(0));
                    entry.pollItem2 = PollItemEntry.fromJson(pollItems.getJSONObject(1));

                } else {
                    throw new JSONException("poll must contain exactly 2 items");
                }
            }
        }

        if (obj.has("comments_count")) {
            entry.commentsCount = obj.getInt("comments_count");
        }
        return entry;
    }

    public static QuestionEntry fromCursor(Cursor cursor) {
        QuestionEntry entry = new QuestionEntry();
        entry.loadFromCursor(cursor);
        return entry;
    }

    public static void deleteAll() {
        new Delete().from(QuestionEntry.class).execute();
    }
}
