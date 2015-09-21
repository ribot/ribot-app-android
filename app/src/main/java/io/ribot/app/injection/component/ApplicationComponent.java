package io.ribot.app.injection.component;

import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Component;
import io.ribot.app.data.DataManager;
import io.ribot.app.injection.module.ApplicationModule;
import io.ribot.app.ui.activity.MainActivity;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(MainActivity mainActivity);

    Application application();
    DataManager dataManager();
    Bus eventBus();
}
