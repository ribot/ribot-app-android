package io.ribot.app.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.ribot.app.data.model.Ribot;

public class MockModelsUtil {

    public static String generateRandomString() {
        return UUID.randomUUID().toString();
    }

    public static Ribot createRibot() {
        Ribot ribot = new Ribot();
        ribot.info = new Ribot.Info();
        ribot.id = generateRandomString();
        ribot.hexCode = "#f49637";
        ribot.info.firstName = "Antony";
        ribot.info.lastName = "Ribot";
        ribot.info.role = "CEO";
        return ribot;
    }

    public static List<Ribot> createListRibots(int number) {
        List<Ribot> ribots = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            Ribot ribot = new Ribot();
            ribot.info = new Ribot.Info();
            ribot.id = generateRandomString();
            ribot.hexCode = "#f49637";
            ribot.info.firstName = "Name" + i;
            ribot.info.lastName = "Surname" + i;
            ribot.info.role = "Role" + i;
            ribots.add(ribot);
        }
        return ribots;
    }

}