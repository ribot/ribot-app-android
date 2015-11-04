package io.ribot.app.ui.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.ribot.app.R;
import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.Encounter;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.util.DateUtil;

public class RibotAdapter extends RecyclerView.Adapter<RibotAdapter.RibotHolder> {
    private List<Ribot> mTeamMembers;
    private Context mContext;

    public RibotAdapter(Context context) {
        this.mContext = context;
        this.mTeamMembers = new ArrayList<>();
    }

    @Override
    public RibotHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view =
                LayoutInflater.from(parent.getContext()).inflate(R.layout.item_team, parent, false);
        return new RibotHolder(view);
    }

    @Override
    public void onBindViewHolder(final RibotHolder holder, final int position) {
        Ribot ribot = mTeamMembers.get(position);
        holder.name.setText(ribot.profile.name.first);
        Picasso.with(mContext)
                .load(ribot.profile.avatar)
                .fit()
                .centerCrop()
                .placeholder(R.drawable.profile_placeholder_large)
                .into(holder.avatar);
        CheckIn latestCheckIn = ribot.getLatestCheckIn();

        if (latestCheckIn != null
                && !latestCheckIn.isCheckedOut
                && DateUtil.isToday(latestCheckIn.checkedInDate.getTime())) {
            Encounter latestEncounter = latestCheckIn.getLatestEncounter();
            holder.location.setText(latestEncounter == null ? latestCheckIn.getLocationName() :
                    latestEncounter.beacon.zone.label);
            holder.layoutContainer.setAlpha(1f);
        } else {
            holder.layoutContainer.setAlpha(0.4f);
            holder.location.setText(mContext.getString(R.string.text_checked_out));
        }
    }

    @Override
    public int getItemCount() {
        return mTeamMembers.size();
    }

    public void setTeamMembers(List<Ribot> list) {
        mTeamMembers = list;
    }

    class RibotHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.layout_container)
        public RelativeLayout layoutContainer;

        @Bind(R.id.text_name)
        public TextView name;

        @Bind(R.id.circle_image_profile_main)
        public ImageView avatar;

        @Bind(R.id.text_location)
        public TextView location;

        public RibotHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}