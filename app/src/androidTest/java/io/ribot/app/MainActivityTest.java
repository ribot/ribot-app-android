package io.ribot.app;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.main.MainActivity;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final ActivityTestRule<MainActivity> main =
            new ActivityTestRule<MainActivity>(MainActivity.class, false, false);
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(main);

    // This Intent starts the MainActivity without launching the auto check-in service so
    // we avoid problems with auto check-in service running in the background during tests.
    private static final Intent MAIN_ACTIVITY_INTENT =
            MainActivity.getStartIntent(InstrumentationRegistry.getTargetContext(), true);

    @Test
    public void signOutSuccessful() {
        doReturn(Observable.just(MockModelFabric.newRibotList(17)))
                .when(component.getMockDataManager())
                .getRibots();

        doReturn(Observable.empty())
                .when(component.getMockDataManager())
                .signOut();

        main.launchActivity(MAIN_ACTIVITY_INTENT);

        openActionBarOverflowOrOptionsMenu(main.getActivity());
        onView(withText(R.string.action_sign_out))
                .perform(click());
        // Check that sign in screen open after sign out.
        onView(withText(R.string.action_sign_in))
                .check(matches(isDisplayed()));
    }

    @Test
    public void displayRibotsInTeamGridSuccess() {
        // We only use 4 items because RecyclerViewActions.scrollToPosition() seemed to be
        // quite buggy when trying to scroll a grid.
        List<Ribot> ribotList = MockModelFabric.newRibotList(4);
        // First ribot is checked in
        ribotList.get(0).latestCheckIn = MockModelFabric.newCheckInWithVenue();
        // Last ribot is checked-in with an encounter
        CheckIn checkInWithEncounters = MockModelFabric.newCheckInWithVenue();
        checkInWithEncounters.latestBeaconEncounter = MockModelFabric.newEncounter();
        ribotList.get(ribotList.size() - 1).latestCheckIn = checkInWithEncounters;
        // The RecylerView will sort the list so we ensure our list is sorted
        // the same before comparing
        Collections.sort(ribotList);
        doReturn(Observable.just(ribotList))
                .when(component.getMockDataManager())
                .getRibots();

        main.launchActivity(MAIN_ACTIVITY_INTENT);

        onView(withId(R.id.text_no_ribots)).check(matches(not(isDisplayed())));
        onView(withId(R.id.recycler_view_team)).check(matches(isDisplayed()));

        checkRibotDisplayOnRecyclerView(ribotList);
    }

    @Test
    public void displayEmptyTeamGrid() {
        List<Ribot> emptyList = new ArrayList<>();
        doReturn(Observable.just(emptyList))
                .when(component.getMockDataManager())
                .getRibots();

        main.launchActivity(MAIN_ACTIVITY_INTENT);

        onView(withText(main.getActivity().getString(R.string.message_no_ribots)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void displayRibotsInTeamGridFailure() {
        doReturn(Observable.just(new RuntimeException()))
                .when(component.getMockDataManager())
                .getRibots();

        main.launchActivity(MAIN_ACTIVITY_INTENT);

        onView(withText(main.getActivity().getString(R.string.error_loading_ribots)))
                .check(matches(isDisplayed()));
    }

    private void checkRibotDisplayOnRecyclerView(List<Ribot> ribotsToCheck) {
        for (int i = 0; i < ribotsToCheck.size(); i++) {
            onView(withId(R.id.recycler_view_team))
                    .perform(RecyclerViewActions.scrollToPosition(i));
            Ribot ribot = ribotsToCheck.get(i);
            CheckIn checkIn = ribot.latestCheckIn;
            if (checkIn != null) {
                Encounter encounter = checkIn.latestBeaconEncounter;
                String expectedLocationName = encounter == null ? checkIn.getLocationName() :
                        encounter.beacon.zone.label;
                onView(withText(expectedLocationName))
                        .check(matches(isDisplayed()));
                onView(withText(ribot.profile.name.first))
                        .check(matches(isDisplayed()));
            }
        }
    }

}
