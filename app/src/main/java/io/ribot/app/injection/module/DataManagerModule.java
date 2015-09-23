package io.ribot.app.injection.module;

import android.content.Context;

import dagger.Module;
import dagger.Provides;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.injection.scope.PerDataManager;
import rx.Scheduler;
import rx.schedulers.Schedulers;

/**
 * Provide dependencies to the DataManager, mainly Helper classes and Retrofit services.
 */
@Module
public class DataManagerModule {

    private final Context mContext;

    public DataManagerModule(Context context) {
        mContext = context;
    }

    @Provides
    @PerDataManager
    DatabaseHelper provideDatabaseHelper() {
        return new DatabaseHelper(mContext);
    }

    @Provides
    @PerDataManager
    PreferencesHelper providePreferencesHelper() {
        return new PreferencesHelper(mContext);
    }

    @Provides
    @PerDataManager
    RibotService provideRibotService() {
        return RibotService.Instance.newRibotService();
    }

    @Provides
    @PerDataManager
    Scheduler provideSubscribeScheduler() {
        return Schedulers.io();
    }

    @Provides
    @PerDataManager
    GoogleAuthHelper providesGoogleAuthHelper() {
        return new GoogleAuthHelper();
    }
}
