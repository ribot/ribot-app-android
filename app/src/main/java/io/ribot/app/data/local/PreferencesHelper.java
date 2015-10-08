package io.ribot.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.util.List;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import rx.Observable;
import rx.Subscriber;

public class PreferencesHelper {

    private SharedPreferences mPref;
    private Gson mGson;

    public static final String PREF_FILE_NAME = "ribot_app_pref_file";

    private static final String PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN";
    private static final String PREF_KEY_SIGNED_IN_RIBOT = "PREF_KEY_SIGNED_IN_RIBOT";
    private static final String PREF_KEY_VENUES = "PREF_KEY_VENUES";
    private static final String PREF_KEY_LATEST_CHECK_IN = "PREF_KEY_LATEST_CHECK_IN";


    public PreferencesHelper(Context context) {
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

}
