package io.ribot.app.test.common;

import android.content.Context;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.remote.GoogleAuthHelper;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.test.common.injection.component.DaggerTestComponent;
import io.ribot.app.test.common.injection.component.TestComponent;
import io.ribot.app.test.common.injection.module.ApplicationTestModule;

/**
 * Test rule that creates and sets a Dagger TestComponent into the application overriding the
 * existing application component.
 * Use this rule in your test case in order for the app to use mock dependencies.
 * It also exposes some of the dependencies so they can be easily accessed from the tests, e.g. to
 * stub mocks etc.
 */
public class TestComponentRule implements TestRule {

    private TestComponent mTestComponent;
    private RibotApplication mRibotApplication;
    private boolean mMockableDataManager;

    public TestComponentRule(RibotApplication ribotApplication) {
        mRibotApplication = ribotApplication;
        mMockableDataManager = false;
    }

    /**
     * If mockableDataManager is true, it will crate a data manager using Mockity.spy()
     * Spy objects call real methods unless they are stubbed. So the DataManager will work as
     * usual unless an specific method is mocked.
     * A full mock DataManager is not an option because there are several methods that still
     * need to return the real value, i.e dataManager.getSubscribeScheduler()
     */
    public TestComponentRule(RibotApplication ribotApplication, boolean mockableDataManager) {
        mRibotApplication = ribotApplication;
        mMockableDataManager = mockableDataManager;
    }

    public Context getApplication() {
        return mRibotApplication;
    }

    public TestComponent getTestComponent() {
        return mTestComponent;
    }

    public TestDataManager getDataManager() {
        return (TestDataManager) mTestComponent.dataManager();
    }

    public RibotService getMockRibotService() {
        return getDataManager().getRibotService();
    }

    public DatabaseHelper getDatabaseHelper() {
        return getDataManager().getDatabaseHelper();
    }

    public PreferencesHelper getPreferencesHelper() {
        return getDataManager().getPreferencesHelper();
    }

    public GoogleAuthHelper getMockGoogleAuthHelper() {
        return getDataManager().getGoogleAuthHelper();
    }

    private void setupDaggerTestComponentInApplication() {
        if (mRibotApplication.getComponent() instanceof TestComponent) {
            mTestComponent = (TestComponent) mRibotApplication.getComponent();
        } else {
            mTestComponent = DaggerTestComponent.builder()
                    .applicationTestModule(
                            new ApplicationTestModule(mRibotApplication, mMockableDataManager))
                    .build();
            mRibotApplication.setComponent(mTestComponent);
        }
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    setupDaggerTestComponentInApplication();
                    base.evaluate();
                } finally {
                    mTestComponent = null;
                }
            }
        };
    }
}
