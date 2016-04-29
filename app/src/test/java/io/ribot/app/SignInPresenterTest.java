package io.ribot.app;

import android.accounts.Account;
import android.content.Intent;

import com.google.android.gms.auth.UserRecoverableAuthException;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.test.common.MockModelFabric;
import io.ribot.app.ui.signin.SignInMvpView;
import io.ribot.app.ui.signin.SignInPresenter;
import io.ribot.app.util.RxSchedulersOverrideRule;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class SignInPresenterTest {

    @Mock SignInMvpView mMockSignInMvpView;
    @Mock DataManager mMockDataManager;
    private SignInPresenter mSignInPresenter;
    private final Account mAccount = new Account("accounts@ribot.co.uk", "com.google");

    @Rule
    public final RxSchedulersOverrideRule mOverrideSchedulersRule = new RxSchedulersOverrideRule();

    @Before
    public void setUp() {
        mSignInPresenter = new SignInPresenter(mMockDataManager);
        mSignInPresenter.attachView(mMockSignInMvpView);
    }

    @After
    public void detachView() {
        mSignInPresenter.detachView();
    }

    @Test
    public void signInSuccessful() {
        //Stub mock data manager

        Ribot ribot = MockModelFabric.newRibot();
        stubDataManagerSignIn(Observable.just(ribot));

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
        stubDataManagerSignIn(Observable.error(exception));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).onUserRecoverableAuthException(any(Intent.class));
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    @Test
    public void signInFailedWithGeneralErrorMessage() {
        //Stub mock data manager
        stubDataManagerSignIn(Observable.error(new RuntimeException("error")));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).setSignInButtonEnabled(true);
        verify(mMockSignInMvpView).showGeneralSignInError();
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    @Test
    public void signInFailedWithRibotProfileNotFoundError() {
        //Stub mock data manager
        HttpException http403Exception = new HttpException(Response.error(403,
                ResponseBody.create(MediaType.parse("type"), "")));
        stubDataManagerSignIn(Observable.error(http403Exception));

        mSignInPresenter.signInWithGoogle(mAccount);
        //Check that the right methods are called
        verify(mMockSignInMvpView).showProgress(true);
        verify(mMockSignInMvpView).setSignInButtonEnabled(true);
        verify(mMockSignInMvpView).showProfileNotFoundError(mAccount.name);
        verify(mMockSignInMvpView).showProgress(false);
        verify(mMockSignInMvpView).setSignInButtonEnabled(false);
    }

    private void stubDataManagerSignIn(Observable observable) {
        doReturn(observable)
                .when(mMockDataManager)
                .signIn(mAccount);
    }

}
