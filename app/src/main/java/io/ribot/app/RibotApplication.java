package io.ribot.app;

import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import io.ribot.app.data.BusEvent;
import io.ribot.app.data.DataManager;
import io.ribot.app.injection.component.ApplicationComponent;
import io.ribot.app.injection.component.DaggerApplicationComponent;
import io.ribot.app.injection.module.ApplicationModule;
import io.ribot.app.ui.signin.SignInActivity;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RibotApplication extends Application {

    @Inject Bus mEventBus;
    @Inject DataManager mDataManager;
    ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());

        mApplicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();
        mApplicationComponent.inject(this);
        mEventBus.register(this);
    }

    public static RibotApplication get(Context context) {
        return (RibotApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        return mApplicationComponent;
    }

    // Needed to replace the component with a test specific one
    public void setComponent(ApplicationComponent applicationComponent) {
        mApplicationComponent = applicationComponent;
    }

    @Subscribe
    public void onAuthenticationError(BusEvent.AuthenticationError event) {
        mDataManager.signOut()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<Void>() {
                    @Override
                    public void call(Void aVoid) {
                        startSignInActivity();
                    }
                });
    }

    private void startSignInActivity() {
        startActivity(SignInActivity.getStartIntent(
                this, true, getString(R.string.authentication_message)));
    }
}

