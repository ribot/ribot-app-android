package io.ribot.app;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.List;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Venue;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.checkin.CheckInMvpView;
import io.ribot.app.ui.checkin.CheckInPresenter;
import io.ribot.app.util.DefaultConfig;
import rx.Observable;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class CheckInPresenterTest {

    private CheckInPresenter mPresenter;
    private CheckInMvpView mMockMvpView;

    // We mock the DataManager because there is not need to test the dataManager again
    // from the presenters because there is already a DataManagerTest class.
    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application, true);

    @Before
    public void setUp() {
        mMockMvpView = mock(CheckInMvpView.class);
        when(mMockMvpView.getViewContext()).thenReturn(RuntimeEnvironment.application);
        mPresenter = new CheckInPresenter();
        mPresenter.attachView(mMockMvpView);
    }

    @After
    public void detachView() {
        mPresenter.detachView();
    }

    @Test
    public void loadVenuesSuccessful() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        doReturn(Observable.just(venues))
                .when(component.getDataManager())
                .getVenues();

        mPresenter.loadVenues();
        verify(mMockMvpView).showVenuesProgress(true);
        verify(mMockMvpView).showVenues(venues, null);
        verify(mMockMvpView).showVenuesProgress(false);
    }

    @Test
    public void loadVenuesSuccessfulWhenLatestCheckInToday() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        component.getPreferencesHelper().putLatestCheckIn(checkIn);
        doReturn(Observable.just(venues))
                .when(component.getDataManager())
                .getVenues();

        mPresenter.loadVenues();
        verify(mMockMvpView).showVenuesProgress(true);
        verify(mMockMvpView).showVenues(venues, checkIn.venue.id);
        verify(mMockMvpView).showVenuesProgress(false);
    }

    @Test
    public void loadVenuesFail() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getDataManager())
                .getVenues();

        mPresenter.loadVenues();
        verify(mMockMvpView).showVenuesProgress(true);
        verify(mMockMvpView, never()).showVenues(venues, null);
        verify(mMockMvpView).showVenuesProgress(false);
    }

    @Test
    public void checkInAtVenueSuccessful() {
        Venue venue = MockModelFabric.newVenue();
        CheckIn checkIn = MockModelFabric.newCheckInWithVenue();
        checkIn.venue = venue;
        CheckInRequest request = CheckInRequest.fromVenue(venue.id);
        doReturn(Observable.just(checkIn))
                .when(component.getDataManager())
                .checkIn(request);

        mPresenter.checkInAtVenue(venue);
        verify(mMockMvpView).showCheckInAtVenueProgress(true, venue.id);
        verify(mMockMvpView).showCheckInAtVenueSuccessful(venue);
        verify(mMockMvpView).showCheckInAtVenueProgress(false, venue.id);
    }

    @Test
    public void checkInAtVenueFail() {
        Venue venue = MockModelFabric.newVenue();
        CheckInRequest request = CheckInRequest.fromVenue(venue.id);
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getDataManager())
                .checkIn(request);

        mPresenter.checkInAtVenue(venue);
        verify(mMockMvpView).showCheckInAtVenueProgress(true, venue.id);
        String expectedError = component.getApplication().getString(R.string.manual_check_in_error);
        verify(mMockMvpView).showCheckInFailed(expectedError);
        verify(mMockMvpView).showCheckInAtVenueProgress(false, venue.id);
    }

    @Test
    public void checkInSuccessful() {
        String locationName = MockModelFabric.randomString();
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        checkIn.label = locationName;
        CheckInRequest request = CheckInRequest.fromLabel(locationName);
        doReturn(Observable.just(checkIn))
                .when(component.getDataManager())
                .checkIn(request);

        mPresenter.checkIn(locationName);
        verify(mMockMvpView).showCheckInButton(false);
        verify(mMockMvpView).showCheckInProgress(true);
        verify(mMockMvpView).showCheckInSuccessful(locationName);
        verify(mMockMvpView).showCheckInProgress(false);
    }

    @Test
    public void checkInFail() {
        String locationName = MockModelFabric.randomString();
        CheckInRequest request = CheckInRequest.fromLabel(locationName);
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getDataManager())
                .checkIn(request);

        mPresenter.checkIn(locationName);
        verify(mMockMvpView).showCheckInButton(false);
        verify(mMockMvpView).showCheckInProgress(true);
        String expectedError = component.getApplication().getString(R.string.manual_check_in_error);
        verify(mMockMvpView).showCheckInFailed(expectedError);
        verify(mMockMvpView).showCheckInProgress(false);
        verify(mMockMvpView).showCheckInButton(true);
    }

    @Test
    public void loadTodayLatestCheckInWithLabel() {
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        component.getPreferencesHelper().putLatestCheckIn(checkIn);

        mPresenter.loadTodayLatestCheckInWithLabel();
        verify(mMockMvpView).showTodayLatestCheckInWithLabel(checkIn.label);
    }

    @Test
    public void loadTodayLatestCheckInWithLabelWhenNoneSaved() {
        mPresenter.loadTodayLatestCheckInWithLabel();
        verify(mMockMvpView, never()).showTodayLatestCheckInWithLabel(anyString());
    }
}
