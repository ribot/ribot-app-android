package io.ribot.app;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
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
import io.ribot.app.ui.main.MainActivity;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    private Ribot mSignedInRibot;

    public final TestComponentRule component = new TestComponentRule(
            RibotApplication.get(InstrumentationRegistry.getTargetContext()),
            true);
    public final ClearDataRule clearDataRule = new ClearDataRule(component);
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<MainActivity>(MainActivity.class) {
                @Override
                protected void beforeActivityLaunched() {
                    mSignedInRibot = MockModelFabric.newRibot();
                    component.getPreferencesHelper().putSignedInRibot(mSignedInRibot);
                }
            };
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    // ClearDataRule must run after the TestComponent is set up but before ActivityTestRule.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(clearDataRule).around(main);

    @Test
    public void checkActivityStarts() {
        onView(withText(R.string.app_name))
                .check(matches(isDisplayed()));
        String text = main.getActivity()
                .getString(R.string.signed_in_welcome, mSignedInRibot.profile.name.first);
        onView(withText(text))
                .check(matches(isDisplayed()));
    }

    @Test
    public void signOutSuccessful() {
        doReturn(Observable.empty())
                .when(component.getDataManager())
                .signOut();

        openActionBarOverflowOrOptionsMenu(main.getActivity());
        onView(withText(R.string.action_sign_out))
                .perform(click());
        // Check that sign in screen open after sign out.
        onView(withText(R.string.action_sign_in))
                .check(matches(isDisplayed()));
    }

}
