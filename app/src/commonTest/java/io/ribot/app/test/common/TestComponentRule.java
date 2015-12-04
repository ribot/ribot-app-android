package io.ribot.app.test.common;

import android.accounts.AccountManager;
import android.content.Context;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import io.ribot.app.RibotApplication;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.local.DatabaseHelper;
import io.ribot.app.data.local.PreferencesHelper;
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
    private Context mContext;

    public TestComponentRule(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }

    public RibotService getMockRibotsService() {
        return mTestComponent.ribotService();
    }

    public AccountManager getMockAccountManager() {
        return mTestComponent.accountManager();
    }

    //tODO remove
    public DataManager getDataManager() {
        return mTestComponent.dataManager();
    }

    public DatabaseHelper getDatabaseHelper() {
        return mTestComponent.databaseHelper();
    }

    public PreferencesHelper getPreferencesHelper() {
        return mTestComponent.preferencesHelper();
    }

    private void setupDaggerTestComponentInApplication() {
        RibotApplication application = RibotApplication.get(mContext);
        mTestComponent = DaggerTestComponent.builder()
                .applicationTestModule(new ApplicationTestModule(application))
                .build();
        application.setComponent(mTestComponent);
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
