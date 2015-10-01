package io.ribot.app.ui.main;

import javax.inject.Inject;

import io.ribot.app.R;
import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.Presenter;

public class MainPresenter implements Presenter<MainMvpView> {

    @Inject
    protected DataManager mDataManager;
    private MainMvpView mMvpView;

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
    }
}
