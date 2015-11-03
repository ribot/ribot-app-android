package io.ribot.app.data.model;

public class Venue {
    public String id;
    public String label;
    public Float latitude;
    public Float longitude;

    public Venue() {
    }

    public Venue(String label) {
        this.label = label;
    }

    public Venue(String id, String label) {
        this.id = id;
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Venue venue = (Venue) o;

        if (id != null ? !id.equals(venue.id) : venue.id != null) return false;
        if (label != null ? !label.equals(venue.label) : venue.label != null) return false;
        if (latitude != null ? !latitude.equals(venue.latitude) : venue.latitude != null)
            return false;
        return !(longitude != null ? !longitude.equals(venue.longitude) : venue.longitude != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        return result;
    }
}
