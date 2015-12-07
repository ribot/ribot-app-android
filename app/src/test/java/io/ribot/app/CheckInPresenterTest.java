package io.ribot.app;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Venue;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.ui.checkin.CheckInMvpView;
import io.ribot.app.ui.checkin.CheckInPresenter;
import io.ribot.app.util.RxSchedulersOverrideRule;
import rx.Observable;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class CheckInPresenterTest {

    @Mock CheckInMvpView mMockMvpView;
    @Mock DataManager mMockDataManager;
    private CheckInPresenter mPresenter;

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() {
        mPresenter = new CheckInPresenter(mMockDataManager);
        mPresenter.attachView(mMockMvpView);
        // Default stub return empty, some test can override this.
        doReturn(Observable.empty())
                .when(mMockDataManager)
                .getTodayLatestCheckIn();
    }

    @After
    public void detachView() {
        mPresenter.detachView();
    }

    @Test
    public void loadVenuesSuccessful() {
        List<Venue> venues = MockModelFabric.newVenueList(10);
        doReturn(Observable.just(venues))
                .when(mMockDataManager)
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
        doReturn(Observable.just(checkIn))
                .when(mMockDataManager)
                .getTodayLatestCheckIn();
        doReturn(Observable.just(venues))
                .when(mMockDataManager)
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
                .when(mMockDataManager)
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
                .when(mMockDataManager)
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
                .when(mMockDataManager)
                .checkIn(request);

        mPresenter.checkInAtVenue(venue);
        verify(mMockMvpView).showCheckInAtVenueProgress(true, venue.id);
        verify(mMockMvpView).showCheckInFailed();
        verify(mMockMvpView).showCheckInAtVenueProgress(false, venue.id);
    }

    @Test
    public void checkInSuccessful() {
        String locationName = MockModelFabric.randomString();
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        checkIn.label = locationName;
        CheckInRequest request = CheckInRequest.fromLabel(locationName);
        doReturn(Observable.just(checkIn))
                .when(mMockDataManager)
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
                .when(mMockDataManager)
                .checkIn(request);

        mPresenter.checkIn(locationName);
        verify(mMockMvpView).showCheckInButton(false);
        verify(mMockMvpView).showCheckInProgress(true);
        verify(mMockMvpView).showCheckInFailed();
        verify(mMockMvpView).showCheckInProgress(false);
        verify(mMockMvpView).showCheckInButton(true);
    }

    @Test
    public void loadTodayLatestCheckInWithLabel() {
        CheckIn checkIn = MockModelFabric.newCheckInWithLabel();
        doReturn(Observable.just(checkIn))
                .when(mMockDataManager)
                .getTodayLatestCheckIn();

        mPresenter.loadTodayLatestCheckInWithLabel();
        verify(mMockMvpView).showTodayLatestCheckInWithLabel(checkIn.label);
    }

    @Test
    public void loadTodayLatestCheckInWithLabelWhenNoneSaved() {
        mPresenter.loadTodayLatestCheckInWithLabel();
        verify(mMockMvpView, never()).showTodayLatestCheckInWithLabel(anyString());
    }

}
