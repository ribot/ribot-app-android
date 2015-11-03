package io.ribot.app.data.local;


import android.content.ContentValues;
import android.database.Cursor;

import io.ribot.app.data.model.RegisteredBeacon;

public class Db {

    public Db() { }

    public static final class BeaconTable {
        public static final String TABLE_NAME = "beacon";

        public static final String COLUMN_ID = "id";
        public static final String COLUMN_UUID = "uuid";
        public static final String COLUMN_MAJOR = "major";
        public static final String COLUMN_MINOR = "minor";

        public static final String CREATE =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        COLUMN_ID + " TEXT PRIMARY KEY," +
                        COLUMN_UUID + " TEXT NOT NULL," +
                        COLUMN_MAJOR + " INTEGER NOT NULL," +
                        COLUMN_MINOR + " INTEGER NOT NULL" +
                        " );";

        public static ContentValues toContentValues(RegisteredBeacon beacon) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_ID, beacon.id);
            values.put(COLUMN_UUID, beacon.uuid);
            values.put(COLUMN_MAJOR, beacon.major);
            values.put(COLUMN_MINOR, beacon.minor);
            return values;
        }

        public static RegisteredBeacon parseCursor(Cursor cursor) {
            RegisteredBeacon beacon = new RegisteredBeacon();
            beacon.id = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ID));
            beacon.uuid = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_UUID));
            beacon.minor = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MINOR));
            beacon.major = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_MAJOR));
            return beacon;
        }
    }

}
