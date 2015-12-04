package io.ribot.app.test.common.injection.module;

import android.accounts.AccountManager;
import android.app.Application;
import android.content.Context;

import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import io.ribot.app.data.remote.RibotService;
import io.ribot.app.injection.ApplicationContext;
import rx.subscriptions.CompositeSubscription;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

/**
 * Provides application-level dependencies for an app running on a testing environment
 * This allows injecting mocks if necessary.
 */
@Module
public class ApplicationTestModule {
    protected final Application mApplication;

    public ApplicationTestModule(Application application) {
        mApplication = application;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    Bus provideEventBus() {
        return new Bus();
    }

    @Provides
    @Singleton
    RibotService provideRibotService() {
        return mock(RibotService.class);
    }

    @Provides
    AccountManager provideAccountManager() {
        return mock(AccountManager.class);
    }

    @Provides
    CompositeSubscription provideCompositeSubscription() {
        return new CompositeSubscription();
    }
}
