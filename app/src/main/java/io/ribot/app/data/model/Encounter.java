package io.ribot.app.data.model;

import java.util.Date;

/**
 * A beacon Encounter
 */
public class Encounter {
    public String id;
    public Date encounterDate;
    public RegisteredBeacon beacon;
    public CheckIn checkIn;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Encounter encounter = (Encounter) o;

        if (id != null ? !id.equals(encounter.id) : encounter.id != null) return false;
        if (encounterDate != null ? !encounterDate.equals(encounter.encounterDate) :
                encounter.encounterDate != null)
            return false;
        if (beacon != null ? !beacon.equals(encounter.beacon) : encounter.beacon != null)
            return false;
        return !(checkIn != null ? !checkIn.equals(encounter.checkIn) : encounter.checkIn != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (encounterDate != null ? encounterDate.hashCode() : 0);
        result = 31 * result + (beacon != null ? beacon.hashCode() : 0);
        result = 31 * result + (checkIn != null ? checkIn.hashCode() : 0);
        return result;
    }
}
