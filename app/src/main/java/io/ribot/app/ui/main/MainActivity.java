package io.ribot.app.ui.main;

import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ribot.app.R;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.ui.signin.SignInActivity;

public class MainActivity extends BaseActivity implements MainMvpView {

    @Inject
    MainPresenter mMainPresenter;

    @Bind(R.id.textView)
    TextView mTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mMainPresenter.attachView(this);
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
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /***** MVP View methods implementation *****/

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showWelcomeMessage(String message) {
        mTextView.setText(message);
    }

    @Override
    public void onSignedOut() {
        startActivity(SignInActivity.newStartIntent(this, true));
    }
}
