package io.ribot.app;

import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;

import java.util.List;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Venue;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.checkin.CheckInActivity;
import rx.Observable;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static io.ribot.app.util.CustomMatchers.hasCompoundDrawableRelative;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;

public class CheckInActivityTest {

    public final TestComponentRule component =
            new TestComponentRule(InstrumentationRegistry.getTargetContext());
    public final IntentsTestRule<CheckInActivity> main =
            new IntentsTestRule<>(CheckInActivity.class, false, false);
    // TestComponentRule needs to go first so we make sure the ApplicationTestComponent is set
    // in the Application before any Activity is launched.
    @Rule
    public TestRule chain = RuleChain.outerRule(component).around(main);

    private List<Venue> mVenues;

    @Before
    public void setUp() {
        mVenues = MockModelFabric.newVenueList(10);
        doReturn(Observable.just(mVenues))
                .when(component.getMockDataManager())
                .getVenues();
        // Stub to return empty by default, some tests may override this stub
        doReturn(Observable.empty())
                .when(component.getMockDataManager())
                .getTodayLatestCheckIn();
    }

    @Test
    public void checkInButtonShowsOnlyWhenTyping() {
        main.launchActivity(null);

        onView(withId(R.id.fab_check_in))
                .check(matches(not(isDisplayed())));
        onView(withId(R.id.edit_text_location))
                .perform(typeText("abc"));
        onView(withId(R.id.fab_check_in))
                .check(matches(isDisplayed()));
    }

    @Test
    public void venuesShow() {
        main.launchActivity(null);

        for (int position = 0; position < mVenues.size(); position++) {
            Venue venue = mVenues.get(position);
            onView(withId(R.id.recycler_view_venues))
                    .perform(RecyclerViewActions.scrollToPosition(position));
            onView(withText(venue.label))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void previousVenueCheckInShows() {
        Venue venue = mVenues.get(2);
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.venue = venue;
        doReturn(Observable.just(checkIn))
                .when(component.getMockDataManager())
                .getTodayLatestCheckIn();
        main.launchActivity(null);

        onView(allOf(withId(R.id.image_venue_tick), isDisplayed()))
                .check(matches(isDisplayed()));
    }

    @Test
    public void previousLabelCheckInShows() {
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        doReturn(Observable.just(checkIn))
                .when(component.getMockDataManager())
                .getTodayLatestCheckIn();
        main.launchActivity(null);

        onView(withId(R.id.edit_text_location))
                .check(matches(withHint(checkIn.label)))
                .check(matches(hasCompoundDrawableRelative(false, false, true, false)));
    }

    @Test
    public void checkInWithLabelSuccessful() {
        main.launchActivity(null);
        String label = "The cow";
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        checkIn.label = label;
        doReturn(Observable.just(checkIn))
                .when(component.getMockDataManager())
                .checkIn(any(CheckInRequest.class));

        onView(withId(R.id.edit_text_location))
                .check(matches(hasCompoundDrawableRelative(false, false, false, false)))
                .perform(typeText(label));
        onView(withId(R.id.fab_check_in))
                .perform(click());
        onView(withId(R.id.edit_text_location))
                .check(matches(hasCompoundDrawableRelative(false, false, true, false)));
    }

    @Test
    public void checkInWithLabelFail() {
        main.launchActivity(null);
        String label = "The cow";
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getMockDataManager())
                .checkIn(any(CheckInRequest.class));

        onView(withId(R.id.edit_text_location))
                .perform(typeText(label));
        onView(withId(R.id.fab_check_in))
                .perform(click());
        onView(withText(main.getActivity().getString(R.string.manual_check_in_error)))
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkInAtVenueSuccessful() {
        main.launchActivity(null);
        int venuePosition = 3;
        Venue venue = mVenues.get(venuePosition);
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.venue = venue;
        doReturn(Observable.just(checkIn))
                .when(component.getMockDataManager())
                .checkIn(CheckInRequest.fromVenue(venue.id));

        onView(withId(R.id.recycler_view_venues))
                .perform(RecyclerViewActions.actionOnItemAtPosition(venuePosition, click()));
        onView(allOf(withId(R.id.image_venue_tick), isDisplayed()))
                .check(matches(isDisplayed()));
    }

    @Test
    public void checkInAtVenueFail() {
        main.launchActivity(null);
        int venuePosition = 3;
        Venue venue = mVenues.get(venuePosition);
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getMockDataManager())
                .checkIn(CheckInRequest.fromVenue(venue.id));

        onView(withId(R.id.recycler_view_venues))
                .perform(RecyclerViewActions.actionOnItemAtPosition(venuePosition, click()));
        onView(withText(main.getActivity().getString(R.string.manual_check_in_error)))
                .check(matches(isDisplayed()));
    }

}
