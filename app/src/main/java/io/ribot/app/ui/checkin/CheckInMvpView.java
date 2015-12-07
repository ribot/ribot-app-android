package io.ribot.app.ui.checkin;

import android.support.annotation.Nullable;

import java.util.List;

import io.ribot.app.data.model.Venue;
import io.ribot.app.ui.base.MvpView;

public interface CheckInMvpView extends MvpView {

    void showVenues(List<Venue> venues, @Nullable String todayLatestCheckInVenueId);

    void showVenuesProgress(boolean show);

    void showCheckInProgress(boolean show);

    void showCheckInAtVenueProgress(boolean show, String venueId);

    void showCheckInSuccessful(String venueName);

    void showCheckInAtVenueSuccessful(Venue venue);

    void showCheckInFailed();

    void showCheckInButton(boolean show);

    void showTodayLatestCheckInWithLabel(String label);
}
