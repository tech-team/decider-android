package org.techteam.decider.content.entities;

import android.database.Cursor;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "Categories", id = BaseColumns._ID)
public class ContentCategory extends Model {

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int uid;

    @Column(name = "localized_label")
    private String localizedLabel;

    @Column(name = "selected")
    private boolean selected;

    public ContentCategory() {
        super();
    }

    public ContentCategory(int uid, String localizedLabel, boolean selected) {
        super();
        this.uid = uid;
        this.localizedLabel = localizedLabel;
        this.selected = selected;
    }

    public ContentCategory(int uid, String localizedLabel) {
        this(uid, localizedLabel, false);
    }

    public int getUid() {
        return uid;
    }

    public String getLocalizedLabel() {
        return localizedLabel;
    }

    public boolean isSelected() {
        return selected;
    }

    public static ContentCategory fromJson(JSONObject obj) throws JSONException {
        int uid = obj.getInt("id");
        String name = obj.getString("name");
        return new ContentCategory(uid, name, false);
    }

    public static ContentCategory fromCursor(Cursor cursor) {
        ContentCategory entry = new ContentCategory();
        entry.loadFromCursor(cursor);
        return entry;
    }
}
