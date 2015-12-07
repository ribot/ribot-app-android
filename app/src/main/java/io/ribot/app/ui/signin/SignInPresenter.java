package io.ribot.app.ui.signin;

import android.accounts.Account;
import android.content.Intent;

import com.google.android.gms.auth.UserRecoverableAuthException;

import javax.inject.Inject;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.Presenter;
import io.ribot.app.util.NetworkUtil;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class SignInPresenter implements Presenter<SignInMvpView> {

    private final DataManager mDataManager;
    private SignInMvpView mMvpView;
    private Subscription mSubscription;

    @Inject
    public SignInPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(SignInMvpView mvpView) {
        this.mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void signInWithGoogle(final Account account) {
        Timber.i("Starting sign in with account " + account.name);
        mMvpView.showProgress(true);
        mMvpView.setSignInButtonEnabled(false);
        mSubscription = mDataManager.signIn(account)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<Ribot>() {
                    @Override
                    public void onCompleted() {
                        mMvpView.showProgress(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        mMvpView.showProgress(false);
                        Timber.w("Sign in has called onError" + e);
                        if (e instanceof UserRecoverableAuthException) {
                            Timber.w("UserRecoverableAuthException has happen");
                            Intent recover = ((UserRecoverableAuthException) e).getIntent();
                            mMvpView.onUserRecoverableAuthException(recover);
                        } else {
                            mMvpView.setSignInButtonEnabled(true);
                            if (NetworkUtil.isHttpStatusCode(e, 403)) {
                                // Google Auth was successful, but the user does not have a ribot
                                // profile set up.
                                mMvpView.showProfileNotFoundError(account.name);
                            } else {
                                mMvpView.showGeneralSignInError();
                            }
                        }
                    }

                    @Override
                    public void onNext(Ribot ribot) {
                        Timber.i("Sign in successful. Profile name: " + ribot.profile.name.first);
                        mMvpView.onSignInSuccessful(ribot.profile);
                    }
                });
    }

}
