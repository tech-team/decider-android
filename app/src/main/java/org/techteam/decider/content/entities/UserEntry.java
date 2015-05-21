package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.Entry;

@Table(name="Users", id = BaseColumns._ID)
public class UserEntry extends Model {

    @Column(name="uid", unique=true, onUniqueConflict = Column.ConflictAction.REPLACE)
    public String uid;

    @Column(name="username")
    public String username;

    @Column(name="name")
    public String name;

    @Column(name="avatar")
    public String avatar;

    public UserEntry() {
        super();
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getAvatar() {
        return avatar;
    }

    public static UserEntry byUId(String uid) {
        return new Select().from(UserEntry.class).where("uid = ?", uid).executeSingle();
    }

    public static UserEntry fromJson(JSONObject obj) throws JSONException {
        UserEntry entry = new UserEntry();
        String uid = obj.getString("uid");
        UserEntry e = byUId(uid); // TODO: probably need to rewrite this without extra select
        if (e != null) {
            return e;
        }
        entry.uid = uid;
        entry.username = obj.getString("username");
        entry.name = String.format("%s %s", obj.getString("first_name"), obj.getString("last_name"));
        entry.avatar = obj.getString("avatar");
        return entry;
    }
}
