package io.ribot.app.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;

/**
 * Starts auto-check in service on boot completed if user is signed in.
 */
public class BootCompletedReceiver extends BroadcastReceiver {

    @Inject DataManager mDataManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        RibotApplication.get(context).getComponent().inject(this);
        if (mDataManager.getPreferencesHelper().getAccessToken() != null) {
            context.startService(AutoCheckInService.getStartIntent(context));
        }
    }

}
