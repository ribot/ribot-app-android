package io.ribot.app.data.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Date;

public class Profile implements Parcelable {
    public Name name;
    public String email;
    public String hexColor;
    public String avatar;
    public Date dateOfBirth;
    public String bio;

    public Profile() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Profile profile = (Profile) o;

        if (name != null ? !name.equals(profile.name) : profile.name != null) return false;
        if (email != null ? !email.equals(profile.email) : profile.email != null) return false;
        if (hexColor != null ? !hexColor.equals(profile.hexColor) : profile.hexColor != null)
            return false;
        if (avatar != null ? !avatar.equals(profile.avatar) : profile.avatar != null)
            return false;
        if (dateOfBirth != null
                ? !dateOfBirth.equals(profile.dateOfBirth) : profile.dateOfBirth != null)
            return false;
        return !(bio != null ? !bio.equals(profile.bio) : profile.bio != null);

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (hexColor != null ? hexColor.hashCode() : 0);
        result = 31 * result + (avatar != null ? avatar.hashCode() : 0);
        result = 31 * result + (dateOfBirth != null ? dateOfBirth.hashCode() : 0);
        result = 31 * result + (bio != null ? bio.hashCode() : 0);
        return result;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.name, 0);
        dest.writeString(this.email);
        dest.writeString(this.hexColor);
        dest.writeString(this.avatar);
        dest.writeLong(dateOfBirth != null ? dateOfBirth.getTime() : -1);
        dest.writeString(this.bio);
    }

    protected Profile(Parcel in) {
        this.name = in.readParcelable(Name.class.getClassLoader());
        this.email = in.readString();
        this.hexColor = in.readString();
        this.avatar = in.readString();
        long tmpDateOfBirth = in.readLong();
        this.dateOfBirth = tmpDateOfBirth == -1 ? null : new Date(tmpDateOfBirth);
        this.bio = in.readString();
    }

    public static final Parcelable.Creator<Profile> CREATOR = new Parcelable.Creator<Profile>() {
        public Profile createFromParcel(Parcel source) {
            return new Profile(source);
        }

        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };
}
