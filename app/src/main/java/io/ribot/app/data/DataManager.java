package io.ribot.app.data;

import android.accounts.Account;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.squareup.otto.Bus;

import javax.inject.Inject;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.data.remote.RibotService.SignInRequest;
import io.ribot.app.data.remote.RibotService.SignInResponse;
import io.ribot.app.injection.component.DaggerDataManagerComponent;
import io.ribot.app.injection.module.DataManagerModule;
import rx.Observable;
import rx.Scheduler;
import rx.Subscriber;
import rx.functions.Func1;

public class DataManager {

    @Inject
    protected RibotService mRibotService;
    @Inject
    protected DatabaseHelper mDatabaseHelper;
    @Inject
    protected PreferencesHelper mPreferencesHelper;
    @Inject
    protected Bus mBus;
    @Inject
    protected Scheduler mSubscribeScheduler;
    @Inject
    protected GoogleAuthHelper mGoogleAuthHelper;

    public DataManager(Context context) {
        injectDependencies(context);
    }

    /* This constructor is provided so we can set up a DataManager with mocks from unit test.
     * At the moment this is not possible to do with Dagger because the Gradle APT plugin doesn't
     * work for the unit test variant, plus Dagger 2 doesn't provide a nice way of overriding
     * modules */
    public DataManager(RibotService ribotService,
                       DatabaseHelper databaseHelper,
                       Bus bus,
                       PreferencesHelper preferencesHelper,
                       Scheduler subscribeScheduler) {
        mRibotService = ribotService;
        mDatabaseHelper = databaseHelper;
        mBus = bus;
        mPreferencesHelper = preferencesHelper;
        mSubscribeScheduler = subscribeScheduler;
    }

    protected void injectDependencies(Context context) {
        DaggerDataManagerComponent.builder()
                .applicationComponent(RibotApplication.get(context).getComponent())
                .dataManagerModule(new DataManagerModule(context))
                .build()
                .inject(this);
    }

    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    public Scheduler getSubscribeScheduler() {
        return mSubscribeScheduler;
    }

    public Observable<Ribot> signIn(Context context, Account account) {
        return mGoogleAuthHelper.retrieveAuthTokenAsObservable(context, account)
                .concatMap(new Func1<String, Observable<SignInResponse>>() {
                    @Override
                    public Observable<SignInResponse> call(String googleAccessToken) {
                        return mRibotService.signIn(new SignInRequest(googleAccessToken));
                    }
                })
                .map(new Func1<SignInResponse, Ribot>() {
                    @Override
                    public Ribot call(SignInResponse signInResponse) {
                        mPreferencesHelper.putAccessToken(signInResponse.accessToken);
                        mPreferencesHelper.putSignedInRibot(signInResponse.ribot);
                        return signInResponse.ribot;
                    }
                });
    }

    public Observable<Void> signOut() {
        return Observable.create(new Observable.OnSubscribe<Void>() {
            @Override
            public void call(Subscriber<? super Void> subscriber) {
                mPreferencesHelper.clear();
                subscriber.onCompleted();
            }
        });
        // TODO clear database if we use one
    }


    // Helper method to post an event from a different thread to the main one.
    private void postEventSafely(final Object event) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                mBus.post(event);
            }
        });
    }

}
