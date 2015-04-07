package org.techteam.decider.content.entities;


import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.Entry;

@Table(name = "QuestionsNew", id = BaseColumns._ID)
public class QuestionEntry extends Model {
    @Column(name="qid", unique=true, onUniqueConflict=Column.ConflictAction.REPLACE)
    public int qid;

    @Column(name="q_text")
    public String text;

    @Column(name="creation_date")
    public String creationDate; // TODO: change to DateTime

    @Column(name="category_id")
    public int categoryId;

    @Column(name="author", onUpdate=Column.ForeignKeyAction.CASCADE, onDelete=Column.ForeignKeyAction.CASCADE)
    public UserEntry author;

    @Column(name="likes_count")
    public int likesCount;

    @Column(name="poll_item_1", onUpdate=Column.ForeignKeyAction.CASCADE, onDelete=Column.ForeignKeyAction.CASCADE)
    public PollItemEntry pollItem1;

    @Column(name="poll_item_2", onUpdate=Column.ForeignKeyAction.CASCADE, onDelete=Column.ForeignKeyAction.CASCADE)
    public PollItemEntry pollItem2;

    @Column(name="comments_count")
    public int commentsCount;

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

    public Long saveTotal() {
        author.save();
        pollItem1.save();
        pollItem2.save();
        return save();
    }

    public static QuestionEntry fromJson(JSONObject obj) throws JSONException {
        QuestionEntry entry = new QuestionEntry();
        entry.qid = obj.getInt("id");
        entry.text = obj.getString("text");
        entry.creationDate = obj.getString("creation_date");
        entry.categoryId = obj.getInt("category_id");
        entry.author = UserEntry.fromJson(obj.getJSONObject("author"));
        entry.likesCount = obj.getInt("likes_count");

        JSONArray pollItems = obj.getJSONArray("poll");

        if (pollItems.length() == 2) {
            entry.pollItem1 = PollItemEntry.fromJson(pollItems.getJSONObject(0));
            entry.pollItem2 = PollItemEntry.fromJson(pollItems.getJSONObject(1));

        } else {
            throw new JSONException("poll must contain exactly 2 items");
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
}
