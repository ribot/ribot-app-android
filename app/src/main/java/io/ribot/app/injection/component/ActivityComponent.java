package io.ribot.app.injection.component;

import dagger.Component;
import io.ribot.app.injection.PerActivity;
import io.ribot.app.injection.module.ActivityModule;
import io.ribot.app.ui.LauncherActivity;
import io.ribot.app.ui.checkin.CheckInActivity;
import io.ribot.app.ui.main.MainActivity;
import io.ribot.app.ui.signin.SignInActivity;
import io.ribot.app.ui.team.TeamFragment;

@PerActivity
@Component(dependencies = ApplicationComponent.class, modules = ActivityModule.class)
public interface ActivityComponent {

    void inject(SignInActivity signInActivity);
    void inject(LauncherActivity launcherActivity);
    void inject(MainActivity mainActivity);
    void inject(CheckInActivity checkInActivity);

    void inject(TeamFragment teamFragment);
}

