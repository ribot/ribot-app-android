package io.ribot.app.data.local;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesHelper {

    private SharedPreferences mPref;

    public static final String PREF_FILE_NAME = "ribot_app_pref_file";


    public PreferencesHelper(Context context) {
        mPref = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
    }

    public void clear() {
        mPref.edit().clear().apply();
    }

}
