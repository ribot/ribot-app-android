package io.ribot.app.data.local;

import android.content.Context;
import android.database.Cursor;

import com.squareup.sqlbrite.BriteDatabase;
import com.squareup.sqlbrite.SqlBrite;

import java.util.List;

import io.ribot.app.data.model.RegisteredBeacon;
import rx.Observable;
import rx.Subscriber;

public class DatabaseHelper {

    private BriteDatabase mDb;

    public DatabaseHelper(Context context) {
        mDb = SqlBrite.create().wrapDatabaseHelper(new DbOpenHelper(context));
    }

    public BriteDatabase getBriteDb() {
        return mDb;
    }

    /**
     * Remove all the data from all the tables in the database.
     */
    public Observable<Void> clearTables() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    Cursor cursor = mDb.query("SELECT name FROM sqlite_master WHERE type='table'");
                    while (cursor.moveToNext()) {
                        mDb.delete(cursor.getString(cursor.getColumnIndex("name")), null);
                    }
                    cursor.close();
                    transaction.markSuccessful();
                    subscriber.onCompleted();
                } finally {
                    transaction.end();
                }
            }
        });
    }

    // Delete all beacons in table and add the new ones.
    public Observable<Void> setRegisteredBeacons(final List<RegisteredBeacon> beacons) {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                BriteDatabase.Transaction transaction = mDb.newTransaction();
                try {
                    mDb.delete(Db.BeaconTable.TABLE_NAME, null);
                    for (RegisteredBeacon beacon : beacons) {
                        mDb.insert(Db.BeaconTable.TABLE_NAME,
                                Db.BeaconTable.toContentValues(beacon));
                    }
                    transaction.markSuccessful();
                    subscriber.onCompleted();
                } finally {
                    transaction.end();
                }
            }
        });
    }

    public Observable<RegisteredBeacon> findRegisteredBeacon(final String uuid, final int major,
                                                             final int minor) {
        return Observable.create(new Observable.OnSubscribe<RegisteredBeacon>() {
            @Override
            public void call(Subscriber<? super RegisteredBeacon> subscriber) {
                Cursor cursor = mDb.query(
                        "SELECT * FROM " + Db.BeaconTable.TABLE_NAME +
                                " WHERE " + Db.BeaconTable.COLUMN_UUID + " = ? AND " +
                                Db.BeaconTable.COLUMN_MAJOR + "= ? AND " +
                                Db.BeaconTable.COLUMN_MINOR + "= ?",
                        uuid, String.valueOf(major), String.valueOf(minor));
                while (cursor.moveToNext()) {
                    subscriber.onNext(Db.BeaconTable.parseCursor(cursor));
                }
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

    public Observable<String> findRegisteredBeaconsUuids() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                Cursor cursor = mDb.query("SELECT DISTINCT " + Db.BeaconTable.COLUMN_UUID +
                                " FROM " + Db.BeaconTable.TABLE_NAME);
                while (cursor.moveToNext()) {
                    subscriber.onNext(cursor.getString(
                            cursor.getColumnIndexOrThrow(Db.BeaconTable.COLUMN_UUID)));
                }
                cursor.close();
                subscriber.onCompleted();
            }
        });
    }

}
