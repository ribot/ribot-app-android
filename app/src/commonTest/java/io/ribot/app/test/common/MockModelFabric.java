package io.ribot.app.test.common;

import java.util.Date;
import java.util.UUID;

import io.ribot.app.data.model.Name;
import io.ribot.app.data.model.Profile;
import io.ribot.app.data.model.Ribot;

public class MockModelFabric {

    public static String generateRandomString() {
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
        profile.bio = generateRandomString();
        profile.dateOfBirth = new Date();
        profile.hexColor = "#FFFFFF";
        return profile;
    }

    public static Name newName() {
        Name name = new Name();
        name.first = generateRandomString();
        name.last = generateRandomString();
        return name;
    }

}
