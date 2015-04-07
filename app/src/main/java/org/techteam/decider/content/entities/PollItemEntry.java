package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.Entry;

@Table(name="PollItems", id = BaseColumns._ID)
public class PollItemEntry extends Model {
    @Column(name="pid", unique=true, onUniqueConflict=Column.ConflictAction.REPLACE)
    public int pid;

    @Column(name="poll_item_text")
    public String text;

    @Column(name="image_url")
    public String imageUrl;

    @Column(name="votes_count")
    public int votesCount;

    public PollItemEntry() {
        super();
    }

    public int getPid() {
        return pid;
    }

    public String getText() {
        return text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public static PollItemEntry fromJson(JSONObject obj) throws JSONException {
        PollItemEntry entry = new PollItemEntry();
        entry.pid = obj.getInt("id");
        entry.text = obj.getString("text");
        entry.imageUrl = obj.getString("image_url");
        entry.votesCount = obj.getInt("votes_count");
        return entry;
    }
}
