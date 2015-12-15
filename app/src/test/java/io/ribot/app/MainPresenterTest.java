package io.ribot.app;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.ribot.app.data.DataManager;
import io.ribot.app.ui.main.MainMvpView;
import io.ribot.app.ui.main.MainPresenter;
import io.ribot.app.util.RxSchedulersOverrideRule;
import rx.Observable;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class MainPresenterTest {

    @Mock MainMvpView mMockMainMvpView;
    @Mock DataManager mMockDataManager;
    private MainPresenter mMainPresenter;

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() {
        mMainPresenter = new MainPresenter(mMockDataManager);
        mMainPresenter.attachView(mMockMainMvpView);
    }

    @After
    public void detachView() {
        mMainPresenter.detachView();
    }

    @Test
    public void signOutSuccessful() {
        doReturn(Observable.empty())
                .when(mMockDataManager)
                .signOut();

        mMainPresenter.signOut();
        verify(mMockMainMvpView).onSignedOut();
    }
}
