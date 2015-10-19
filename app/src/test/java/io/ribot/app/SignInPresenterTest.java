package io.ribot.app;

import android.accounts.Account;
import android.content.Context;
import android.content.Intent;

import com.google.android.gms.auth.UserRecoverableAuthException;

import org.junit.After;
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
import io.ribot.app.ui.signin.SignInMvpView;
import io.ribot.app.ui.signin.SignInPresenter;
import io.ribot.app.util.DefaultConfig;
import retrofit.HttpException;
import retrofit.Response;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = DefaultConfig.EMULATE_SDK)
public class SignInPresenterTest {

    private SignInPresenter mSignInPresenter;
    private SignInMvpView mMockSignInMvpView;
    private Account mAccount;

    // We mock the DataManager because there is not need to test the dataManager again
    // from the presenters because there is already a DataManagerTest class.
    @Rule
    public final TestComponentRule component =
            new TestComponentRule((RibotApplication) RuntimeEnvironment.application, true);

    @Before
    public void setUp() {
        mMockSignInMvpView = mock(SignInMvpView.class);
        when(mMockSignInMvpView.getViewContext()).thenReturn(RuntimeEnvironment.application);
        mSignInPresenter = new SignInPresenter();
        mSignInPresenter.attachView(mMockSignInMvpView);
        mAccount = new Account("ivan@ribot.co.uk", "com.google");
    }

    @After
    public void detachView() {
        mSignInPresenter.detachView();
    }

    @Test
    public void signInSuccessful() {
        //Stub mock data manager

        Ribot ribot = MockModelFabric.newRibot();
        doReturn(Observable.just(ribot))
                .when(component.getDataManager())
                .signIn(any(Context.class), eq(mAccount));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).onSignInSuccessful(ribot.profile);
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    @Test
    public void signInFailedWithUserRecoverableException() {
        //Stub mock data manager
        Intent intent = new Intent();
        UserRecoverableAuthException exception = new UserRecoverableAuthException("error", intent);
        doReturn(Observable.error(exception))
                .when(component.getDataManager())
                .signIn(any(Context.class), eq(mAccount));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).onUserRecoverableAuthException(intent);
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    @Test
    public void signInFailedWithGeneralErrorMessage() {
        //Stub mock data manager
        doReturn(Observable.error(new RuntimeException("error")))
                .when(component.getDataManager())
                .signIn(any(Context.class), eq(mAccount));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).setSignInButtonEnabled(true);
        verify(mMockSignInMvpView)
                .showError(RuntimeEnvironment.application.getString(R.string.error_sign_in));
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    @Test
    public void signInFailedWithRibotProfileNotFoundError() {
        //Stub mock data manager
        HttpException http403Exception = new HttpException(Response.error(403, null));
        doReturn(Observable.error(http403Exception))
                .when(component.getDataManager())
                .signIn(any(Context.class), eq(mAccount));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).setSignInButtonEnabled(true);
        String expectedError = RuntimeEnvironment.application
                .getString(R.string.error_ribot_profile_not_found, mAccount.name);
        verify(mMockSignInMvpView).showError(expectedError);
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

}
