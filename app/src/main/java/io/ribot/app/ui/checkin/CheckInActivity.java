package io.ribot.app.ui.checkin;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import io.ribot.app.R;
import io.ribot.app.data.model.Venue;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.util.DialogFactory;
import io.ribot.app.util.ViewUtil;

public class CheckInActivity extends BaseActivity
        implements CheckInMvpView, VenuesAdapter.Callback {

    public static final long FINISH_DELAY_AFTER_CHECK_IN = 1000; //1sec

    @Inject
    protected CheckInPresenter mCheckInPresenter;

    @Bind(R.id.edit_text_location)
    EditText mEditTextLocation;
    @Bind(R.id.recycler_view_venues)
    RecyclerView mRecyclerViewVenues;
    @Bind(R.id.progress_venues)
    ProgressBar mProgressVenues;
    @Bind(R.id.progress_check_in)
    ProgressBar mProgressCheckIn;
    @Bind(R.id.fab_check_in)
    FloatingActionButton mFabCheckIn;

    private VenuesAdapter mVenuesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        activityComponent().inject(this);
        ButterKnife.bind(this);
        mCheckInPresenter.attachView(this);
        // Setup recycler view
        mVenuesAdapter = new VenuesAdapter();
        mVenuesAdapter.setCallback(this);
        mRecyclerViewVenues.setAdapter(mVenuesAdapter);
        mRecyclerViewVenues.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerViewVenues.requestFocus();

        mCheckInPresenter.loadTodayLatestCheckInWithLabel();
        mCheckInPresenter.loadVenues();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCheckInPresenter.detachView();
    }

    @OnTextChanged(R.id.edit_text_location)
    void venueEditTextChanged(CharSequence text) {
        showCheckInButton(text.length() > 1);
    }

    @OnClick(R.id.fab_check_in)
    void onFabClicked() {
        ViewUtil.hideKeyboard(this);
        mCheckInPresenter.checkIn(mEditTextLocation.getText().toString());
    }

    /***** MVP View methods implementation *****/

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showVenues(List<Venue> venues, @Nullable String todayLatestCheckInVenueId) {
        mVenuesAdapter.setVenues(venues);
        mVenuesAdapter.notifyDataSetChanged();
        if (todayLatestCheckInVenueId != null) {
            mVenuesAdapter.setLastCheckedInVenue(todayLatestCheckInVenueId);
        }
        mRecyclerViewVenues.setVisibility(View.VISIBLE);
    }

    @Override
    public void showVenuesProgress(boolean show) {
        mProgressVenues.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCheckInProgress(boolean show) {
        mProgressCheckIn.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void showCheckInAtVenueProgress(boolean show, String venueId) {
        if (show) {
            mVenuesAdapter.enableVenueProgress(venueId);
        } else {
            mVenuesAdapter.disableVenueProgress();
        }
    }

    @Override
    public void showCheckInSuccessful(String venueName) {
        setEditTextCompoundTick();
        if (mVenuesAdapter.getLastCheckInVenueId() != null) {
            mVenuesAdapter.setLastCheckedInVenue(null);
        }
        onCheckInSuccessful(venueName);
    }

    @Override
    public void showCheckInAtVenueSuccessful(Venue venue) {
        mVenuesAdapter.setLastCheckedInVenue(venue.id);
        onCheckInSuccessful(venue.label);
        // Clear the edit text tick in case there was one from a previous label check in.
        mEditTextLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, null, null);
    }

    @Override
    public void showCheckInFailed(String errorMessage) {
        DialogFactory.createGenericErrorDialog(this, errorMessage).show();
    }

    @Override
    public void showCheckInButton(boolean show) {
        if (show) {
            mFabCheckIn.show();
        } else {
            mFabCheckIn.hide();
        }
    }

    @Override
    public void showTodayLatestCheckInWithLabel(String label) {
        mEditTextLocation.setHintTextColor(Color.WHITE);
        mEditTextLocation.setHint(label);
        setEditTextCompoundTick();
    }

    /***** VenuesAdapter callback *****/

    @Override
    public void onVenueClicked(Venue venue) {
        // Check that there isn't another in progress
        if (!mVenuesAdapter.isProgressEnabled()) {
            mCheckInPresenter.checkInAtVenue(venue);
        }
    }

    private void onCheckInSuccessful(String venueName) {
        String msg = getString(R.string.manual_check_in_successful, venueName);
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
        finishDelayed(FINISH_DELAY_AFTER_CHECK_IN);
    }

    private void finishDelayed(long delay) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, delay);
    }

    private void setEditTextCompoundTick() {
        Drawable tick = ContextCompat.getDrawable(this, R.drawable.ic_tick);
        mEditTextLocation.setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, tick, null);
    }

}
