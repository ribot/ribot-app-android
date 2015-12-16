package io.ribot.app.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.BusEvent;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.util.AndroidComponentUtil;
import io.ribot.app.util.DateUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class AutoCheckInService extends Service implements
        BeaconManager.ServiceReadyCallback,
        BeaconManager.MonitoringListener,
        BeaconManager.RangingListener {

    private BeaconManager mBeaconManager;
    private RegisteredBeacon mLatestEncounterBeacon;
    private Date mLatestEncounterDate;
    private String mLatestEncounterCheckInId;
    private Subscription mCheckInSubscription;
    private Subscription mBeaconsUuidSubscription;
    private Set<String> mMonitoredRegionsUuids;

    @Inject Bus mBus;
    @Inject DataManager mDataManager;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, AutoCheckInService.class);
    }

    public static boolean isRunning(Context context) {
        return AndroidComponentUtil.isServiceRunning(context, AutoCheckInService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        RibotApplication.get(this).getComponent().inject(this);
        mBus.register(this);
        mMonitoredRegionsUuids = new HashSet<>();
        mBeaconManager = new BeaconManager(this);
        mBeaconManager.setMonitoringListener(this);
        mBeaconManager.setRangingListener(this);
        mBeaconManager.setForegroundScanPeriod(5000, 150000); // Scan during 5s every 2.5min
        mBeaconManager.setBackgroundScanPeriod(10000, 300000); // Scan during 10s every 5min
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mBeaconManager.connect(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Timber.i("Destroying AutoCheckInService and disconnecting BeaconManager");
        if (mCheckInSubscription != null) mCheckInSubscription.unsubscribe();
        if (mBeaconsUuidSubscription != null) mBeaconsUuidSubscription.unsubscribe();
        mBeaconManager.disconnect();
        mBeaconManager = null;
        mMonitoredRegionsUuids = null;
        mBus.unregister(this);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Subscribe
    public void onBeaconsSyncCompleted(BusEvent.BeaconsSyncCompleted event) {
        Timber.i("Beacons sync completed, refreshing monitoring regions...");
        // Connect will trigger a call to startMonitoringRegisteredBeaconUuids() that will
        // start monitoring any new beacon saved after the sync.
        mBeaconManager.connect(this);
    }

    @Subscribe
    public void onUserSignedOut(BusEvent.UserSignedOut event) {
        stopSelf();
    }

    /******** BeaconManager.ServiceReadyCallback Implementation  ********/

    @Override
    public void onServiceReady() {
        startMonitoringRegisteredBeaconUuids();
    }

    /******** BeaconManager.MonitoringListener Implementation  ********/

    @Override
    public void onEnteredRegion(Region region, List<Beacon> list) {
        Timber.i("Entered region %s. Starting ranging...", region.getIdentifier());
        mBeaconManager.startRanging(region);
    }

    @Override
    public void onExitedRegion(Region region) {
        Timber.i("Exited region %s. Stopping ranging...", region.getIdentifier());
        mBeaconManager.stopRanging(region);

        RegisteredBeacon latestEncounterBeacon = getLatestEncounterBeacon();
        if (latestEncounterBeacon != null &&
                latestEncounterBeacon.uuid.equalsIgnoreCase(region.getProximityUUID().toString())) {
            performCheckOutLatestEncounter();
        }
    }

    /******** BeaconManager.RangingListener Implementation ********/

    @Override
    public void onBeaconsDiscovered(Region region, List<Beacon> list) {
        Timber.i("Beacons discovered in region %s are %d ", region.getIdentifier(), list.size());
        for (Beacon beacon : list) {
            Timber.i(beacon.toString());
        }
        Beacon nearestBeacon = calculateNearestBeacon(list);
        if (nearestBeacon == null) return;

        Timber.i("Nearest beacon is " + nearestBeacon);
        if (isSameAsTodayLatestEncounter(nearestBeacon)) {
            Timber.i("Skipping encounter. Beacon is same as today's latest successful encounter");
        } else {
            performEncounter(nearestBeacon);
        }
    }

    /******** Helper methods ********/

    @Nullable
    private Beacon calculateNearestBeacon(Collection<Beacon> beacons) {
        Beacon nearestBeacon = null;
        Double shortestDistance = null;
        for (Beacon beacon : beacons) {
            double distance = Utils.computeAccuracy(beacon);
            if (nearestBeacon != null) {
                if (distance < shortestDistance) {
                    nearestBeacon = beacon;
                    shortestDistance = distance;
                }
            } else {
                nearestBeacon = beacon;
                shortestDistance = distance;
            }
        }
        return nearestBeacon;
    }

    private void startMonitoringRegisteredBeaconUuids() {
        // Each region we monitor matches a Venue in the API
        // All the beacons within the same Venue (region) have the same UUID an different
        // major/minor.
        if (mBeaconsUuidSubscription != null) mBeaconsUuidSubscription.unsubscribe();
        mBeaconsUuidSubscription = mDataManager.findRegisteredBeaconsUuids()
                .subscribeOn(Schedulers.io())
                        // Filter UUIDs that match regions already being monitored
                .filter(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String uuid) {
                        boolean isAlreadyMonitoring = mMonitoredRegionsUuids.contains(uuid);
                        if (isAlreadyMonitoring) {
                            Timber.i("Skipping region with uuid %s. Already monitoring.", uuid);
                        }
                        return !isAlreadyMonitoring;
                    }
                })
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String uuid) {
                        Timber.i("Starting monitoring region with UUID %s", uuid);
                        Region region = new Region("region-" + uuid, UUID.fromString(uuid),
                                null, null);
                        mBeaconManager.startMonitoring(region);
                        mMonitoredRegionsUuids.add(region.getProximityUUID().toString());
                        // Possibly needs stopping monitoring of regions that not longer exit
                    }
                });
    }

    private void performEncounter(Beacon beacon) {
        Timber.i("Performing encounter...");
        if (mCheckInSubscription != null) mCheckInSubscription.unsubscribe();
        mCheckInSubscription = mDataManager.performBeaconEncounter(
                beacon.getProximityUUID().toString(), beacon.getMajor(), beacon.getMinor())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Encounter>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "Performing encounter failed");
                    }

                    @Override
                    public void onNext(Encounter encounter) {
                        mLatestEncounterBeacon = encounter.beacon;
                        mLatestEncounterDate = encounter.encounterDate;
                        mLatestEncounterCheckInId = encounter.checkIn.id;
                        Timber.i("Encounter performed correctly at %s, %s" +
                                        " for beacon %s, major: %d, minor %d",
                                encounter.beacon.zone.label,
                                encounter.beacon.zone.venue.label,
                                encounter.beacon.uuid,
                                encounter.beacon.major,
                                encounter.beacon.minor);
                    }
                });
    }

    private void performCheckOutLatestEncounter() {
        Timber.i("Checking out...");
        String checkInId = getLatestEncounterCheckInId();
        if (checkInId == null) {
            Timber.e("Cannot check-out because latest encounter check-in ID is null");
            return;
        }
        if (mCheckInSubscription != null) mCheckInSubscription.unsubscribe();
        mCheckInSubscription = mDataManager.checkOut(checkInId)
                .retry(3)
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<CheckIn>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Checked out successfully!");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error checking out");
                    }

                    @Override
                    public void onNext(CheckIn checkIn) {

                    }
                });
    }

    @Nullable
    private RegisteredBeacon getLatestEncounterBeacon() {
        // Lazy loading of latest encounter beacon
        if (mLatestEncounterBeacon == null) {
            mLatestEncounterBeacon = mDataManager.getPreferencesHelper().getLatestEncounterBeacon();
        }
        return mLatestEncounterBeacon;
    }

    @Nullable
    private Date getLatestEncounterDate() {
        // Lazy loading of latest encounter date
        if (mLatestEncounterDate == null) {
            mLatestEncounterDate = mDataManager.getPreferencesHelper().getLatestEncounterDate();
        }
        return mLatestEncounterDate;
    }

    @Nullable
    private String getLatestEncounterCheckInId() {
        // Lazy loading of latest encounter check-in ID
        if (mLatestEncounterCheckInId == null) {
            mLatestEncounterCheckInId = mDataManager.getPreferencesHelper()
                    .getLatestEncounterCheckInId();
        }
        return mLatestEncounterCheckInId;
    }

    private boolean isSameAsTodayLatestEncounter(Beacon beacon) {
        Date latestEncounterDate = getLatestEncounterDate();
        RegisteredBeacon latestEncounterBeacon = getLatestEncounterBeacon();
        if (latestEncounterDate != null && latestEncounterBeacon != null &&
                DateUtil.isToday(latestEncounterDate.getTime())) {
            return latestEncounterBeacon.hasSameUuidMajorMinor(beacon.getProximityUUID().toString(),
                    beacon.getMajor(), beacon.getMinor());
        }
        return false;
    }

}
