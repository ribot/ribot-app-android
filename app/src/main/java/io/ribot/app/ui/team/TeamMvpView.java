package io.ribot.app.ui.team;

import java.util.List;

import io.ribot.app.data.model.Ribot;
import io.ribot.app.ui.base.MvpView;

public interface TeamMvpView extends MvpView {

    void showRibots(List<Ribot> ribots);

    void showRibotProgress(boolean show);

    void showEmptyMessage();

    void showRibotsError();
}
