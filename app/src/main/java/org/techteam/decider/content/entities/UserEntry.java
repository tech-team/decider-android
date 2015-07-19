package org.techteam.decider.content.entities;

import android.provider.BaseColumns;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Select;

import org.json.JSONException;
import org.json.JSONObject;
import org.techteam.decider.content.Entry;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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

    @Column(name="gender")
    private String gender;

    @Column(name="country")
    private String country = "";

    @Column(name="city")
    private String city = "";

    @Column(name="birthday")
    private Date birthday = null;

    private static final SimpleDateFormat birthdayFormat = new SimpleDateFormat("yyyy-MM-dd");

    public enum Gender {
        None("N"),
        Male("M"),
        Female("F");

        String letter;
        private static Gender[] cachedValues = values();

        Gender(String letter) {
            this.letter = letter;
        }

        public String getLetter() {
            return letter;
        }

        public static Gender fromLetter(String letter) {
            for (Gender g : cachedValues) {
                if(g.getLetter().equals(letter)) {
                    return g;
                }
            }
            throw new RuntimeException("Unknown gender id");
        }
    }


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
        String uid = obj.getString("uid");
        UserEntry entry = byUId(uid); // TODO: probably need to rewrite this without extra select
        if (forceRecreate || entry == null) {
            entry = new UserEntry();
        }

        entry.uid = uid;
        entry.username = obj.getString("username");
        entry.avatar = obj.isNull("avatar") ? null : obj.getString("avatar");
        //TODO: new fields
        entry.firstName = obj.getString("first_name");
        entry.middleName = obj.getString("middle_name");
        entry.lastName = obj.getString("last_name");
        entry.gender = obj.has("gender") ? obj.getString("gender") : Gender.None.getLetter();
        if (obj.has("country")) {
            entry.country = obj.isNull("country") ? null : obj.getString("country");
        }
        if (obj.has("city")) {
            entry.city = obj.isNull("city") ? null : obj.getString("city");
        }
        if (obj.has("birthday")) {
            String birthday = obj.isNull("birthday") ? null : obj.getString("birthday");
            if (birthday != null) {
                try {
                    entry.birthday = birthdayFormat.parse(birthday);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
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

    public String getGenderRaw() {
        return gender;
    }

    public Gender getGender() {
        return Gender.fromLetter(gender);
    }

    public String getCountry() {
        return country;
    }

    public String getCity() {
        return city;
    }

    public Date getBirthday() {
        return birthday;
    }
}
