package io.ribot.app.data.model;

public class CheckInRequest {
    String venueId;
    String label;
    Float latitude;
    Float longitude;

    private CheckInRequest() {
    }

    public static CheckInRequest fromVenue(String venueId) {
        CheckInRequest request = new CheckInRequest();
        request.venueId = venueId;
        return request;
    }

    public static CheckInRequest fromLabel(String label) {
        CheckInRequest request = new CheckInRequest();
        request.label = label;
        return request;
    }

    public static CheckInRequest fromLabel(String label, float latitude, float longitude) {
        CheckInRequest request = fromLabel(label);
        request.latitude = latitude;
        request.longitude = longitude;
        return request;
    }

    public String getVenueId() {
        return venueId;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CheckInRequest request = (CheckInRequest) o;

        if (venueId != null ? !venueId.equals(request.venueId) : request.venueId != null)
            return false;
        if (label != null ? !label.equals(request.label) : request.label != null) return false;
        if (latitude != null ? !latitude.equals(request.latitude) : request.latitude != null)
            return false;
        return !(longitude != null ? !longitude.equals(request.longitude) :
                request.longitude != null);

    }

    @Override
    public int hashCode() {
        int result = venueId != null ? venueId.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        result = 31 * result + (latitude != null ? latitude.hashCode() : 0);
        result = 31 * result + (longitude != null ? longitude.hashCode() : 0);
        return result;
    }
}
