package io.ribot.app;

import android.database.Cursor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.Db;
import io.ribot.app.data.local.DbOpenHelper;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.util.DefaultConfig;
import rx.observers.TestSubscriber;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class DatabaseHelperTest {

    final DatabaseHelper mDatabaseHelper =
            new DatabaseHelper(new DbOpenHelper(RuntimeEnvironment.application));

    @Before
    public void setUp() {
        mDatabaseHelper.clearTables().subscribe();
    }

    @Test
    public void findRegisteredBeacon() {
        RegisteredBeacon beaconToFind = MockModelFabric.newRegisteredBeacon();
        RegisteredBeacon anotherBeacon = MockModelFabric.newRegisteredBeacon();
        mDatabaseHelper.getBriteDb().insert(Db.BeaconTable.TABLE_NAME,
                Db.BeaconTable.toContentValues(beaconToFind));
        mDatabaseHelper.getBriteDb().insert(Db.BeaconTable.TABLE_NAME,
                Db.BeaconTable.toContentValues(anotherBeacon));

        TestSubscriber<RegisteredBeacon> testSubscriber = new TestSubscriber<>();
        mDatabaseHelper.findRegisteredBeacon(beaconToFind.uuid,
                beaconToFind.major, beaconToFind.minor)
                .subscribe(testSubscriber);

        //zone is not saved in DB so we won't expect it to be returned
        beaconToFind.zone = null;
        testSubscriber.assertNoErrors();
        testSubscriber.assertValue(beaconToFind);
        testSubscriber.assertCompleted();
    }

    @Test
    public void setRegisteredBeacons() {
        List<RegisteredBeacon> beacons = MockModelFabric.newRegisteredBeaconList(3);
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDatabaseHelper.setRegisteredBeacons(beacons).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();

        checkBeaconsSavedSuccessfully(beacons);
    }

    @Test
    public void setRegisteredBeaconsDeletesPreviousData() {
        List<RegisteredBeacon> existingBeacons = MockModelFabric.newRegisteredBeaconList(10);
        mDatabaseHelper.setRegisteredBeacons(existingBeacons).subscribe();

        List<RegisteredBeacon> newBeacons = MockModelFabric.newRegisteredBeaconList(5);
        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDatabaseHelper.setRegisteredBeacons(newBeacons).subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();

        checkBeaconsSavedSuccessfully(newBeacons);
    }

    @Test
    public void setRegisteredBeaconsWithEmptyListClearsData() {
        List<RegisteredBeacon> existingBeacons = MockModelFabric.newRegisteredBeaconList(10);
        mDatabaseHelper.setRegisteredBeacons(existingBeacons).subscribe();

        TestSubscriber<Void> testSubscriber = new TestSubscriber<>();
        mDatabaseHelper.setRegisteredBeacons(new ArrayList<RegisteredBeacon>())
                .subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertNoValues();
        testSubscriber.assertCompleted();

        Cursor cursor = mDatabaseHelper.getBriteDb()
                .query("SELECT * FROM " + Db.BeaconTable.TABLE_NAME);
        assertEquals(0, cursor.getCount());
    }

    @Test
    public void findRegisteredBeaconsUuids() {
        RegisteredBeacon beacon1 = MockModelFabric.newRegisteredBeacon();
        RegisteredBeacon beacon2 = MockModelFabric.newRegisteredBeacon();
        RegisteredBeacon beacon3 = MockModelFabric.newRegisteredBeacon();
        beacon3.uuid = beacon1.uuid;
        mDatabaseHelper.setRegisteredBeacons(Arrays.asList(beacon1, beacon2, beacon3)).subscribe();

        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        mDatabaseHelper.findRegisteredBeaconsUuids().subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        // beacon3 has the same uuid as beacon2 so it shouldn't be returned twice
        testSubscriber.assertReceivedOnNext(Arrays.asList(beacon1.uuid, beacon2.uuid));
        testSubscriber.assertCompleted();
    }

    private void checkBeaconsSavedSuccessfully(List<RegisteredBeacon> expectedBeacons) {
        Cursor cursor = mDatabaseHelper.getBriteDb()
                .query("SELECT * FROM " + Db.BeaconTable.TABLE_NAME);
        assertEquals(expectedBeacons.size(), cursor.getCount());
        for (RegisteredBeacon beacon : expectedBeacons) {
            cursor.moveToNext();
            //zone is not saved in DB so we won't expect it to be returned
            beacon.zone = null;
            assertEquals(beacon, Db.BeaconTable.parseCursor(cursor));
        }
        cursor.close();
    }

}
