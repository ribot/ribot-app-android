package io.ribot.app.injection.component;

import android.accounts.AccountManager;
import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.remote.UnauthorisedInterceptor;
import io.ribot.app.injection.module.ApplicationModule;
import io.ribot.app.service.AutoCheckInService;
import io.ribot.app.service.BeaconsSyncService;
import io.ribot.app.ui.checkin.CheckInPresenter;
import io.ribot.app.ui.main.MainPresenter;
import io.ribot.app.ui.signin.SignInPresenter;
import io.ribot.app.ui.team.TeamPresenter;
import rx.subscriptions.CompositeSubscription;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(RibotApplication ribotApplication);
    void inject(SignInPresenter signInPresenter);
    void inject(MainPresenter mainPresenter);
    void inject(CheckInPresenter checkInPresenter);
    void inject(TeamPresenter teamPresenter);
    void inject(UnauthorisedInterceptor unauthorisedInterceptor);
    void inject(AutoCheckInService autoCheckInService);
    void inject(BeaconsSyncService beaconsSyncService);

    Application application();
    DataManager dataManager();
    Bus eventBus();
    AccountManager accountManager();
    CompositeSubscription compositeSubscription();
}
