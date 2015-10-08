package io.ribot.app.test.common.injection.module;

import android.accounts.AccountManager;
import android.app.Application;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ribot.app.data.DataManager;
import io.ribot.app.test.common.TestDataManager;
import rx.subscriptions.CompositeSubscription;

import static org.mockito.Mockito.spy;

/**
 * Provides application-level dependencies for an app running on a testing environment
 * This allows injecting mocks if necessary.
 */
@Module
public class ApplicationTestModule {
    private final Application mApplication;
    private boolean mMockableDataManager;

    public ApplicationTestModule(Application application, boolean mockableDataManager) {
        mApplication = application;
        mMockableDataManager = mockableDataManager;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    DataManager provideDataManager() {
        TestDataManager testDataManager = new TestDataManager(mApplication);
        return mMockableDataManager ? spy(testDataManager) : testDataManager;
    }

    @Provides
    @Singleton
    Bus provideEventBus() {
        return new Bus();
    }

    @Provides
    AccountManager provideAccountManager() {
        return AccountManager.get(mApplication);
    }

    @Provides
    CompositeSubscription provideCompositeSubscription() {
        return new CompositeSubscription();
    }
}
