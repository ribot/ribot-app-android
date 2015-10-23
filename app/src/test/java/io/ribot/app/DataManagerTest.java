package io.ribot.app;

import android.accounts.Account;
import android.content.Context;
import android.database.Cursor;
import android.text.format.DateUtils;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ribot.app.data.BeaconNotRegisteredException;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.local.Db;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.test.common.ClearDataRule;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.util.DefaultConfig;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class DataManagerTest {

    private DataManager mDataManager;
    private String mAuthorization;

    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application);
    @Rule
    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    @Before
    public void setUp() {
        mDataManager = component.getDataManager();
        String apiAccessToken = MockModelFabric.randomString();
        mAuthorization = RibotService.Util.buildAuthorization(apiAccessToken);
        component.getPreferencesHelper().putAccessToken(apiAccessToken);
    }

    @Test
    public void signInSuccessful() {
        // Stub GoogleAuthHelper and RibotService mocks
        RibotService.SignInResponse signInResponse = new RibotService.SignInResponse();
        signInResponse.ribot = MockModelFabric.newRibot();
        signInResponse.accessToken = MockModelFabric.randomString();
        Account account = new Account("ivan@ribot.co.uk", "google.com");
        String googleAccessCode = MockModelFabric.randomString();
        doReturn(Observable.just(googleAccessCode))
                .when(component.getMockGoogleAuthHelper())
                .retrieveAuthTokenAsObservable(any(Context.class), eq(account));
        doReturn(Observable.just(signInResponse))
                .when(component.getMockRibotService())
                .signIn(any(RibotService.SignInRequest.class));
        // Test the sign in Observable
        TestSubscriber<Ribot> testSubscriber = new TestSubscriber<>();
        mDataManager.signIn(RuntimeEnvironment.application, account).subscribe(testSubscriber);
        testSubscriber.assertValue(signInResponse.ribot);
        testSubscriber.assertCompleted();
        // Check that it saves the correct data in preferences after a successful sign in.
        assertEquals(signInResponse.accessToken, component.getPreferencesHelper().getAccessToken());
        assertEquals(signInResponse.ribot, component.getPreferencesHelper().getSignedInRibot());
    }

    @Test
    public void signOutClearSharedPreferences() {
        component.getPreferencesHelper().putSignedInRibot(MockModelFabric.newRibot());
        component.getPreferencesHelper().putAccessToken(MockModelFabric.randomString());

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.signOut().subscribe(testSubscriber);
        testSubscriber.assertCompleted();

        assertNull(component.getPreferencesHelper().getAccessToken());
        assertNull(component.getPreferencesHelper().getSignedInRibot());
    }

    @Test
    public void signOutClearDatabase() {
        RegisteredBeacon beacon = MockModelFabric.newRegisteredBeacon();
        component.getDatabaseHelper().getBriteDb()
                .insert(Db.BeaconTable.TABLE_NAME, Db.BeaconTable.toContentValues(beacon));

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.signOut().subscribe(testSubscriber);
        testSubscriber.assertCompleted();

        Cursor cursor = component.getDatabaseHelper()
                .getBriteDb().query("SELECT * FROM " + Db.BeaconTable.TABLE_NAME);
        assertEquals(0, cursor.getCount());
        cursor.close();
    }

    @Test
    public void getRibots() {
        List<Ribot> ribots = MockModelFabric.newRibotList(17);

        doReturn(Observable.just(ribots))
                .when(component.getMockRibotService())
                .getRibots(mAuthorization, "checkins");

        TestSubscriber<List<Ribot>> testSubscriber = new TestSubscriber<>();
        mDataManager.getRibots().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertValue(ribots);
    }

    @Test
    public void getVenuesWhenEmptyCache() {
        List<Venue> venuesApi = MockModelFabric.newVenueList(10);
        stubRibotServiceGetVenues(Observable.just(venuesApi));

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertReceivedOnNext(Collections.singletonList(venuesApi));
        // Check that the API result is cached
        assertEquals(venuesApi, component.getPreferencesHelper().getVenues());
    }

    @Test
    public void getVenuesWhenDataCachedSameAsApi() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        stubRibotServiceGetVenues(Observable.just(venues));
        component.getPreferencesHelper().putVenues(venues);

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertReceivedOnNext(Collections.singletonList(venues));
    }

    @Test
    public void getVenuesWhenDataCachedDifferentToApi() {
        List<Venue> venuesApi = MockModelFabric.newVenueList(10);
        List<Venue> venuesCache = MockModelFabric.newVenueList(4);
        stubRibotServiceGetVenues(Observable.just(venuesApi));
        component.getPreferencesHelper().putVenues(venuesCache);

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(2);
        testSubscriber.assertReceivedOnNext(Arrays.asList(venuesCache, venuesApi));
        // Check that the new API result is cached
        assertEquals(venuesApi, component.getPreferencesHelper().getVenues());
    }

    @Test
    public void getVenuesWhenDataCachedAndApiFails() {
        List<Venue> venuesCache = MockModelFabric.newVenueList(4);
        stubRibotServiceGetVenues(Observable.<List<Venue>>error(new RuntimeException()));
        component.getPreferencesHelper().putVenues(venuesCache);

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertReceivedOnNext(Collections.singletonList(venuesCache));
    }

    @Test
    public void checkInSuccessful() {
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        CheckInRequest request = CheckInRequest.fromLabel(MockModelFabric.randomString());
        doReturn(Observable.just(checkIn))
                .when(component.getMockRibotService())
                .checkIn(mAuthorization, request);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.checkIn(request).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(checkIn));
        // Check that is saved in preferences
        assertEquals(checkIn, mDataManager.getPreferencesHelper().getLatestCheckIn());
    }

    @Test
    public void checkInFail() {
        CheckInRequest request = CheckInRequest.fromLabel(MockModelFabric.randomString());
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getMockRibotService())
                .checkIn(mAuthorization, request);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.checkIn(request).subscribe(testSubscriber);
        testSubscriber.assertError(RuntimeException.class);
        testSubscriber.assertNoValues();

        assertNull(mDataManager.getPreferencesHelper().getLatestCheckIn());
    }

    @Test
    public void getTodayLatestCheckIn() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        mDataManager.getPreferencesHelper().putLatestCheckIn(checkIn);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.getTodayLatestCheckIn().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(checkIn));
    }

    @Test
    public void getTodayLatestCheckInWhenLatestWasBeforeToday() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.checkedInDate.setTime(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
        mDataManager.getPreferencesHelper().putLatestCheckIn(checkIn);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.getTodayLatestCheckIn().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoValues();
    }

    @Test
    public void performBeaconEncounter() {
        String beaconId = MockModelFabric.randomString();
        Encounter encounter = MockModelFabric.newEncounter();
        doReturn(Observable.just(encounter))
                .when(component.getMockRibotService())
                .performBeaconEncounter(anyString(), eq(beaconId));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(beaconId).subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertValue(encounter);

        assertEquals(encounter.beacon,
                mDataManager.getPreferencesHelper().getLatestEncounterBeacon());
        assertEquals(encounter.encounterDate,
                mDataManager.getPreferencesHelper().getLatestEncounterDate());
    }

    @Test
    public void performBeaconEncounterFails() {
        String beaconId = MockModelFabric.randomString();
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getMockRibotService())
                .performBeaconEncounter(anyString(), eq(beaconId));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(beaconId).subscribe(testSubscriber);

        testSubscriber.assertError(RuntimeException.class);
        testSubscriber.assertNoValues();

        assertNull(mDataManager.getPreferencesHelper().getLatestEncounterBeacon());
        assertNull(mDataManager.getPreferencesHelper().getLatestEncounterDate());
    }

    @Test
    public void performBeaconEncounterWithUuidMajorAndMinor() {
        RegisteredBeacon registeredBeacon = MockModelFabric.newRegisteredBeacon();
        component.getDatabaseHelper()
                .setRegisteredBeacons(Collections.singletonList(registeredBeacon)).subscribe();

        Encounter encounter = MockModelFabric.newEncounter();
        doReturn(Observable.just(encounter))
                .when(component.getMockRibotService())
                .performBeaconEncounter(anyString(), eq(registeredBeacon.id));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(registeredBeacon.uuid,
                registeredBeacon.major, registeredBeacon.minor).subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(encounter);
        testSubscriber.assertCompleted();
    }

    @Test
    public void performBeaconEncounterFailWithBeaconNotRegistered() {
        //This beacon is not saved in the local database so the encounter should fail
        RegisteredBeacon registeredBeacon = MockModelFabric.newRegisteredBeacon();

        Encounter encounter = MockModelFabric.newEncounter();
        doReturn(Observable.just(encounter))
                .when(component.getMockRibotService())
                .performBeaconEncounter(anyString(), eq(registeredBeacon.id));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(registeredBeacon.uuid,
                registeredBeacon.major, registeredBeacon.minor).subscribe(testSubscriber);
        testSubscriber.assertError(BeaconNotRegisteredException.class);
    }

    @Test
    public void syncRegisteredBeacons() {
        List<RegisteredBeacon> registeredBeacons = MockModelFabric.newRegisteredBeaconList(3);
        doReturn(Observable.just(registeredBeacons))
                .when(component.getMockRibotService())
                .getRegisteredBeacons(anyString());

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.syncRegisteredBeacons().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        Cursor cursor = component.getDatabaseHelper().getBriteDb()
                .query("SELECT * FROM " + Db.BeaconTable.TABLE_NAME);
        assertEquals(registeredBeacons.size(), cursor.getCount());
        cursor.close();
    }

    @Test
    public void checkOutCompletesAndEmitsCheckIn() {
        Encounter encounter = MockModelFabric.newEncounter();
        CheckIn checkIn = encounter.checkIn;
        component.getPreferencesHelper().putLatestEncounter(encounter);

        checkIn.isCheckedOut = true;
        stubRibotServiceUpdateCheckIn(checkIn);

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.checkOut(checkIn.id).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(checkIn);
    }

    @Test
    public void checkOutSuccessfulUpdatesLatestCheckIn() {
        Encounter encounter = MockModelFabric.newEncounter();
        CheckIn checkIn = encounter.checkIn;
        checkIn.isCheckedOut = false;
        component.getPreferencesHelper().putLatestEncounter(encounter);

        checkIn.isCheckedOut = true; // api would return isCheckedOut true if successful
        stubRibotServiceUpdateCheckIn(checkIn);

        mDataManager.checkOut(checkIn.id).subscribe();

        assertEquals(checkIn, component.getPreferencesHelper().getLatestCheckIn());
    }

    @Test
    public void checkOutSuccessfulClearsLatestEncounter() {
        Encounter encounter = MockModelFabric.newEncounter();
        CheckIn checkIn = encounter.checkIn;
        checkIn.isCheckedOut = false;
        component.getPreferencesHelper().putLatestEncounter(encounter);

        checkIn.isCheckedOut = true; // api would return isCheckedOut true if successful
        stubRibotServiceUpdateCheckIn(checkIn);

        mDataManager.checkOut(checkIn.id).subscribe();

        assertNull(component.getPreferencesHelper().getLatestEncounterDate());
        assertNull(component.getPreferencesHelper().getLatestEncounterCheckInId());
        assertNull(component.getPreferencesHelper().getLatestEncounterBeacon());
    }

    /*********************** Helper methods ***********************/

    private void stubRibotServiceGetVenues(Observable<List<Venue>> observable) {
        doReturn(observable)
                .when(component.getMockRibotService())
                .getVenues(mAuthorization);
    }

    private void stubRibotServiceUpdateCheckIn(CheckIn checkIn) {
        doReturn(Observable.just(checkIn))
                .when(component.getMockRibotService())
                .updateCheckIn(anyString(), eq(checkIn.id),
                        any(RibotService.UpdateCheckInRequest.class));
    }

}
