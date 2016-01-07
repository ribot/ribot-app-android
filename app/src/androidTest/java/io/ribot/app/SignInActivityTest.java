package io.ribot.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Intent;
import android.os.Build;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.uiautomator.UiDevice;
import android.support.test.uiautomator.UiObject;
import android.support.test.uiautomator.UiObjectNotFoundException;
import android.support.test.uiautomator.UiSelector;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.signin.SignInActivity;
import retrofit.HttpException;
import retrofit.Response;
import rx.Observable;
import timber.log.Timber;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class SignInActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final IntentsTestRule<SignInActivity> main =
            new IntentsTestRule<>(SignInActivity.class, false, false);
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(main);

    private final Account mSelectedAccount =
            new Account("accounts@ribot.com", SignInActivity.ACCOUNT_TYPE_GOOGLE);

    private UiDevice mDevice;

    @Before
    public void setup() {
        when(component.getMockAccountManager().getAccountsByType(mSelectedAccount.type))
                .thenReturn(new Account[]{mSelectedAccount});
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
    }

    @Test
    public void checkViewsDisplay() {
        main.launchActivity(SignInActivity.getStartIntent(component.getContext(), false));

        onView(withId(R.id.button_sign_in))
                .check(matches(isDisplayed()));
        onView(withText(R.string.sign_in_message))
                .check(matches(isDisplayed()));
        onView(withId(R.id.image_ribot_logo))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signInSuccessfulNavigatesToWelcome() {
        main.launchActivity(SignInActivity.getStartIntent(component.getContext(), false));
        stubAccountPickerIntent();

        // Stub sign in method in the DataManager
        Ribot ribot = MockModelFabric.newRibot();
        doReturn(Observable.just(ribot))
                .when(component.getMockDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
        allowPermissionsIfNeeded();

        // Check that it navigates correctly to the welcome screen
        String expectedWelcome = main.getActivity()
                .getString(R.string.welcome_greetings, ribot.profile.name.first);
        onView(withText(expectedWelcome))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signInFailsWithGeneralError() {
        main.launchActivity(SignInActivity.getStartIntent(component.getContext(), false));
        stubAccountPickerIntent();

        // Stub an error when calling sign in
        doReturn(Observable.error(new RuntimeException("Error")))
                .when(component.getMockDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
        allowPermissionsIfNeeded();

        onView(withText(R.string.error_sign_in))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signInFailsWithProfileNotFound() {
        main.launchActivity(SignInActivity.getStartIntent(component.getContext(), false));
        stubAccountPickerIntent();

        // Stub with http 403 error
        HttpException http403Exception = new HttpException(Response.error(403, null));
        doReturn(Observable.error(http403Exception))
                .when(component.getMockDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
        allowPermissionsIfNeeded();

        String expectedWelcome = main.getActivity()
                .getString(R.string.error_ribot_profile_not_found, mSelectedAccount.name);
        onView(withText(expectedWelcome))
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkPopUpMessageDisplays() {
        String popUpMessage = "You have been signed out";
        Intent intent = SignInActivity
                .getStartIntent(component.getContext(), false, popUpMessage);
        main.launchActivity(intent);

        onView(withText(popUpMessage))
                .check(matches(isDisplayed()));
    }

    private void stubAccountPickerIntent() {
        // Stub the account picker using Espresso intents.
        // It requires the test devices to be signed in into at least 1 Google account.
        Intent data = new Intent();
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, mSelectedAccount.name);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, data);
        // The account picker intent is a bit special and it doesn't seem to have an Action or
        // package, so we have to match it by some of the extra keys.
        // This is not ideal but I couldn't find a better way.
        intending(hasExtraWithKey("allowableAccountTypes"))
                .respondWith(result);
    }

    private void allowPermissionsIfNeeded()  {
        if (Build.VERSION.SDK_INT >= 23) {
            UiObject allowPermissions = mDevice.findObject(new UiSelector().text("Allow"));
            if (allowPermissions.exists()) {
                try {
                    allowPermissions.click();
                } catch (UiObjectNotFoundException e) {
                    Timber.w(e, "There is no permissions dialog to interact with ");
                }
            }
        }
    }
}