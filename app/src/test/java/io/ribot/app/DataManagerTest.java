package io.ribot.app;

import android.accounts.Account;
import android.content.Context;
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

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
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
        String apiAccessToken = MockModelFabric.generateRandomString();
        mAuthorization = RibotService.Util.buildAuthorization(apiAccessToken);
        component.getPreferencesHelper().putAccessToken(apiAccessToken);
    }

    @Test
    public void signInSuccessful() {
        // Stub GoogleAuthHelper and RibotService mocks
        RibotService.SignInResponse signInResponse = new RibotService.SignInResponse();
        signInResponse.ribot = MockModelFabric.newRibot();
        signInResponse.accessToken = MockModelFabric.generateRandomString();
        Account account = new Account("ivan@ribot.co.uk", "google.com");
        String googleAccessCode = MockModelFabric.generateRandomString();
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
    public void signOutSuccessful() {
        component.getPreferencesHelper().putSignedInRibot(MockModelFabric.newRibot());
        component.getPreferencesHelper().putAccessToken(MockModelFabric.generateRandomString());

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDataManager.signOut().subscribe(testSubscriber);
        testSubscriber.assertCompleted();

        assertNull(component.getPreferencesHelper().getAccessToken());
        assertNull(component.getPreferencesHelper().getSignedInRibot());
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
        CheckInRequest request = CheckInRequest.fromLabel(MockModelFabric.generateRandomString());
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
        CheckInRequest request = CheckInRequest.fromLabel(MockModelFabric.generateRandomString());
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

    private void stubRibotServiceGetVenues(Observable<List<Venue>> observable) {
        doReturn(observable)
                .when(component.getMockRibotService())
                .getVenues(mAuthorization);
    }
}
