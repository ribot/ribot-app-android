package io.ribot.app.data.local;

import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.ribot.app.data.model.RegisteredBeacon;
import rx.Observable;
import rx.functions.Func0;

@Singleton
public class DatabaseHelper {

    private final BriteDatabase mDb;

    @Inject
    public DatabaseHelper(DbOpenHelper dbOpenHelper) {
        mDb = SqlBrite.create().wrapDatabaseHelper(dbOpenHelper);
    }

    public BriteDatabase getBriteDb() {
        return mDb;
    }

    /**
     * Remove all the data from all the tables in the database.
     */
    public Observable<Void> clearTables() {

        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    Cursor cursor = mDb.query("SELECT name FROM sqlite_master WHERE type='table'");
                    while (cursor.moveToNext()) {
                        mDb.delete(cursor.getString(cursor.getColumnIndex("name")), null);
                    }
                    cursor.close();
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                }
                return null;
            }
        });
    }

    // Delete all beacons in table and add the new ones.
    public Observable<Void> setRegisteredBeacons(final List<RegisteredBeacon> beacons) {

        return Observable.fromCallable(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    mDb.delete(Db.BeaconTable.TABLE_NAME, null);
                    for (RegisteredBeacon beacon : beacons) {
                        mDb.insert(Db.BeaconTable.TABLE_NAME,
                                Db.BeaconTable.toContentValues(beacon));
                    }
                    transaction.markSuccessful();
                } finally {
                    transaction.end();
                }
                return null;
            }
        });
    }

    public Observable<RegisteredBeacon> findRegisteredBeacon(final String uuid, final int major,
                                                             final int minor) {

        return Observable.defer(new Func0<Observable<RegisteredBeacon>>() {
            @Override
            public Observable<RegisteredBeacon> call() {
                Cursor cursor = mDb.query(
                        "SELECT * FROM " + Db.BeaconTable.TABLE_NAME +
                                " WHERE " + Db.BeaconTable.COLUMN_UUID + " = ? AND " +
                                Db.BeaconTable.COLUMN_MAJOR + "= ? AND " +
                                Db.BeaconTable.COLUMN_MINOR + "= ?",
                        uuid, String.valueOf(major), String.valueOf(minor));
                List<RegisteredBeacon> registeredBeacons = new ArrayList<>();
                while (cursor.moveToNext()) {
                    registeredBeacons.add(Db.BeaconTable.parseCursor(cursor));
                }
                cursor.close();
                return Observable.from(registeredBeacons);
            }
        });

    }

    public Observable<String> findRegisteredBeaconsUuids() {

        return Observable.defer(new Func0<Observable<String>>() {
            @Override
            public Observable<String> call() {
                Cursor cursor = mDb.query("SELECT DISTINCT " + Db.BeaconTable.COLUMN_UUID +
                        " FROM " + Db.BeaconTable.TABLE_NAME);
                List<String> Uuids = new ArrayList<>();

                while (cursor.moveToNext()) {
                    Uuids.add(cursor.getString(
                            cursor.getColumnIndexOrThrow(Db.BeaconTable.COLUMN_UUID)));
                }
                cursor.close();
                return Observable.from(Uuids);
            }
        });
    }

}
