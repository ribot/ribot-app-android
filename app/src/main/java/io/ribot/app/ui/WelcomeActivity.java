package io.ribot.app.ui;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;
import io.ribot.app.R;
import io.ribot.app.data.model.Profile;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.ui.main.MainActivity;
import timber.log.Timber;

public class WelcomeActivity extends BaseActivity {

    private static final String EXTRA_PROFILE = "io.ribot.app.ui.WelcomeActivity.EXTRA_PROFILE";
    private static final String EXTRA_TIME_DISPLAYING =
            "io.ribot.app.ui.WelcomeActivity.EXTRA_TIME_DISPLAYING";

    @BindView(R.id.layout_profile_info) View mProfileInfoLayout;
    @BindView(R.id.image_profile) CircleImageView mProfileImage;
    @BindView(R.id.text_greeting) TextView mGreetingText;

    private Handler mHandler;
    private long mTimeDisplaying;

    public static Intent newStartIntent(Context context, Profile profile) {
        Intent intent = new Intent(context, WelcomeActivity.class);
        intent.putExtra(EXTRA_PROFILE, profile);
        return intent;
    }

    // timeDisplaying is milliseconds Activity remains opened until it navigates to main screen.
    public static Intent newStartIntent(Context context, Profile profile, long timeDisplaying) {
        Intent intent = newStartIntent(context, profile);
        intent.putExtra(EXTRA_TIME_DISPLAYING, timeDisplaying);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getResources().getColor(R.color.black_20p));
        }
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        // Set profile values in views
        Intent intent = getIntent();
        Profile profile = intent.getParcelableExtra(EXTRA_PROFILE);
        mTimeDisplaying = intent.getLongExtra(EXTRA_TIME_DISPLAYING, -1);
        mGreetingText.setText(getString(R.string.welcome_greetings, profile.name.first));
        loadProfileImage(profile.avatar);

        String hexColor = profile.hexColor;
        if (hexColor != null) mProfileInfoLayout.setBackgroundColor(Color.parseColor(hexColor));

        mHandler = new Handler();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // We need to start the reveal animation from the listener so we make sure the view
            // is attached.
            // http://stackoverflow.com/questions/26819429/cannot-start-this-animator-on-a-detached-view-reveal-effect
            mProfileInfoLayout.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                           int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    v.removeOnLayoutChangeListener(this);
                    revealProfileInfo();
                }
            });
        } else {
            mProfileInfoLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        if (mTimeDisplaying > 0) {
            mHandler.postDelayed(mNavigateToMainActivity, mTimeDisplaying);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mHandler.removeCallbacks(mNavigateToMainActivity);
    }

    private void loadProfileImage(String avatar) {
        Glide.with(this)
                .load(avatar)
                .placeholder(R.drawable.profile_placeholder_large)
                .error(R.drawable.profile_placeholder_large)
                .listener(new RequestListener<String, GlideDrawable>() {
                    @Override
                    public boolean onException(Exception e, String model,
                                               Target<GlideDrawable> target,
                                               boolean isFirstResource) {
                        Timber.e("There was an error loading the profile image");
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GlideDrawable resource, String model,
                                                   Target<GlideDrawable> target,
                                                   boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        mProfileImage.setBorderColor(Color.WHITE);
                        return false;
                    }
                });
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void revealProfileInfo() {
        int cx = mProfileInfoLayout.getMeasuredWidth() / 2;
        int cy = mProfileInfoLayout.getMeasuredHeight() / 2;
        int finalRadius = Math.max(mProfileInfoLayout.getWidth(),
                mProfileInfoLayout.getHeight()) / 2;
        Animator anim = ViewAnimationUtils
                .createCircularReveal(mProfileInfoLayout, cx, cy, 0, finalRadius);
        mProfileInfoLayout.setVisibility(View.VISIBLE);
        anim.start();
    }

    private Runnable mNavigateToMainActivity = new Runnable() {
        @Override
        public void run() {
            Intent intent = MainActivity.getStartIntent(WelcomeActivity.this, false);
            startActivity(intent);
            finish();
        }
    };
}
