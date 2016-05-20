package io.ribot.app.ui.checkin;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.ribot.app.R;
import io.ribot.app.data.model.Venue;

public class VenuesAdapter extends RecyclerView.Adapter<VenuesAdapter.VenueViewHolder> {

    private List<Venue> mVenues;
    private Callback mCallback;
    private String mVenueInProgressId;
    private String mLastCheckInVenueId;

    public VenuesAdapter() {
        mVenues = new ArrayList<>();
    }

    public VenuesAdapter(List<Venue> venues) {
        mVenues = venues;
    }

    public void setVenues(List<Venue> venues) {
        mVenues = venues;
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void enableVenueProgress(String venueId) {
        mVenueInProgressId = venueId;
        Integer position = getVenuePosition(venueId);
        if (position != null) {
            notifyItemChanged(position);
        }
    }

    public void disableVenueProgress() {
        if (mVenueInProgressId != null) {
            Integer position = getVenuePosition(mVenueInProgressId);
            mVenueInProgressId = null;
            if (position != null) notifyItemChanged(position);
        }
    }

    public boolean isProgressEnabled() {
        return mVenueInProgressId != null;
    }

    public void setLastCheckedInVenue(String venueId) {
        mLastCheckInVenueId = venueId;
        notifyDataSetChanged();
    }

    public String getLastCheckInVenueId() {
        return mLastCheckInVenueId;
    }

    @Override
    public VenueViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_venue, parent, false);
        return new VenueViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(VenueViewHolder holder, int position) {
        Venue venue = mVenues.get(position);
        holder.setVenue(venue);
        holder.textVenueName.setText(venue.label);
        if (venue.id.equals(mVenueInProgressId)) {
            holder.progressCheckIn.setVisibility(View.VISIBLE);
        } else {
            holder.progressCheckIn.setVisibility(View.GONE);
        }
        if (venue.id.equals(mLastCheckInVenueId) && !venue.id.equals(mVenueInProgressId)) {
            holder.imageVenueTick.setVisibility(View.VISIBLE);
            holder.layoutItem.setAlpha(1f);
        } else {
            holder.imageVenueTick.setVisibility(View.GONE);
            holder.layoutItem.setAlpha(0.6f);
        }
    }

    @Override
    public int getItemCount() {
        return mVenues.size();
    }

    @Nullable
    private Integer getVenuePosition(String venueId) {
        for (int position = 0; position < getItemCount(); position++) {
            Venue venue = mVenues.get(position);
            if (venue.id.equals(venueId)) {
                return position;
            }
        }
        return null;
    }

    class VenueViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_venue_name)
        public TextView textVenueName;
        @BindView(R.id.image_venue_tick)
        public ImageView imageVenueTick;
        @BindView(R.id.progress_check_in)
        public ProgressBar progressCheckIn;
        @BindView(R.id.layout_item)
        public View layoutItem;
        public Venue venue;

        public VenueViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.layout_item)
        void onItemClicked() {
            if (mCallback != null) mCallback.onVenueClicked(venue);
        }

        public void setVenue(Venue venue) {
            this.venue = venue;
        }
    }

    interface Callback {
        void onVenueClicked(Venue venue);
    }

}
