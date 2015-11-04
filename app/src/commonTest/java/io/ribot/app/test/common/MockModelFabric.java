package io.ribot.app.test.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.Name;
import io.ribot.app.data.model.Profile;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.data.model.Zone;

public class MockModelFabric {

    public static String randomString() {
        return UUID.randomUUID().toString();
    }

    public static Ribot newRibot() {
        Ribot ribot = new Ribot();
        ribot.profile = newProfile();
        return ribot;
    }

    public static Profile newProfile() {
        Profile profile = new Profile();
        profile.name = newName();
        profile.email = profile.name.first + "@ribot.co.uk";
        profile.avatar = "https://ribot.io/" + profile.name.first + "/avatar";
        profile.bio = randomString();
        profile.dateOfBirth = new Date();
        profile.hexColor = "#FFFFFF";
        return profile;
    }

    public static Name newName() {
        Name name = new Name();
        name.first = randomString();
        name.last = randomString();
        return name;
    }

    public static Venue newVenue() {
        Venue venue = new Venue();
        venue.id = randomString();
        venue.label = venue.id + "_Name";
        venue.latitude = 10f;
        venue.longitude = 20f;
        return venue;
    }

    public static List<Ribot> newRibotList(int size) {
        ArrayList<Ribot> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(newRibot());
        }
        return list;
    }

    public static List<Venue> newVenueList(int size) {
        ArrayList<Venue> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(newVenue());
        }
        return list;
    }

    public static CheckIn newCheckInWithVenue() {
        CheckIn checkIn = new CheckIn();
        checkIn.id = randomString();
        checkIn.checkedInDate = new Date();
        checkIn.venue = newVenue();
        return checkIn;
    }

    public static CheckIn newCheckInWithLabel() {
        CheckIn checkIn = new CheckIn();
        checkIn.id = randomString();
        checkIn.checkedInDate = new Date();
        checkIn.label = randomString();
        return checkIn;
    }

    public static RegisteredBeacon newRegisteredBeacon() {
        Random random = new Random();
        RegisteredBeacon beacon = new RegisteredBeacon();
        beacon.id = randomString();
        beacon.uuid = randomString();
        beacon.major = random.nextInt(20000);
        beacon.minor = random.nextInt(20000);
        beacon.zone = newZone();
        return beacon;
    }

    public static List<RegisteredBeacon> newRegisteredBeaconList(int size) {
        List<RegisteredBeacon> beacons = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            beacons.add(newRegisteredBeacon());
        }
        return beacons;
    }

    public static Encounter newEncounter() {
        Encounter encounter = new Encounter();
        encounter.id = randomString();
        encounter.encounterDate = new Date();
        encounter.checkIn = newCheckInWithVenue();
        encounter.beacon = newRegisteredBeacon();
        return encounter;
    }

    public static Zone newZone() {
        Zone zone = new Zone();
        zone.label = randomString();
        zone.id = randomString();
        return zone;
    }

}
