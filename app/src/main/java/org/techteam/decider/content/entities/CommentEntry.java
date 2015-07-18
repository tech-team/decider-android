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

import java.util.List;

@Table(name = "Comment", id = BaseColumns._ID)
public class CommentEntry extends Model {

    @Column(name="cid", unique=true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int cid;

    @Column(name="question_id", index = true)
    public int questionId;

    @Column(name="author", onUpdate = Column.ForeignKeyAction.CASCADE, onDelete = Column.ForeignKeyAction.CASCADE)
    public UserEntry author;

    @Column(name="text")
    public String text;

    @Column(name="creation_date")
    public String creationDate; // TODO: change to DateTime

    @Column(name="anonymous")
    public boolean anonymous;

    public CommentEntry() {
        super();
    }

    public int getCid() {
        return cid;
    }

    public UserEntry getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public String getCreationDate() {
        return creationDate;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public Long saveTotal() {
        author.save();
        return save();
    }

    public static CommentEntry byCId(int cid) {
        return new Select().from(CommentEntry.class).where("cid = ?", cid).executeSingle();
    }

    public static List<CommentEntry> byQuestionId(int questionId) {
        return new Select().from(CommentEntry.class).where("question_id = ?", questionId).execute();
    }

    public static CommentEntry fromJson(JSONObject obj) throws JSONException {
        int cid = obj.getInt("id");
        CommentEntry entry = byCId(cid);
        if (entry == null) {
            entry = new CommentEntry();
        }
        entry.cid = cid;
        entry.questionId = obj.getInt("question_id");
        entry.text = obj.getString("text");
        entry.creationDate = obj.getString("creation_date");
        entry.author = UserEntry.fromJson(obj.getJSONObject("author"));
        if (obj.has("is_anonymous")) {
            entry.anonymous = obj.getBoolean("is_anonymous");
        }

        return entry;
    }

    public static CommentEntry fromCursor(Cursor cursor) {
        CommentEntry entry = new CommentEntry();
        entry.loadFromCursor(cursor);
        return entry;
    }

    public static void deleteAll() {
        new Delete().from(CommentEntry.class).execute();
    }
}
