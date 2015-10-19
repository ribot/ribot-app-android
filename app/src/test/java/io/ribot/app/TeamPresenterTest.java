package io.ribot.app;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.team.TeamMvpView;
import io.ribot.app.ui.team.TeamPresenter;
import io.ribot.app.util.DefaultConfig;
import rx.Observable;

import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class TeamPresenterTest {

    private TeamPresenter mPresenter;
    private TeamMvpView mMockMvpView;

    // We mock the DataManager because there is not need to test the dataManager again
    // from the presenters because there is already a DataManagerTest class.
    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application, true);

    @Before
    public void setUp() {
        mMockMvpView = mock(TeamMvpView.class);
        when(mMockMvpView.getViewContext()).thenReturn(RuntimeEnvironment.application);
        mPresenter = new TeamPresenter();
        mPresenter.attachView(mMockMvpView);
    }

    @After
    public void detachView() {
        mPresenter.detachView();
    }

    @Test
    public void loadRibotsSuccessful() {
        List<Ribot> ribots = MockModelFabric.newRibotList(20);
        doReturn(Observable.just(ribots))
                .when(component.getDataManager())
                .getRibots();

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView).showRibots(ribots);
        verify(mMockMvpView).showRibotProgress(false);
    }

    @Test
    public void loadRibotsFail() {
        doReturn(Observable.error(new RuntimeException()))
                .when(component.getDataManager())
                .getRibots();

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView, never()).showRibots(anyListOf(Ribot.class));
        verify(mMockMvpView).showRibotProgress(false);
        verify(mMockMvpView).showRibotsError(
                RuntimeEnvironment.application.getString(R.string.error_loading_ribots));
    }

    @Test
    public void loadRibotsEmpty() {
        List<Ribot> emptyList = new ArrayList<>();
        doReturn(Observable.just(emptyList))
                .when(component.getDataManager())
                .getRibots();

        mPresenter.loadRibots();
        verify(mMockMvpView).showRibotProgress(true);
        verify(mMockMvpView).showRibotProgress(false);
        verify(mMockMvpView).showEmptyMessage();
    }

}
