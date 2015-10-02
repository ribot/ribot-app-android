package io.ribot.app.ui.main;

import javax.inject.Inject;

import io.ribot.app.R;
import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class MainPresenter implements Presenter<MainMvpView> {

    @Inject
    protected DataManager mDataManager;
    private MainMvpView mMvpView;
    private Subscription mSubscription;

    @Override
    public void attachView(MainMvpView mvpView) {
        mMvpView = mvpView;
        RibotApplication.get(mMvpView.getContext()).getComponent().inject(this);

        Ribot signedInRibot = mDataManager.getPreferencesHelper().getSignedInRibot();
        mMvpView.showWelcomeMessage(mMvpView.getContext().getString(R.string.signed_in_welcome,
                signedInRibot.profile.name.first));
    }

    @Override
    public void detachView() {
        mMvpView = null;
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void signOut() {
        mSubscription = mDataManager.signOut()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getSubscribeScheduler())
                .subscribe(new Subscriber<Void>() {
                    @Override
                    public void onCompleted() {
                        Timber.i("Sign out successful!");
                        mMvpView.onSignedOut();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("Error signing out: " + e);
                    }

                    @Override
                    public void onNext(Void aVoid) {

                    }
                });
    }
}
