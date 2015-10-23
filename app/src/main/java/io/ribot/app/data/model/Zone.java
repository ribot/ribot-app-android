package io.ribot.app.data.model;

public class Zone {
    public String id;
    public String label;
    public Venue venue;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Zone zone = (Zone) o;

        if (id != null ? !id.equals(zone.id) : zone.id != null) return false;
        if (label != null ? !label.equals(zone.label) : zone.label != null) return false;
        return !(venue != null ? !venue.equals(zone.venue) : zone.venue != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (venue != null ? venue.hashCode() : 0);
        return result;
    }
}
