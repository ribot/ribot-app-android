package io.ribot.app.data.model;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class Ribot implements Comparable<Ribot> {

    public Profile profile;
    @Nullable public CheckIn latestCheckIn;

    @Override
    public int compareTo(@NonNull Ribot another) {
        return profile.name.first.compareToIgnoreCase(another.profile.name.first);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ribot ribot = (Ribot) o;

        if (profile != null ? !profile.equals(ribot.profile) : ribot.profile != null) return false;
        return latestCheckIn != null ? latestCheckIn.equals(ribot.latestCheckIn) :
                ribot.latestCheckIn == null;

    }

    @Override
    public int hashCode() {
        int result = profile != null ? profile.hashCode() : 0;
        result = 31 * result + (latestCheckIn != null ? latestCheckIn.hashCode() : 0);
        return result;
    }
}
