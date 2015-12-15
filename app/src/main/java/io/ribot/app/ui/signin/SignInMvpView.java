package io.ribot.app.ui.signin;

import android.content.Intent;

import io.ribot.app.data.model.Profile;
import io.ribot.app.ui.base.MvpView;

public interface SignInMvpView extends MvpView {

    void onSignInSuccessful(Profile signedInProfile);

    void onUserRecoverableAuthException(Intent recoverIntent);

    void showProgress(boolean show);

    void setSignInButtonEnabled(boolean enabled);

    void showProfileNotFoundError(String accountName);

    void showGeneralSignInError();

}
