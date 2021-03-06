package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.Entry;

@Table(name="PollItems", id = BaseColumns._ID)
public class PollItemEntry extends Model {
    @Column(name="pid", unique=true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public int pid;

    @Column(name="poll_item_text")
    public String text;

    @Column(name="image_url")
    public String imageUrl;

    @Column(name="preview_url")
    public String previewUrl;

    @Column(name="votes_count")
    public int votesCount;

    @Column(name="voted")
    public boolean voted;

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

    public String getPreviewUrl() {
        return previewUrl;
    }

    public int getVotesCount() {
        return votesCount;
    }

    public boolean isVoted() {
        return voted;
    }

    public static PollItemEntry fromJson(JSONObject obj) throws JSONException {
        int pid = obj.getInt("id");
        PollItemEntry entry = byPId(pid);
        if (entry == null) {
            entry = new PollItemEntry();
        }
        entry.pid = pid;
        entry.text = obj.isNull("text") ? null : obj.getString("text");
        entry.imageUrl = obj.getString("image_url");
        entry.previewUrl = obj.getString("preview_url");
        entry.votesCount = obj.has("votes_count") ? obj.getInt("votes_count") : 0;
        entry.voted = obj.has("voted") && obj.getBoolean("voted");
        return entry;
    }

    public static PollItemEntry byPId(int pid) {
        return new Select().from(PollItemEntry.class).where("pid = ?", pid).executeSingle();
    }

    public static void deleteAll() {
        new Delete().from(PollItemEntry.class).execute();
    }
}
