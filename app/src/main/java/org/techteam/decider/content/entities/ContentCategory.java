package org.techteam.decider.content.entities;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.util.Toaster;

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

    public void setSelectedAsync(boolean selected) {
        CategorySelectionSaver saver = new CategorySelectionSaver();
        saver.execute(selected);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.save();
    }

    public static ContentCategory byUid(int uid) {
        return new Select().from(ContentCategory.class).where("uid = ?", uid).executeSingle();
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

    private class CategorySelectionSaver extends AsyncTask<Boolean, Void, Void> {
        protected Void doInBackground(Boolean... selectedArr) {
            for (Boolean selected : selectedArr) {
                setSelected(selected);
                if (isCancelled()) break;
            }
            return null;
        }

        protected void onProgressUpdate(Void... progress) {

        }

        protected void onPostExecute(Void result) {
            System.out.println("Saved Category selection");
        }
    }

    public boolean contentEquals(final ContentCategory rhs) {
        if (rhs == null) {
            return false;
        }
        if (this == rhs) {
            return true;
        }
        return uid == rhs.getUid() && localizedLabel.equals(rhs.localizedLabel);
    }
}
