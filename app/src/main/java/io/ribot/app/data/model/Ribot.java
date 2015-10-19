package io.ribot.app.data.model;

import android.support.annotation.Nullable;

import java.util.List;

public class Ribot implements Comparable<Ribot> {

    public Profile profile;
    public List<CheckIn> checkIns;

    @Nullable
    public CheckIn getLatestCheckIn() {
        if (checkIns != null) {
            CheckIn latestCheckIn = null;
            for (CheckIn checkIn : checkIns) {
                if (latestCheckIn != null &&
                        checkIn.checkedInDate.after(latestCheckIn.checkedInDate)) {
                    latestCheckIn = checkIn;
                } else if (latestCheckIn == null) {
                    latestCheckIn = checkIn;
                }
            }
            return latestCheckIn;
        }
        return null;
    }

    @Override
    public int compareTo(Ribot another) {
        return profile.name.first.compareToIgnoreCase(another.profile.name.first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ribot ribot = (Ribot) o;

        if (profile != null ? !profile.equals(ribot.profile) : ribot.profile != null) return false;
        return !(checkIns != null ? !checkIns.equals(ribot.checkIns) : ribot.checkIns != null);

    }

    @Override
    public int hashCode() {
        int result = profile != null ? profile.hashCode() : 0;
        result = 31 * result + (checkIns != null ? checkIns.hashCode() : 0);
        return result;
    }
}
