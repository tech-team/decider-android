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

    @Column(name="avatar")
    public String avatar;

    @Column(name="firstName")
    private String firstName;

    @Column(name="middleName")
    private String middleName;

    @Column(name="lastName")
    private String lastName;

    @Column(name="country")
    private String country = "";

    @Column(name="city")
    private String city = "";

    @Column(name="birthday")
    private String birthday = "";

    public UserEntry() {
        super();
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getAvatar() {
        return avatar;
    }

    public static UserEntry byUId(String uid) {
        return new Select().from(UserEntry.class).where("uid = ?", uid).executeSingle();
    }

    public static UserEntry fromJson(JSONObject obj) throws JSONException {
        return fromJson(obj, false);
    }

    public static UserEntry fromJson(JSONObject obj, boolean forceRecreate) throws JSONException {
        UserEntry entry = new UserEntry();
        String uid = obj.getString("uid");
        UserEntry e = byUId(uid); // TODO: probably need to rewrite this without extra select
        if (!forceRecreate && e != null) {
            return e;
        }
        entry.uid = uid;
        entry.username = obj.getString("username");
        entry.avatar = obj.getString("avatar");
        //TODO: new fields
        entry.firstName = obj.getString("first_name");
        entry.middleName = obj.getString("middle_name");
        entry.lastName = obj.getString("last_name");
        if (obj.has("country")) {
            entry.country = obj.getString("country");
        }
        if (obj.has("city")) {
            entry.city = obj.getString("city");
        }
        if (obj.has("birthday")) {
            entry.birthday = obj.getString("birthday");
        }

        return entry;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public String getBirthday() {
        return birthday;
    }
}
