package io.ribot.app.data;

public class BeaconNotRegisteredException extends Exception {

    public BeaconNotRegisteredException(String uuid, int major, int minor) {
        super(String.format("Beacon with UUID %s, major %d and minor %d not found",
                uuid, major, minor));
    }
}
