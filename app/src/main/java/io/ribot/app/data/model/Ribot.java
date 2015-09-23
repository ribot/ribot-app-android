package io.ribot.app.data.model;

public class Ribot {

    public Profile profile;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Ribot ribot = (Ribot) o;

        return !(profile != null ? !profile.equals(ribot.profile) : ribot.profile != null);

    }

    @Override
    public int hashCode() {
        return profile != null ? profile.hashCode() : 0;
    }

}
