package io.ribot.app;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.ui.main.MainMvpView;
import io.ribot.app.ui.main.MainPresenter;
import io.ribot.app.util.DefaultConfig;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class MainPresenterTest {

    private MainPresenter mMainPresenter;
    private MainMvpView mMockMainMvpView;
    private Ribot mSignedInRibot;

    // We mock the DataManager because there is not need to test the dataManager again
    // from the presenters because there is already a DataManagerTest class.
    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application, true);

    @Before
    public void setUp() {
        mMockMainMvpView = mock(MainMvpView.class);
        when(mMockMainMvpView.getContext()).thenReturn(RuntimeEnvironment.application);
        mMainPresenter = new MainPresenter();
        mSignedInRibot = MockModelFabric.newRibot();
        //Emulate a signed in user
        component.getPreferencesHelper().putSignedInRibot(mSignedInRibot);
        mMainPresenter.attachView(mMockMainMvpView);
    }

    @Test
    public void checkShowWelcomeMessage() {
        String expectedMessage = RuntimeEnvironment.application
                .getString(R.string.signed_in_welcome, mSignedInRibot.profile.name.first);
        verify(mMockMainMvpView).showWelcomeMessage(expectedMessage);
    }
}
