package io.ribot.app;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.test.common.TestComponentRule;
import io.ribot.app.util.DefaultConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class PreferencesHelperTest {

    PreferencesHelper mPreferencesHelper;

    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application);

    @Before
    public void setUp() {
        mPreferencesHelper = component.getPreferencesHelper();
        mPreferencesHelper.clear();
    }

    @Test
    public void putAndGetAccessToken() {
        String token = "sexyAccessToken";
        mPreferencesHelper.putAccessToken(token);
        assertEquals(token, mPreferencesHelper.getAccessToken());
    }

    @Test
    public void putAndGetSignedInRibot() {
        Ribot ribot = MockModelFabric.newRibot();
        mPreferencesHelper.putSignedInRibot(ribot);
        assertEquals(ribot, mPreferencesHelper.getSignedInRibot());
    }

}

