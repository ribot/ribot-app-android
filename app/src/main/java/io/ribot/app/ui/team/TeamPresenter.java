package io.ribot.app.ui.team;


import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.Presenter;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class TeamPresenter implements Presenter<TeamMvpView> {

    private final DataManager mDataManager;
    public Subscription mSubscription;
    private TeamMvpView mMvpView;
    private List<Ribot> mCachedRibots;

    @Inject
    public TeamPresenter(DataManager dataManager) {
        mDataManager = dataManager;
    }

    @Override
    public void attachView(TeamMvpView mvpView) {
        mMvpView = mvpView;
    }

    @Override
    public void detachView() {
        mMvpView = null;
        if (mSubscription != null) mSubscription.unsubscribe();
    }

    public void loadRibots() {
        loadRibots(false);
    }

    /**
     * Load the list of Ribots
     * @param allowMemoryCacheVersion if true a cached version will be returned from memory,
     *                                unless nothing is cached yet. Use false if you want an up
     *                                to date version of the ribots.
     */
    public void loadRibots(boolean allowMemoryCacheVersion) {
        mMvpView.showRibotProgress(true);
        if (mSubscription != null) mSubscription.unsubscribe();
        mSubscription = getRibotsObservable(allowMemoryCacheVersion)
                .subscribe(new Subscriber<List<Ribot>>() {
                    @Override
                    public void onCompleted() {
                        mMvpView.showRibotProgress(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("There was an error retrieving the ribots " + e);
                        mMvpView.showRibotProgress(false);
                        mMvpView.showRibotsError();
                    }

                    @Override
                    public void onNext(List<Ribot> ribots) {
                        mCachedRibots = ribots;
                        if (!ribots.isEmpty()) {
                            Collections.sort(ribots);
                            mMvpView.showRibots(ribots);
                        } else {
                            mMvpView.showEmptyMessage();
                        }
                    }
                });
    }

    private Observable<List<Ribot>> getRibotsObservable(boolean allowMemoryCacheVersion) {
        if (allowMemoryCacheVersion && mCachedRibots != null) {
            return Observable.just(mCachedRibots);
        } else {
            return mDataManager.getRibots()
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeOn(Schedulers.io());
        }
    }
}
