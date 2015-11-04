package io.ribot.app.data.model;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.List;

public class CheckIn {
    public String id;
    public Venue venue;
    //Location name. Only to be used if not attached to a specific venue.
    public String label;
    public Date checkedInDate;
    public boolean isCheckedOut;
    public List<Encounter> beaconEncounters;

    public boolean hasVenue() {
        return venue != null && venue.label != null;
    }

    public String getLocationName() {
        if (hasVenue()) {
            return venue.label;
        }
        return label;
    }

    @Nullable
    public Encounter getLatestEncounter() {
        if (beaconEncounters == null || beaconEncounters.isEmpty()) return null;

        Encounter latestEncounter = null;
        for (Encounter encounter : beaconEncounters) {
            if (latestEncounter == null) {
                latestEncounter = encounter;
                continue;
            }
            if (encounter.encounterDate.after(latestEncounter.encounterDate)) {
                latestEncounter = encounter;
            }
        }
        return latestEncounter;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckIn checkIn = (CheckIn) o;

        if (isCheckedOut != checkIn.isCheckedOut) return false;
        if (id != null ? !id.equals(checkIn.id) : checkIn.id != null) return false;
        if (venue != null ? !venue.equals(checkIn.venue) : checkIn.venue != null) return false;
        if (label != null ? !label.equals(checkIn.label) : checkIn.label != null) return false;
        if (checkedInDate != null ? !checkedInDate.equals(checkIn.checkedInDate) :
                checkIn.checkedInDate != null)
            return false;
        return !(beaconEncounters != null ? !beaconEncounters.equals(checkIn.beaconEncounters) :
                checkIn.beaconEncounters != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (venue != null ? venue.hashCode() : 0);
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (checkedInDate != null ? checkedInDate.hashCode() : 0);
        result = 31 * result + (isCheckedOut ? 1 : 0);
        result = 31 * result + (beaconEncounters != null ? beaconEncounters.hashCode() : 0);
        return result;
    }
}
