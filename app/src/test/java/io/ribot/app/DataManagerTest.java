package io.ribot.app;

import android.accounts.Account;
import android.text.format.DateUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import io.ribot.app.data.BeaconNotRegisteredException;
import io.ribot.app.data.BusEvent;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.util.EventPosterHelper;
import rx.Observable;
import rx.observers.TestSubscriber;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This test class performs local unit tests without dependencies on the Android framework
 */
@RunWith(MockitoJUnitRunner.class)
public class DataManagerTest {

    @Mock RibotService mMockRibotsService;
    @Mock DatabaseHelper mMockDatabaseHelper;
    @Mock PreferencesHelper mMockPreferencesHelper;
    @Mock GoogleAuthHelper mMockGoogleAuthHelper;
    @Mock EventPosterHelper mMockEventPosterHelper;
    DataManager mDataManager;

    @Before
    public void setUp() {
        mDataManager = new DataManager(mMockRibotsService, mMockDatabaseHelper,
                mMockPreferencesHelper, mMockEventPosterHelper, mMockGoogleAuthHelper);
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
                .when(mMockGoogleAuthHelper)
                .retrieveAuthTokenAsObservable(account);
        doReturn(Observable.just(signInResponse))
                .when(mMockRibotsService)
                .signIn(any(RibotService.SignInRequest.class));

        // Test the sign in Observable
        TestSubscriber<Ribot> testSubscriber = new TestSubscriber<>();
        mDataManager.signIn(account).subscribe(testSubscriber);
        testSubscriber.assertValue(signInResponse.ribot);
        testSubscriber.assertCompleted();
        testSubscriber.assertNoErrors();

        verify(mMockPreferencesHelper).putAccessToken(signInResponse.accessToken);
        verify(mMockPreferencesHelper).putSignedInRibot(signInResponse.ribot);
    }

    @Test
    public void signOutCompletes() {
        doReturn(Observable.empty())
                .when(mMockDatabaseHelper)
                .clearTables();

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.signOut().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
    }

    @Test
    public void signOutClearsData() {
        doReturn(Observable.empty())
                .when(mMockDatabaseHelper)
                .clearTables();

        mDataManager.signOut().subscribe(new TestSubscriber<Void>());

        verify(mMockDatabaseHelper).clearTables();
        verify(mMockPreferencesHelper).clear();
    }

    @Test
    public void getRibots() {
        List<Ribot> ribots = MockModelFabric.newRibotList(17);
        doReturn(Observable.just(ribots))
                .when(mMockRibotsService)
                .getRibots(anyString(), anyString());

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
        stubPreferencesHelperGetVenues(Observable.<List<Venue>>empty());

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(1);
        testSubscriber.assertReceivedOnNext(Collections.singletonList(venuesApi));
        // Check that the API result is cached
        verify(mMockPreferencesHelper).putVenues(venuesApi);
    }

    @Test
    public void getVenuesWhenDataCachedSameAsApi() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        stubRibotServiceGetVenues(Observable.just(venues));
        stubPreferencesHelperGetVenues(Observable.just(venues));

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
        stubPreferencesHelperGetVenues(Observable.just(venuesCache));

