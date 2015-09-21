package io.ribot.app;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.ribot.app.injection.TestComponentRule;
import io.ribot.app.ui.activity.MainActivity;
import io.ribot.app.util.ClearDataRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<>(MainActivity.class, false, false);
    @Rule
    public final TestComponentRule component = new TestComponentRule();
    @Rule
    public final ClearDataRule clearDataRule = new ClearDataRule(component);

    @Test
    public void checkActivityStarts() {
        main.launchActivity(null);
        onView(withText(R.string.app_name))
                .check(matches(isDisplayed()));
    }

}