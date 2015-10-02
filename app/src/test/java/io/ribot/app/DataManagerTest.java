package io.ribot.app;

import android.accounts.Account;
import android.content.Context;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
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

    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application);
    @Rule
    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    @Before
    public void setUp() {
        mDataManager = component.getDataManager();
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
}
