package io.ribot.app.injection.component;

import android.accounts.AccountManager;
import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.data.DataManager;
import io.ribot.app.injection.module.ApplicationModule;
import io.ribot.app.ui.main.MainPresenter;
import io.ribot.app.ui.signin.SignInPresenter;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(SignInPresenter signInPresenter);
    void inject(MainPresenter mainPresenter);

    Application application();
    DataManager dataManager();
    Bus eventBus();
    AccountManager accountManager();
}
