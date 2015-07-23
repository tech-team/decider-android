package org.techteam.decider.content.entities;

import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.gui.fragments.OnCategorySelectedListener;

import java.util.List;

@Table(name = "Categories", id = BaseColumns._ID)
public class CategoryEntry extends Model {
    public final static String LOCALIZED_LABEL_FIELD = "localized_label";

    @Column(name = "uid", unique = true, onUniqueConflict = Column.ConflictAction.REPLACE)
    private int uid;

    @Column(name = LOCALIZED_LABEL_FIELD)
    private String localizedLabel;

    @Column(name = "selected")
    private boolean selected;

    public CategoryEntry() {
        super();
    }

    public CategoryEntry(int uid, String localizedLabel, boolean selected) {
        super();
        this.uid = uid;
        this.localizedLabel = localizedLabel;
        this.selected = selected;
    }

    public CategoryEntry(int uid, String localizedLabel) {
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

    public void setSelectedAsync(final boolean selected, final OnCategorySelectedListener cb) {
        CategorySelectionSaver saver = new CategorySelectionSaver() {
            @Override
            protected void onPostExecute(Void aVoid) {
                cb.categorySelected(CategoryEntry.this, selected);
            }
        };
        saver.execute(selected);
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
        this.save();
    }

    public static CategoryEntry byUid(int uid) {
        return new Select().from(CategoryEntry.class).where("uid = ?", uid).executeSingle();
    }

    public static List<CategoryEntry> getSelected() {
        return new Select().from(CategoryEntry.class).where("selected = 1").execute();
    }

    public static CategoryEntry fromJson(JSONObject obj) throws JSONException {
        int uid = obj.getInt("id");
        String name = obj.getString("name");
        return new CategoryEntry(uid, name, false);
    }

    public static CategoryEntry fromCursor(Cursor cursor) {
        CategoryEntry entry = new CategoryEntry();
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
    }

    public boolean contentEquals(final CategoryEntry rhs) {
        if (rhs == null) {
            return false;
        }
        if (this == rhs) {
            return true;
        }
        return uid == rhs.getUid() && localizedLabel.equals(rhs.localizedLabel);
    }

    public static void deleteAll() {
        new Delete().from(CategoryEntry.class).execute();
    }
}
