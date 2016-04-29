package io.ribot.app.ui.team;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.ribot.app.R;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.BaseActivity;
import io.ribot.app.util.DialogFactory;

public class TeamFragment extends Fragment implements TeamMvpView {

    @Inject TeamPresenter mTeamPresenter;
    @Inject RibotAdapter mRibotAdapter;

    @BindView(R.id.recycler_view_team) RecyclerView mTeamRecycler;
    @BindView(R.id.swipe_refresh_container) SwipeRefreshLayout mSwipeRefreshContainer;
    @BindView(R.id.text_no_ribots) TextView mNoRibotsText;
    @BindView(R.id.progress) ProgressBar mProgress;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_team, container, false);
        ButterKnife.bind(this, fragmentView);
        mTeamPresenter.attachView(this);
        mTeamRecycler.setHasFixedSize(true);
        mTeamRecycler.setAdapter(mRibotAdapter);
        mSwipeRefreshContainer.setColorSchemeResources(R.color.primary);
        mSwipeRefreshContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mTeamPresenter.loadRibots();
            }
        });
        return fragmentView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mTeamPresenter.loadRibots();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTeamPresenter.detachView();
    }

    @Override
    public void showRibots(List<Ribot> ribots) {
        mRibotAdapter.setTeamMembers(ribots);
        mRibotAdapter.notifyDataSetChanged();
        mNoRibotsText.setVisibility(View.GONE);
    }

    @Override
    public void showRibotProgress(boolean show) {
        mSwipeRefreshContainer.setRefreshing(show);
        if (show && mRibotAdapter.getItemCount() == 0) {
            mProgress.setVisibility(View.VISIBLE);
        } else {
            mProgress.setVisibility(View.GONE);
        }
    }

    @Override
    public void showEmptyMessage() {
        mNoRibotsText.setVisibility(View.VISIBLE);
    }

    @Override
    public void showRibotsError() {
        DialogFactory.createSimpleOkErrorDialog(getActivity(),
                getString(R.string.error_loading_ribots)).show();
    }
}
