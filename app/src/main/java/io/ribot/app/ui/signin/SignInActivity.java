package io.ribot.app.ui.signin;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ribot.app.R;
import io.ribot.app.data.DataManager;
import io.ribot.app.data.model.Profile;
import io.ribot.app.ui.WelcomeActivity;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.util.DialogFactory;
import timber.log.Timber;

public class SignInActivity extends BaseActivity implements SignInMvpView {

    private static final String EXTRA_POPUP_MESSAGE =
            "io.ribot.app.ui.signin.SignInActivity.EXTRA_POPUP_MESSAGE";
    private static final int REQUEST_CODE_PLAY_SERVICES = 1;
    private static final int REQUEST_PERMISSION_GET_ACCOUNTS = 2;
    private static final int REQUEST_CODE_ACCOUNT_PICKER = 3;
    private static final int REQUEST_CODE_AUTH_EXCEPTION = 4;
    public static final String ACCOUNT_TYPE_GOOGLE = "com.google";

    @Inject AccountManager mAccountManager;
    @Inject DataManager mDataManager;
    @Inject SignInPresenter mSignInPresenter;
    private Account mSelectedAccount;
    private boolean mShouldFinishOnStop;

    @BindView(R.id.button_sign_in) Button mSignInButton;
    @BindView(R.id.progress) ProgressBar mProgressBar;
    @BindView(R.id.text_welcome) TextView mWelcomeTextView;
    @BindView(R.id.image_ribot_logo) ImageView mRibotLogo;

    public static Intent getStartIntent(Context context, boolean clearPreviousActivities) {
        Intent intent = new Intent(context, SignInActivity.class);
        if (clearPreviousActivities) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        }
        return intent;
    }

    // popUpMessage will show on a Dialog as soon as the Activity opens
    public static Intent getStartIntent(Context context,
                                        boolean clearPreviousActivities,
                                        String popUpMessage) {
        Intent intent = getStartIntent(context, clearPreviousActivities);
        intent.putExtra(EXTRA_POPUP_MESSAGE, popUpMessage);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mShouldFinishOnStop = false;
        setContentView(R.layout.activity_sign_in);
        activityComponent().inject(this);
        ButterKnife.bind(this);
        mSignInPresenter.attachView(this);
        setSignInButtonEnabled(false);

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            setSignInButtonEnabled(true);
        } else if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
            GooglePlayServicesUtil
                    .getErrorDialog(resultCode, this, REQUEST_CODE_PLAY_SERVICES)
                    .show();
        } else {
            showNoPlayServicesError();
            Timber.e("This device doesn't support Play Services");
        }

        String popUpMessage = getIntent().getStringExtra(EXTRA_POPUP_MESSAGE);
        if (popUpMessage != null) {
            DialogFactory.createSimpleOkErrorDialog(this, popUpMessage).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mShouldFinishOnStop) finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSignInPresenter.detachView();
    }

    @OnClick(R.id.button_sign_in)
    void signInWithGoogle() {
        String getAccountsPermission = Manifest.permission.GET_ACCOUNTS;
        if (hasPermission(getAccountsPermission)) {
            triggerAccountPicker();
        } else {
            requestPermissionsSafely(new String[]{getAccountsPermission},
                    REQUEST_PERMISSION_GET_ACCOUNTS);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_GET_ACCOUNTS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    triggerAccountPicker();
                } else {
                    DialogFactory.createSimpleOkErrorDialog(this,
                            R.string.title_permissions,
                            R.string.permission_not_accepted_get_accounts).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case REQUEST_CODE_PLAY_SERVICES:
                if (responseCode == RESULT_OK) {
                    setSignInButtonEnabled(true);
                }
                break;
            case REQUEST_CODE_ACCOUNT_PICKER:
                if (responseCode == RESULT_OK) {
                    String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    mSelectedAccount = findGoogleAccountByName(accountName);
                    mSignInPresenter.signInWithGoogle(mSelectedAccount);
                } else {
                    showProgress(false);
                    setSignInButtonEnabled(true);
                }
                break;
            case REQUEST_CODE_AUTH_EXCEPTION:
                if (responseCode == RESULT_OK) {
                    mSignInPresenter.signInWithGoogle(mSelectedAccount);
                } else {
                    showProgress(false);
                    setSignInButtonEnabled(true);
                }
                break;
            default:
                super.onActivityResult(requestCode, responseCode, intent);
        }
    }

    /***** MVP View methods implementation *****/

    @Override
    public void onSignInSuccessful(Profile signedInProfile) {
        // We only want to show the welcome screen for 2 seconds
        Intent intent = WelcomeActivity.newStartIntent(this, signedInProfile, 2000);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            startActivity(intent);
        } else {
            ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this,
                    mRibotLogo, getString(R.string.transition_ribot_logo));
            startActivity(intent, options.toBundle());
        }
        // We need this flag because if we call finish() here, the activity transition won't work
        mShouldFinishOnStop = true;
    }

    @Override
    public void onUserRecoverableAuthException(Intent recoverIntent) {
        startActivityForResult(recoverIntent, REQUEST_CODE_AUTH_EXCEPTION);
    }

    @Override
    public void showProgress(boolean show) {
        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setSignInButtonEnabled(boolean enabled) {
        mSignInButton.setVisibility(enabled ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void showProfileNotFoundError(String accountName) {
        DialogFactory.createSimpleOkErrorDialog(this,
                getString(R.string.error_ribot_profile_not_found, accountName)).show();
    }

    @Override
    public void showGeneralSignInError() {
        DialogFactory.createSimpleOkErrorDialog(this, getString(R.string.error_sign_in)).show();
    }

    /***** Private helper methods  *****/

    private void triggerAccountPicker() {
        // We need to use the deprecated method because the other one is only supported in API
        // level 23
        setSignInButtonEnabled(false);
        Intent intent = AccountManager.newChooseAccountIntent(null, null,
                new String[]{ACCOUNT_TYPE_GOOGLE}, true, getString(R.string.text_ribot_account),
                null, null, null);
        startActivityForResult(intent, REQUEST_CODE_ACCOUNT_PICKER);
    }

    private Account findGoogleAccountByName(String accountName) {
        Account[] accounts = mAccountManager.getAccountsByType(ACCOUNT_TYPE_GOOGLE);
        for (Account account : accounts) {
            if (account.name.equals(accountName)) {
                return account;
            }
        }
        return null;
    }

    private void showNoPlayServicesError() {
        Dialog playServicesDialog = DialogFactory.createSimpleOkErrorDialog(
                SignInActivity.this,
                R.string.dialog_error_title,
                R.string.error_message_play_services);

        playServicesDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        playServicesDialog.show();
    }
}
