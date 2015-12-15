package io.ribot.app;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.util.DefaultConfig;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class PreferencesHelperTest {

    final PreferencesHelper mPreferencesHelper =
            new PreferencesHelper(RuntimeEnvironment.application);

    @Before
    public void setUp() {
        mPreferencesHelper.clear();
    }

    @Test
    public void putAndGetAccessToken() {
        String token = "sexyAccessToken";
        mPreferencesHelper.putAccessToken(token);
        assertEquals(token, mPreferencesHelper.getAccessToken());
    }

    @Test
    public void putAndGetSignedInRibot() {
        Ribot ribot = MockModelFabric.newRibot();
        mPreferencesHelper.putSignedInRibot(ribot);
        assertEquals(ribot, mPreferencesHelper.getSignedInRibot());
    }

    @Test
    public void putAndGetVenues() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        mPreferencesHelper.putVenues(venues);
        assertEquals(venues, mPreferencesHelper.getVenues());
    }

    @Test
    public void getVenuesAsObservable() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        mPreferencesHelper.putVenues(venues);
        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mPreferencesHelper.getVenuesAsObservable().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(venues));
    }

    @Test
    public void getVenuesAsObservableWhenEmpty() {
        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mPreferencesHelper.getVenuesAsObservable().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoValues();
    }

    @Test
    public void putAndGetLatestCheckIn() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        mPreferencesHelper.putLatestCheckIn(checkIn);
        assertEquals(checkIn, mPreferencesHelper.getLatestCheckIn());
    }

    @Test
    public void getLatestCheckInAsObservable() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        mPreferencesHelper.putLatestCheckIn(checkIn);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mPreferencesHelper.getLatestCheckInAsObservable().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(checkIn));
    }

    @Test
    public void getLatestCheckInAsObservableWhenEmpty() {
        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mPreferencesHelper.getLatestCheckInAsObservable().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoValues();
    }

    @Test
    public void putAndGetLatestEncounterBeacon() {
        Encounter encounter = MockModelFabric.newEncounter();
        mPreferencesHelper.putLatestEncounter(encounter);

        assertEquals(encounter.beacon, mPreferencesHelper.getLatestEncounterBeacon());
    }

    @Test
    public void putAndGetLatestEncounterDate() {
        Encounter encounter = MockModelFabric.newEncounter();
        mPreferencesHelper.putLatestEncounter(encounter);

        assertEquals(encounter.encounterDate, mPreferencesHelper.getLatestEncounterDate());
    }

    @Test
    public void putAndGetLatestEncounterCheckInId() {
        Encounter encounter = MockModelFabric.newEncounter();
        mPreferencesHelper.putLatestEncounter(encounter);

        assertEquals(encounter.checkIn.id, mPreferencesHelper.getLatestEncounterCheckInId());
    }

    @Test
    public void putAndClearLatestEncounter() {
        Encounter encounter = MockModelFabric.newEncounter();
        mPreferencesHelper.putLatestEncounter(encounter);
        assertEquals(encounter.checkIn.id, mPreferencesHelper.getLatestEncounterCheckInId());

        mPreferencesHelper.clearLatestEncounter();
        assertNull(mPreferencesHelper.getLatestEncounterCheckInId());
        assertNull(mPreferencesHelper.getLatestEncounterBeacon());
        assertNull(mPreferencesHelper.getLatestEncounterDate());
    }

    @Test
    public void putLatestEncounterOverridesLatestCheckIn() {
        Encounter encounter = MockModelFabric.newEncounter();
        mPreferencesHelper.putLatestEncounter(encounter);

        assertEquals(encounter.checkIn, mPreferencesHelper.getLatestCheckIn());
    }

    @Test
    public void getLatestEncounterBeaconWhenEmpty() {
        assertNull(mPreferencesHelper.getLatestEncounterBeacon());
    }

    @Test
    public void getLatestEncounterDateWhenEmpty() {
        assertNull(mPreferencesHelper.getLatestEncounterDate());
    }
}

