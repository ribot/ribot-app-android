package io.ribot.app;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.ClearDataRule;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.signin.SignInActivity;
import retrofit.HttpException;
import retrofit.Response;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class SignInActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ClearDataRule clearDataRule = new ClearDataRule(component);
    public final IntentsTestRule<SignInActivity> main =
            new IntentsTestRule<>(SignInActivity.class, false, false);
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    // ClearDataRule must run after the TestComponent is set up but before ActivityTestRule.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    private Account mSelectedAccount;

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
                .when(component.getDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
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
                .when(component.getDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
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
                .when(component.getDataManager())
                .signIn(mSelectedAccount);

        onView(withId(R.id.button_sign_in))
                .perform(click());
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
        AccountManager accountManager = AccountManager
                .get(InstrumentationRegistry.getTargetContext());
        Account[] deviceAccounts = accountManager.getAccountsByType("com.google");
        mSelectedAccount = deviceAccounts[0];
        data.putExtra(AccountManager.KEY_ACCOUNT_NAME, mSelectedAccount.name);
        ActivityResult result = new ActivityResult(Activity.RESULT_OK, data);
        // The account picker intent is a bit special and it doesn't seem to have an Action or
        // package, so we have to match it by some of the extra keys.
        // This is not ideal but I couldn't find a better way.
        intending(hasExtraWithKey("allowableAccountTypes"))
                .respondWith(result);
    }
}
