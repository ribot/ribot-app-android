package io.ribot.app.injection;

import android.support.test.InstrumentationRegistry;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
import io.ribot.app.data.remote.RibotsService;
import io.ribot.app.injection.component.DaggerTestComponent;
import io.ribot.app.injection.component.TestComponent;
import io.ribot.app.injection.module.ApplicationTestModule;
import io.ribot.app.util.TestDataManager;

/**
 * Test rule that creates and sets a Dagger TestComponent into the application overriding the
 * existing application component.
 * Use this rule in your test case in order for the app to use mock dependencies.
 * It also exposes some of the dependencies so they can be easily accessed from the tests, e.g. to
 * stub mocks etc.
 */
public class TestComponentRule implements TestRule {

    private TestComponent mTestComponent;

    public TestComponent getTestComponent() {
        return mTestComponent;
    }

    public TestDataManager getDataManager() {
        return (TestDataManager) mTestComponent.dataManager();
    }

    public RibotsService getMockRibotsService() {
        return getDataManager().getRibotsService();
    }

    public DatabaseHelper getDatabaseHelper() {
        return getDataManager().getDatabaseHelper();
    }

    public PreferencesHelper getPreferencesHelper() {
        return getDataManager().getPreferencesHelper();
    }

    private void setupDaggerTestComponentInApplication() {
        RibotApplication application = RibotApplication
                .get(InstrumentationRegistry.getTargetContext());
        if (application.getComponent() instanceof TestComponent) {
            mTestComponent = (TestComponent) application.getComponent();
        } else {
            mTestComponent = DaggerTestComponent.builder()
                    .applicationTestModule(new ApplicationTestModule(application))
                    .build();
            application.setComponent(mTestComponent);
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
