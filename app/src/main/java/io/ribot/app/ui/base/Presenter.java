package io.ribot.app.ui.base;

/**
 * Every presenter in the app must implement this interface and indicate the MvpView type
 * that wants to be attached with.
 */
public interface Presenter<V extends MvpView> {

    void attachView(V mvpView);

    void detachView();
}
