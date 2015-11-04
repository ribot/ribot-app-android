package io.ribot.app;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.test.common.MockModelFabric;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class CheckInTest {

    @Test
    public void getLatestEncounter() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        Encounter encounterOldest = MockModelFabric.newEncounter(); // 2 days ago
        Encounter encounterMiddle = MockModelFabric.newEncounter(); // Yesterday
        Encounter encounterLatest = MockModelFabric.newEncounter(); // Today
        encounterOldest.encounterDate.setTime(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(2));
        encounterMiddle.encounterDate.setTime(
                System.currentTimeMillis() - TimeUnit.DAYS.toMillis(1));
        checkIn.beaconEncounters = Arrays.asList(encounterMiddle, encounterLatest, encounterOldest);

        assertEquals(encounterLatest, checkIn.getLatestEncounter());
    }

    @Test
    public void getLatestEncounterReturnsNullWhenEncounterListEmpty() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.beaconEncounters = Collections.emptyList();
        assertNull(checkIn.getLatestEncounter());
    }

    @Test
    public void getLatestEncounterReturnsNullWhenEncounterListNull() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.beaconEncounters = null;
        assertNull(checkIn.getLatestEncounter());
    }
}
