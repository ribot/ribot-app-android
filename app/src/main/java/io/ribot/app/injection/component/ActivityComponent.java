package io.ribot.app.injection.component;

import dagger.Component;
import io.ribot.app.injection.module.PresentersModule;
import io.ribot.app.injection.scope.PerActivity;
import io.ribot.app.ui.LauncherActivity;
import io.ribot.app.ui.checkin.CheckInActivity;
import io.ribot.app.ui.main.MainActivity;
import io.ribot.app.ui.signin.SignInActivity;

/**
 * This component inject dependencies to all Activities across the application
 */
@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = PresentersModule.class)
public interface ActivityComponent {

    void inject(SignInActivity signInActivity);
    void inject(LauncherActivity launcherActivity);
    void inject(MainActivity mainActivity);
    void inject(CheckInActivity checkInActivity);
}

