package io.ribot.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.RegisteredBeacon;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import io.ribot.app.injection.ApplicationContext;
import rx.Observable;
import rx.Subscriber;

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "ribot_app_pref_file";

    private static final String PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN";
    private static final String PREF_KEY_SIGNED_IN_RIBOT = "PREF_KEY_SIGNED_IN_RIBOT";
    private static final String PREF_KEY_VENUES = "PREF_KEY_VENUES";
    private static final String PREF_KEY_LATEST_CHECK_IN = "PREF_KEY_LATEST_CHECK_IN";
    private static final String PREF_KEY_LATEST_ENCOUNTER_DATE = "PREF_KEY_LATEST_ENCOUNTER_DATE";
    private static final String PREF_KEY_LATEST_ENCOUNTER_BEACON =
            "PREF_KEY_LATEST_ENCOUNTER_BEACON";
    private static final String PREF_KEY_LATEST_ENCOUNTER_CHECK_IN_ID =
            "PREF_KEY_LATEST_ENCOUNTER_CHECK_IN_ID";

    private final SharedPreferences mPref;
    private final Gson mGson;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mGson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSz")
                .create();
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

    public void putAccessToken(String accessToken) {
        mPref.edit().putString(PREF_KEY_ACCESS_TOKEN, accessToken).apply();
    }

    @Nullable
    public String getAccessToken() {
        return mPref.getString(PREF_KEY_ACCESS_TOKEN, null);
    }

    public void putSignedInRibot(Ribot ribot) {
        mPref.edit().putString(PREF_KEY_SIGNED_IN_RIBOT, mGson.toJson(ribot)).apply();
    }

    @Nullable
    public Ribot getSignedInRibot() {
        String ribotJson = mPref.getString(PREF_KEY_SIGNED_IN_RIBOT, null);
        if (ribotJson == null) return null;
        return mGson.fromJson(ribotJson, Ribot.class);
    }

    public void putVenues(List<Venue> venues) {
        mPref.edit().putString(PREF_KEY_VENUES, mGson.toJson(venues)).apply();
    }

    @Nullable
    public List<Venue> getVenues() {
        String venuesJson = mPref.getString(PREF_KEY_VENUES, null);
        if (venuesJson != null) {
            return mGson.fromJson(venuesJson, new TypeToken<List<Venue>>() {
            }.getType());
        }
        return null;
    }

    public Observable<List<Venue>> getVenuesAsObservable() {
        return Observable.create(new Observable.OnSubscribe<List<Venue>>() {
            @Override
            public void call(Subscriber<? super List<Venue>> subscriber) {
                List<Venue> venues = getVenues();
                if (venues != null) {
                    subscriber.onNext(venues);
                }
                subscriber.onCompleted();
            }
        });
    }

    public void putLatestCheckIn(CheckIn checkIn) {
        mPref.edit().putString(PREF_KEY_LATEST_CHECK_IN, mGson.toJson(checkIn)).apply();
    }

    @Nullable
    public CheckIn getLatestCheckIn() {
        String checkInJson = mPref.getString(PREF_KEY_LATEST_CHECK_IN, null);
        if (checkInJson != null) {
            return mGson.fromJson(checkInJson, CheckIn.class);
        }
        return null;
    }

    public Observable<CheckIn> getLatestCheckInAsObservable() {
        return Observable.create(new Observable.OnSubscribe<CheckIn>() {
            @Override
            public void call(Subscriber<? super CheckIn> subscriber) {
                CheckIn checkIn = getLatestCheckIn();
                if (checkIn != null) {
                    subscriber.onNext(checkIn);
                }
                subscriber.onCompleted();
            }
        });
    }

    /**
     * Return the RegisteredBeacon object related to the latest successful encounter
     * or null if no encounter has been performed yet on this device.
     */
    @Nullable
    public RegisteredBeacon getLatestEncounterBeacon() {
        String beaconJson = mPref.getString(PREF_KEY_LATEST_ENCOUNTER_BEACON, null);
        if (beaconJson != null) {
            return mGson.fromJson(beaconJson, RegisteredBeacon.class);
        }
        return null;
    }

    /**
     * Return the date of the latest successful encounter
     * or null if no encounter has been performed yet on this device.
     */
    @Nullable
    public Date getLatestEncounterDate() {
        long time = mPref.getLong(PREF_KEY_LATEST_ENCOUNTER_DATE, -1);
        if (time > 0) {
            return new Date(time);
        }
        return null;
    }

    /**
     * Return the check-in ID of the latest encounter.
     */
    @Nullable
    public String getLatestEncounterCheckInId() {
        return mPref.getString(PREF_KEY_LATEST_ENCOUNTER_CHECK_IN_ID, null);
    }

    /**
     * Save an Encounter object
     * This overrides the encounter beacon, encounter date and latest check-in.
     */
    public void putLatestEncounter(Encounter encounter) {
        SharedPreferences.Editor editor = mPref.edit();
        editor.putString(PREF_KEY_LATEST_CHECK_IN, mGson.toJson(encounter.checkIn));
        editor.putString(PREF_KEY_LATEST_ENCOUNTER_CHECK_IN_ID, encounter.checkIn.id);
        editor.putString(PREF_KEY_LATEST_ENCOUNTER_BEACON, mGson.toJson(encounter.beacon));
        editor.putLong(PREF_KEY_LATEST_ENCOUNTER_DATE, encounter.encounterDate.getTime());
        editor.apply();
    }

    public void clearLatestEncounter() {
        SharedPreferences.Editor editor = mPref.edit();
        editor.remove(PREF_KEY_LATEST_ENCOUNTER_CHECK_IN_ID);
        editor.remove(PREF_KEY_LATEST_ENCOUNTER_BEACON);
        editor.remove(PREF_KEY_LATEST_ENCOUNTER_DATE);
        editor.apply();
    }

}
