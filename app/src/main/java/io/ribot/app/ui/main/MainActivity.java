package io.ribot.app.ui.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.estimote.sdk.SystemRequirementsChecker;

import javax.inject.Inject;

import butterknife.ButterKnife;
import io.ribot.app.R;
import io.ribot.app.service.AutoCheckInService;
import io.ribot.app.service.BeaconsSyncService;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.ui.checkin.CheckInActivity;
import io.ribot.app.ui.signin.SignInActivity;

public class MainActivity extends BaseActivity implements MainMvpView {

    private static final String EXTRA_AUTO_CHECK_IN_DISABLED =
            "io.ribot.app.ui.main.MainActivity.EXTRA_AUTO_CHECK_IN_DISABLED";

    @Inject
    MainPresenter mMainPresenter;

    /**
     * Create an Intent for the main activity.
     * Set autoCheckInDisabled to true if you want to prevent this activity from
     * triggering any auto check-in related service on create.
     * This is useful for testing purposes.
     */
    public static Intent getStartIntent(Context context, boolean autoCheckInDisabled) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_AUTO_CHECK_IN_DISABLED, autoCheckInDisabled);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainPresenter.attachView(this);

        // Trigger auto check-in related services, unless the flag indicates not to.
        boolean autoCheckInDisabled = getIntent()
                .getBooleanExtra(EXTRA_AUTO_CHECK_IN_DISABLED, false);
        if (!autoCheckInDisabled && savedInstanceState == null) {
            startService(BeaconsSyncService.getStartIntent(this));
            // Handle requesting Estimote sdk requirements like enabling bt or permissions
            SystemRequirementsChecker.checkWithDefaultDialogs(this);
            startService(AutoCheckInService.getStartIntent(this));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMainPresenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sign_out:
                mMainPresenter.signOut();
                return true;
            case R.id.action_check_in:
                startActivity(new Intent(this, CheckInActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***** MVP View methods implementation *****/

    @Override
    public void onSignedOut() {
        startActivity(SignInActivity.getStartIntent(this, true));
    }
}
