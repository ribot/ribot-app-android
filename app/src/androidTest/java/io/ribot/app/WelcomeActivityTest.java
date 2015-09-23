package io.ribot.app;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import io.ribot.app.data.model.Profile;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.WelcomeActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class WelcomeActivityTest {

    public final TestComponentRule component = new TestComponentRule(
            RibotApplication.get(InstrumentationRegistry.getTargetContext()),
            true);
    public final ActivityTestRule<WelcomeActivity> main =
            new ActivityTestRule<>(WelcomeActivity.class, false, false);
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(main);

    @Test
    public void checkViewsDisplay() {
        Profile profile = MockModelFabric.newProfile();
        main.launchActivity(WelcomeActivity.newStartIntent(component.getApplication(), profile));

        String expectedGreeting = main.getActivity()
                .getString(R.string.welcome_greetings, profile.name.first);
        onView(withText(expectedGreeting))
                .check(matches(isDisplayed()));
        onView(withId(R.id.image_profile))
                .check(matches(isDisplayed()));
    }

}
