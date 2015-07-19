package org.techteam.decider.content;

import android.os.Parcel;
import android.os.Parcelable;

public class UserData implements Parcelable {
    private ImageData avatar;
    private String username;
    private String firstName;
    private String lastName;
    private String birthday;
    private String country;
    private String city;
    private String about;

    public UserData() {
    }

    public UserData(Parcel in) {
        avatar = in.readParcelable(ImageData.class.getClassLoader());
        username = in.readString();
        firstName = in.readString();
        lastName = in.readString();
        birthday = in.readString();
        country = in.readString();
        city = in.readString();
        about = in.readString();
    }

    public ImageData getAvatar() {
        return avatar;
    }

    public boolean hasAvatar() {
        return avatar != null;
    }

    public UserData setAvatar(ImageData avatar) {
        this.avatar = avatar;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public boolean hasUsername() {
        return username != null;
    }

    public UserData setUsername(String username) {
        this.username = username;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public boolean hasFirstName() {
        return firstName != null;
    }

    public UserData setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public boolean hasLastName() {
        return lastName != null;
    }

    public UserData setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getBirthday() {
        return birthday;
    }

    public boolean hasBirthday() {
        return birthday != null;
    }

    public UserData setBirthday(String birthday) {
        this.birthday = birthday;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public boolean hasCountry() {
        return country != null;
    }

    public UserData setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getCity() {
        return city;
    }

    public boolean hasCity() {
        return city != null;
    }

    public UserData setCity(String city) {
        this.city = city;
        return this;
    }

    public String getAbout() {
        return about;
    }

    public boolean hasAbout() {
        return about != null;
    }

    public UserData setAbout(String about) {
        this.about = about;
        return this;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(avatar, flags);
        dest.writeString(username);
        dest.writeString(firstName);
        dest.writeString(lastName);
        dest.writeString(birthday);
        dest.writeString(country);
        dest.writeString(city);
        dest.writeString(about);
    }

    public static final Parcelable.Creator<UserData> CREATOR
            = new Parcelable.Creator<UserData>() {
        public UserData createFromParcel(Parcel in) {
            return new UserData(in);
        }

        public UserData[] newArray(int size) {
            return new UserData[size];
        }
    };

    public String createFingerprint() {
        StringBuilder sb = new StringBuilder();
        if (hasAvatar()) { sb.append(avatar.getPreviewFilename()); sb.append(";"); }
        if (hasUsername()) { sb.append(username); sb.append(";"); }
        if (hasFirstName()) { sb.append(firstName); sb.append(";"); }
        if (hasLastName()) { sb.append(lastName); sb.append(";"); }
        if (hasBirthday()) { sb.append(birthday); sb.append(";"); }
        if (hasCountry()) { sb.append(country); sb.append(";"); }
        if (hasCity()) { sb.append(city); sb.append(";"); }
        if (hasAbout()) { sb.append("ABOUT_LENGTH="); sb.append(about.length()); sb.append(";"); }
        return sb.toString();
    }
}
