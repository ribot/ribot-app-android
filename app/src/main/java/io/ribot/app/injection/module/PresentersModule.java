package io.ribot.app.injection.module;

import dagger.Module;
import dagger.Provides;
import io.ribot.app.injection.scope.PerActivity;
import io.ribot.app.ui.checkin.CheckInPresenter;
import io.ribot.app.ui.main.MainPresenter;
import io.ribot.app.ui.signin.SignInPresenter;

/**
 * Provides extensions of Presenter generally to activities
 */
@Module
public class PresentersModule {

    @Provides
    @PerActivity
    SignInPresenter provideSignInPresenter() {
        return new SignInPresenter();
    }

    @Provides
    @PerActivity
    MainPresenter providesMainPresenter() {
        return new MainPresenter();
    }

    @Provides
    @PerActivity
    CheckInPresenter providesCheckInPresenter() {
        return new CheckInPresenter();
    }

}
