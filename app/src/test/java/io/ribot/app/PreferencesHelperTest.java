package io.ribot.app;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.Collections;
import java.util.List;

import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.util.DefaultConfig;
import rx.observers.TestSubscriber;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class PreferencesHelperTest {

    PreferencesHelper mPreferencesHelper;

    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application);

    @Before
    public void setUp() {
        mPreferencesHelper = component.getPreferencesHelper();
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
}

