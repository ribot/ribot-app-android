package io.ribot.app.injection.component;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.data.remote.UnauthorisedInterceptor;
import io.ribot.app.injection.ApplicationContext;
import io.ribot.app.injection.module.ApplicationModule;
import io.ribot.app.service.AutoCheckInService;
import io.ribot.app.service.BeaconsSyncService;
import io.ribot.app.service.BootCompletedReceiver;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(RibotApplication ribotApplication);
    void inject(UnauthorisedInterceptor unauthorisedInterceptor);
    void inject(AutoCheckInService autoCheckInService);
    void inject(BeaconsSyncService beaconsSyncService);
    void inject(BootCompletedReceiver bootCompletedReceiver);

    @ApplicationContext Context context();
    Application application();
    RibotService ribotService();
    PreferencesHelper preferencesHelper();
    DatabaseHelper databaseHelper();
    DataManager dataManager();
    Bus eventBus();
    AccountManager accountManager();
    GoogleAuthHelper googleAuthHelper();
}
