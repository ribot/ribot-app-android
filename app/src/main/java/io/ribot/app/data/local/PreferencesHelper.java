package io.ribot.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.ribot.app.data.model.Ribot;

public class PreferencesHelper {

    private SharedPreferences mPref;
    private Gson mGson;

    public static final String PREF_FILE_NAME = "ribot_app_pref_file";

    private static final String PREF_KEY_ACCESS_TOKEN = "PREF_KEY_ACCESS_TOKEN";
    private static final String PREF_KEY_SIGNED_IN_RIBOT = "PREF_KEY_SIGNED_IN_RIBOT";


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

}