        TestSubscriber<List<Venue>> testSubscriber = new TestSubscriber<>();
        mDataManager.getVenues().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertValueCount(2);
        testSubscriber.assertReceivedOnNext(Arrays.asList(venuesCache, venuesApi));
        // Check that the new API result is cached
        verify(mMockPreferencesHelper).putVenues(venuesApi);
    }

    @Test
    public void getVenuesWhenDataCachedAndApiFails() {
        List<Venue> venuesCache = MockModelFabric.newVenueList(4);
        stubRibotServiceGetVenues(Observable.<List<Venue>>error(new RuntimeException()));
        stubPreferencesHelperGetVenues(Observable.just(venuesCache));

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
                .when(mMockRibotsService)
                .checkIn(anyString(), eq(request));

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.checkIn(request).subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(checkIn));
        // Check that is saved in preferences
        verify(mMockPreferencesHelper).putLatestCheckIn(checkIn);
    }

    @Test
    public void checkInFail() {
        CheckInRequest request = CheckInRequest.fromLabel(MockModelFabric.randomString());
        doReturn(Observable.error(new RuntimeException()))
                .when(mMockRibotsService)
                .checkIn(anyString(), eq(request));

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.checkIn(request).subscribe(testSubscriber);
        testSubscriber.assertError(RuntimeException.class);
        testSubscriber.assertNoValues();

        verify(mMockPreferencesHelper, never()).putLatestCheckIn(any(CheckIn.class));
    }

    @Test
    public void getTodayLatestCheckIn() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        doReturn(Observable.just(checkIn))
                .when(mMockPreferencesHelper)
                .getLatestCheckInAsObservable();

        TestSubscriber<CheckIn> testSubscriber = new TestSubscriber<>();
        mDataManager.getTodayLatestCheckIn().subscribe(testSubscriber);
        testSubscriber.assertCompleted();
        testSubscriber.assertReceivedOnNext(Collections.singletonList(checkIn));
    }

    @Test
    public void getTodayLatestCheckInWhenLatestWasBeforeToday() {
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.checkedInDate.setTime(System.currentTimeMillis() - DateUtils.DAY_IN_MILLIS);
        doReturn(Observable.just(checkIn))
                .when(mMockPreferencesHelper)
                .getLatestCheckInAsObservable();

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
                .when(mMockRibotsService)
                .performBeaconEncounter(anyString(), eq(beaconId));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(beaconId).subscribe(testSubscriber);

        testSubscriber.assertCompleted();
        testSubscriber.assertValue(encounter);

        verify(mMockPreferencesHelper).putLatestEncounter(encounter);
    }

    @Test
    public void performBeaconEncounterFails() {
        String beaconId = MockModelFabric.randomString();
        doReturn(Observable.error(new RuntimeException()))
                .when(mMockRibotsService)
                .performBeaconEncounter(anyString(), eq(beaconId));

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(beaconId).subscribe(testSubscriber);

        testSubscriber.assertError(RuntimeException.class);
        testSubscriber.assertNoValues();

        verify(mMockPreferencesHelper, never()).putLatestEncounter(any(Encounter.class));
    }

    @Test
    public void performBeaconEncounterWithUuidMajorAndMinor() {
        RegisteredBeacon registeredBeacon = MockModelFabric.newRegisteredBeacon();
        doReturn(Observable.just(registeredBeacon))
                .when(mMockDatabaseHelper)
                .findRegisteredBeacon(anyString(), anyInt(), anyInt());
        Encounter encounter = MockModelFabric.newEncounter();
        doReturn(Observable.just(encounter))
                .when(mMockRibotsService)
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
        //This beacon is not returned in the local database so the encounter should fail
        RegisteredBeacon registeredBeacon = MockModelFabric.newRegisteredBeacon();
        doReturn(Observable.empty())
                .when(mMockDatabaseHelper)
                .findRegisteredBeacon(anyString(), anyInt(), anyInt());

        TestSubscriber<Encounter> testSubscriber = new TestSubscriber<>();
        mDataManager.performBeaconEncounter(registeredBeacon.uuid,
                registeredBeacon.major, registeredBeacon.minor).subscribe(testSubscriber);
        testSubscriber.assertError(BeaconNotRegisteredException.class);
    }

    @Test
    public void syncRegisteredBeacons() {
        List<RegisteredBeacon> registeredBeacons = MockModelFabric.newRegisteredBeaconList(3);
        doReturn(Observable.just(registeredBeacons))
                .when(mMockRibotsService)
                .getRegisteredBeacons(anyString());
        doReturn(Observable.empty())
                .when(mMockDatabaseHelper)
                .setRegisteredBeacons(anyListOf(RegisteredBeacon.class));


        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.syncRegisteredBeacons().subscribe(testSubscriber);
        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        verify(mMockDatabaseHelper).setRegisteredBeacons(registeredBeacons);
        verify(mMockEventPosterHelper).postEventSafely(any(BusEvent.BeaconsSyncCompleted.class));
    }

    @Test
    public void checkOutCompletesAndEmitsCheckIn() {
        Encounter encounter = MockModelFabric.newEncounter();
        CheckIn checkIn = encounter.checkIn;
        stubPreferencesHelperLatestEncounter(encounter);

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
        stubPreferencesHelperLatestEncounter(encounter);

        checkIn.isCheckedOut = true; // api would return isCheckedOut true if successful
        stubRibotServiceUpdateCheckIn(checkIn);

        mDataManager.checkOut(checkIn.id).subscribe();

        verify(mMockPreferencesHelper).putLatestCheckIn(checkIn);
    }

    @Test
    public void checkOutSuccessfulClearsLatestEncounter() {
        Encounter encounter = MockModelFabric.newEncounter();
        CheckIn checkIn = encounter.checkIn;
        checkIn.isCheckedOut = false;
        stubPreferencesHelperLatestEncounter(encounter);

        checkIn.isCheckedOut = true; // api would return isCheckedOut true if successful
        stubRibotServiceUpdateCheckIn(checkIn);

        mDataManager.checkOut(checkIn.id).subscribe();

        verify(mMockPreferencesHelper).clearLatestEncounter();
    }

    /*********************** Helper methods ***********************/

    private void stubRibotServiceGetVenues(Observable<List<Venue>> observable) {
        doReturn(observable)
                .when(mMockRibotsService)
                .getVenues(anyString());
    }

    private void stubPreferencesHelperGetVenues(Observable<List<Venue>> observable) {
        doReturn(observable)
                .when(mMockPreferencesHelper)
                .getVenuesAsObservable();
    }

    private void stubRibotServiceUpdateCheckIn(CheckIn checkIn) {
        doReturn(Observable.just(checkIn))
                .when(mMockRibotsService)
                .updateCheckIn(anyString(), anyString(),
                        any(RibotService.UpdateCheckInRequest.class));
    }

    private void stubPreferencesHelperLatestEncounter(Encounter encounter) {
        when(mMockPreferencesHelper.getLatestEncounterBeacon()).thenReturn(encounter.beacon);
        when(mMockPreferencesHelper.getLatestEncounterCheckInId()).thenReturn(encounter.checkIn.id);
        when(mMockPreferencesHelper.getLatestEncounterDate()).thenReturn(encounter.encounterDate);
        when(mMockPreferencesHelper.getLatestCheckIn()).thenReturn(encounter.checkIn);
    }

}
