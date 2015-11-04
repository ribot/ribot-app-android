package io.ribot.app.ui.team;


import android.content.Context;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.ribot.app.R;
import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.Presenter;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TeamPresenter implements Presenter<TeamMvpView> {

    @Inject
    protected DataManager mDataManager;
    public Subscription mSubscription;
    private TeamMvpView mMvpView;

    @Override
    public void attachView(TeamMvpView mvpView) {
        mMvpView = mvpView;
        RibotApplication.get(mMvpView.getViewContext()).getComponent().inject(this);
    }

    @Override
    public void detachView() {
        mMvpView = null;
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void loadRibots() {
        mMvpView.showRibotProgress(true);
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = mDataManager.getRibots()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(mDataManager.getSubscribeScheduler())
                // Workaround for Retrofit https://github.com/square/retrofit/issues/1069
                // Can removed once issue fixed
                .unsubscribeOn(Schedulers.io())
                .subscribe(new Subscriber<List<Ribot>>() {
                    @Override
                    public void onCompleted() {
                        mMvpView.showRibotProgress(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("There was an error retrieving the ribots " + e);
                        Context context = mMvpView.getViewContext();
                        mMvpView.showRibotProgress(false);
                        mMvpView.showRibotsError(
                                context.getString(R.string.error_loading_ribots));
                    }

                    @Override
                    public void onNext(List<Ribot> ribots) {
                        if (!ribots.isEmpty()) {
                            Collections.sort(ribots);
                            mMvpView.showRibots(ribots);
                        } else {
                            mMvpView.showEmptyMessage();
                        }
                    }
                });
    }
}
