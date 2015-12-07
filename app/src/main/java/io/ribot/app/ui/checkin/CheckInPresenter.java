package io.ribot.app.ui.checkin;

import android.support.annotation.Nullable;

import java.util.List;

import javax.inject.Inject;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Venue;
import io.ribot.app.ui.base.BasePresenter;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CheckInPresenter extends BasePresenter<CheckInMvpView> {

    private final CompositeSubscription mSubscriptions;
    private final DataManager mDataManager;

    @Inject
    public CheckInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mSubscriptions = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mSubscriptions.unsubscribe();
    }

    public void loadVenues() {
        getMvpView().showVenuesProgress(true);
        mSubscriptions.add(Observable.combineLatest(
                getTodayLatestCheckInAtVenue().defaultIfEmpty(null), mDataManager.getVenues(),
                new Func2<CheckIn, List<Venue>, VenuesInfo>() {
                    @Override
                    public VenuesInfo call(CheckIn checkIn, List<Venue> venues) {
                        return new VenuesInfo(checkIn, venues);
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<VenuesInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Error loading venues " + e);
                        getMvpView().showVenuesProgress(false);
                    }

                    @Override
                    public void onNext(VenuesInfo venuesInfo) {
                        getMvpView().showVenues(
                                venuesInfo.listVenues,
                                venuesInfo.getTodayLatestCheckInAtVenueId());
                        getMvpView().showVenuesProgress(false);
                    }
                }));
    }

    public void checkIn(String locationName) {
        getMvpView().showCheckInButton(false);
        doCheckIn(CheckInRequest.fromLabel(locationName.trim()));
    }

    public void checkInAtVenue(Venue venue) {
        doCheckIn(CheckInRequest.fromVenue(venue.id));
    }

    private void doCheckIn(final CheckInRequest checkInRequest) {
        showCheckInProgress(true, checkInRequest);
        mSubscriptions.add(mDataManager.checkIn(checkInRequest)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<CheckIn>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Error checking in manually " + e);
                        showCheckInProgress(false, checkInRequest);
                        getMvpView().showCheckInFailed();
                        // if it's a label (typing) request, we make sure we enable the button again
                        if (checkInRequest.getLabel() != null) {
                            getMvpView().showCheckInButton(true);
                        }
                    }

                    @Override
                    public void onNext(CheckIn checkIn) {
                        Timber.i("Manual check in successful at " + checkIn.getLocationName());
                        showCheckInProgress(false, checkInRequest);
                        showCheckInSuccessful(checkIn);
                    }
                }));
    }

    public void loadTodayLatestCheckInWithLabel() {
        mSubscriptions.add(mDataManager.getTodayLatestCheckIn()
                .filter(new Func1<CheckIn, Boolean>() {
                    @Override
                    public Boolean call(CheckIn checkIn) {
                        return checkIn.venue == null && checkIn.label != null;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<CheckIn>() {
                    @Override
                    public void call(CheckIn checkIn) {
                        getMvpView().showTodayLatestCheckInWithLabel(checkIn.label);
                    }
                }));
    }

    // Retrieve today's latest check-ins, discarding the ones that are not at a venue.
    private Observable<CheckIn> getTodayLatestCheckInAtVenue() {
        return mDataManager.getTodayLatestCheckIn()
                .filter(new Func1<CheckIn, Boolean>() {
                    @Override
                    public Boolean call(CheckIn checkIn) {
                        return checkIn.venue != null && checkIn.venue.id != null;
                    }
                });
    }

    private void showCheckInProgress(boolean show, CheckInRequest checkInRequest) {
        if (checkInRequest.getVenueId() != null) {
            getMvpView().showCheckInAtVenueProgress(show, checkInRequest.getVenueId());
        } else {
            getMvpView().showCheckInProgress(show);
        }
    }

    private void showCheckInSuccessful(CheckIn checkIn) {
        if (checkIn.hasVenue()) {
            getMvpView().showCheckInAtVenueSuccessful(checkIn.venue);
        } else {
            getMvpView().showCheckInSuccessful(checkIn.label);
        }
    }

    private final class VenuesInfo {
        CheckIn todayLatestCheckInAtVenue;
        List<Venue> listVenues;

        public VenuesInfo(CheckIn todayLatestCheckInAtVenue, List<Venue> listVenues) {
            this.todayLatestCheckInAtVenue = todayLatestCheckInAtVenue;
            this.listVenues = listVenues;
        }

        @Nullable
        private String getTodayLatestCheckInAtVenueId() {
            if (todayLatestCheckInAtVenue != null) {
                return todayLatestCheckInAtVenue.venue.id;
            }
            return null;
        }
    }
}
