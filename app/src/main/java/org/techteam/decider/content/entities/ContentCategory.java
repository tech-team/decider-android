package org.techteam.decider.content.entities;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;

import org.json.JSONException;
import org.json.JSONObject;

@Table(name = "Categories")
public class ContentCategory extends Model {

    @Column(name = "uid")
    private int uid;

    @Column(name = "localized_label")
    private String localizedLabel;

    public ContentCategory(int uid, String localizedLabel) {
        this.uid = uid;
        this.localizedLabel = localizedLabel;
    }

    public int getUid() {
        return uid;
    }

    public String getLocalizedLabel() {
        return localizedLabel;
    }

    public static ContentCategory fromJson(JSONObject obj) throws JSONException {
        int uid = obj.getInt("id");
        String name = obj.getString("name");
        return new ContentCategory(uid, name);
    }
}
