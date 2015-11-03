package io.ribot.app.data;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import java.util.List;

import javax.inject.Inject;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.data.remote.RibotService.SignInRequest;
import io.ribot.app.data.remote.RibotService.SignInResponse;
import io.ribot.app.injection.component.DaggerDataManagerComponent;
import io.ribot.app.injection.module.DataManagerModule;
import io.ribot.app.util.DateUtil;
import rx.Observable;
import rx.Scheduler;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class DataManager {

    @Inject
    protected RibotService mRibotService;
    @Inject
    protected DatabaseHelper mDatabaseHelper;
    @Inject
    protected PreferencesHelper mPreferencesHelper;
    @Inject
    protected Bus mBus;
    @Inject
    protected Scheduler mSubscribeScheduler;
    @Inject
    protected GoogleAuthHelper mGoogleAuthHelper;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    /* This constructor is provided so we can set up a DataManager with mocks from unit test.
     * At the moment this is not possible to do with Dagger because the Gradle APT plugin doesn't
     * work for the unit test variant, plus Dagger 2 doesn't provide a nice way of overriding
     * modules */
    public DataManager(RibotService ribotService,
                       DatabaseHelper databaseHelper,
                       Bus bus,
                       PreferencesHelper preferencesHelper,
                       Scheduler subscribeScheduler) {
        mRibotService = ribotService;
        mDatabaseHelper = databaseHelper;
        mBus = bus;
        mPreferencesHelper = preferencesHelper;
        mSubscribeScheduler = subscribeScheduler;
    }

    protected void injectDependencies(Context context) {
        DaggerDataManagerComponent.builder()
                .applicationComponent(RibotApplication.get(context).getComponent())
                .dataManagerModule(new DataManagerModule(context))
                .build()
                .inject(this);
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Scheduler getSubscribeScheduler() {
        return mSubscribeScheduler;
    }

    /**
     * Sign in with a Google account.
     * 1. Retrieve an google auth code for the given account
     * 2. Sends code and account to API
     * 3. If success, saves ribot profile and API access token in preferences
     */
    public Observable<Ribot> signIn(Context context, Account account) {
        return mGoogleAuthHelper.retrieveAuthTokenAsObservable(context, account)
                .concatMap(new Func1<String, Observable<SignInResponse>>() {
                    @Override
                    public Observable<SignInResponse> call(String googleAccessToken) {
                        return mRibotService.signIn(new SignInRequest(googleAccessToken));
                    }
                })
                .map(new Func1<SignInResponse, Ribot>() {
                    @Override
                    public Ribot call(SignInResponse signInResponse) {
                        mPreferencesHelper.putAccessToken(signInResponse.accessToken);
                        mPreferencesHelper.putSignedInRibot(signInResponse.ribot);
                        return signInResponse.ribot;
                    }
                });
    }

    public Observable<Void> signOut() {
        return mDatabaseHelper.clearTables()
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mPreferencesHelper.clear();
                        postEventSafely(new BusEvent.UserSignedOut());
                    }
                });
    }

    public Observable<List<Ribot>> getRibots() {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.getRibots(auth, "checkins");
    }

    /**
     * Retrieve list of venues. Behaviour:
     * 1. Return cached venues (empty list if none is cached)
     * 2. Return API venues (if different to cached ones)
     * 3. Save new venues from API in cache
     * 5. If an error happens and cache is not empty, returns venues from cache.
     */
    public Observable<List<Venue>> getVenues() {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.getVenues(auth)
                .doOnNext(new Action1<List<Venue>>() {
                    @Override
                    public void call(List<Venue> venues) {
                        mPreferencesHelper.putVenues(venues);
                    }
                })
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends List<Venue>>>() {
                    @Override
                    public Observable<? extends List<Venue>> call(Throwable throwable) {
                        return getVenuesRecoveryObservable(throwable);
                    }
                })
                .startWith(mPreferencesHelper.getVenuesAsObservable())
                .distinct();
    }

    // Returns venues from cache. If cache is empty, it forwards the error.
    private Observable<List<Venue>> getVenuesRecoveryObservable(Throwable error) {
        return mPreferencesHelper.getVenuesAsObservable()
                .switchIfEmpty(Observable.<List<Venue>>error(error));
    }

    /**
     * Performs a manual check in, either at a venue or a location.
     * Use CheckInRequest.fromVenue() or CheckInRequest.fromLabel() to create the request.
     * If the the check-in is successful, it's saved as the latest check-in.
     */
    public Observable<CheckIn> checkIn(CheckInRequest checkInRequest) {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.checkIn(auth, checkInRequest)
                .doOnNext(new Action1<CheckIn>() {
                    @Override
                    public void call(CheckIn checkIn) {
                        mPreferencesHelper.putLatestCheckIn(checkIn);
                    }
                });
    }

    /**
     * Marks a previous check-in as "checkedOut" and updates the value in preferences
     * if the check-in matches the latest check-in.
     */
    public Observable<CheckIn> checkOut(final String checkInId) {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.updateCheckIn(auth, checkInId,
                new RibotService.UpdateCheckInRequest(true))
                .doOnNext(new Action1<CheckIn>() {
                    @Override
                    public void call(CheckIn checkInUpdated) {
                        CheckIn latestCheckIn = mPreferencesHelper.getLatestCheckIn();
                        if (latestCheckIn != null && latestCheckIn.id.equals(checkInUpdated.id)) {
                            mPreferencesHelper.putLatestCheckIn(checkInUpdated);
                        }
                        String encounterCheckInId =
                                mPreferencesHelper.getLatestEncounterCheckInId();
                        if (encounterCheckInId != null &&
                                encounterCheckInId.equals(checkInUpdated.id)) {
                            mPreferencesHelper.clearLatestEncounter();
                        }
                    }
                });
    }

    /**
     * Returns today's latest manual check in, if there is one.
     */
    public Observable<CheckIn> getTodayLatestCheckIn() {
        return mPreferencesHelper.getLatestCheckInAsObservable()
                .filter(new Func1<CheckIn, Boolean>() {
                    @Override
                    public Boolean call(CheckIn checkIn) {
                        return DateUtil.isToday(checkIn.checkedInDate.getTime());
                    }
                });
    }

    public Observable<Encounter> performBeaconEncounter(String beaconId) {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.performBeaconEncounter(auth, beaconId)
                .doOnNext(new Action1<Encounter>() {
                    @Override
                    public void call(Encounter encounter) {
                        mPreferencesHelper.putLatestEncounter(encounter);
                    }
                });
    }

    public Observable<Encounter> performBeaconEncounter(String uuid, int major, int minor) {
        Observable<RegisteredBeacon> errorObservable = Observable.error(
                new BeaconNotRegisteredException(uuid, major, minor));
        return mDatabaseHelper.findRegisteredBeacon(uuid, major, minor)
                .switchIfEmpty(errorObservable)
                .concatMap(new Func1<RegisteredBeacon, Observable<Encounter>>() {
                    @Override
                    public Observable<Encounter> call(RegisteredBeacon registeredBeacon) {
                        return performBeaconEncounter(registeredBeacon.id);
                    }
                });
    }

    public Observable<String> findRegisteredBeaconsUuids() {
        return mDatabaseHelper.findRegisteredBeaconsUuids();
    }

    public Observable<Void> syncRegisteredBeacons() {
        String auth = RibotService.Util.buildAuthorization(mPreferencesHelper.getAccessToken());
        return mRibotService.getRegisteredBeacons(auth)
                .concatMap(new Func1<List<RegisteredBeacon>, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(List<RegisteredBeacon> beacons) {
                        return mDatabaseHelper.setRegisteredBeacons(beacons);
                    }
                })
                .doOnCompleted(postEventAction(new BusEvent.BeaconsSyncCompleted()));
    }

    /// Helper method to post events from doOnCompleted.
    private Action0 postEventAction(final Object event) {
        return new Action0() {
            @Override
            public void call() {
                postEventSafely(event);
            }
        };
    }

    // Helper method to post an event from a different thread to the main one.
    private void postEventSafely(final Object event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

}
