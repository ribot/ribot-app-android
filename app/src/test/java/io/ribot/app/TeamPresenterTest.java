package io.ribot.app;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.ui.team.TeamMvpView;
import io.ribot.app.ui.team.TeamPresenter;
import io.ribot.app.util.RxSchedulersOverrideRule;
import rx.Observable;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class TeamPresenterTest {

    @Mock TeamMvpView mMockMvpView;
    @Mock DataManager mMockDataManager;
    private TeamPresenter mPresenter;

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() {
        mPresenter = new TeamPresenter(mMockDataManager);
        mPresenter.attachView(mMockMvpView);
    }

    @After
    public void detachView() {
        mPresenter.detachView();
    }

    @Test
    public void loadRibotsSuccessful() {
        List<Ribot> ribots = MockModelFabric.newRibotList(20);
        stubDataManagerGetRibots(Observable.just(ribots));

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView).showRibots(ribots);
        verify(mMockMvpView).showRibotProgress(false);
    }

    @Test
    public void loadRibotsFail() {
        stubDataManagerGetRibots(Observable.error(new RuntimeException()));

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView, never()).showRibots(anyListOf(Ribot.class));
        verify(mMockMvpView).showRibotProgress(false);
        verify(mMockMvpView).showRibotsError();
    }

    @Test
    public void loadRibotsEmpty() {
        List<Ribot> emptyList = new ArrayList<>();
        stubDataManagerGetRibots(Observable.just(emptyList));

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView).showRibotProgress(false);
        verify(mMockMvpView).showEmptyMessage();
    }

    private void stubDataManagerGetRibots(Observable observable) {
        doReturn(observable)
                .when(mMockDataManager)
                .getRibots();
    }

}
