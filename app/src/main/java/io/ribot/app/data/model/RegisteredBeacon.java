package io.ribot.app.data.model;

public class RegisteredBeacon {
    public String id;
    public String uuid;
    public Integer major;
    public Integer minor;
    public Zone zone;

    public RegisteredBeacon() {

    }

    public RegisteredBeacon(String id, String uuid, Integer major, Integer minor) {
        this.id = id;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public boolean hasSameUuidMajorMinor(String uuid, int major, int minor) {
        return this.uuid.equalsIgnoreCase(uuid) && this.major == major && this.minor == minor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RegisteredBeacon beacon = (RegisteredBeacon) o;

        if (id != null ? !id.equals(beacon.id) : beacon.id != null) return false;
        if (uuid != null ? !uuid.equals(beacon.uuid) : beacon.uuid != null) return false;
        if (major != null ? !major.equals(beacon.major) : beacon.major != null) return false;
        if (minor != null ? !minor.equals(beacon.minor) : beacon.minor != null) return false;
        return !(zone != null ? !zone.equals(beacon.zone) : beacon.zone != null);

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (uuid != null ? uuid.hashCode() : 0);
        result = 31 * result + (major != null ? major.hashCode() : 0);
        result = 31 * result + (minor != null ? minor.hashCode() : 0);
        result = 31 * result + (zone != null ? zone.hashCode() : 0);
        return result;
    }
}
