package io.ribot.app.data.remote;

import android.accounts.Account;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

import javax.inject.Inject;

import io.ribot.app.BuildConfig;
import io.ribot.app.injection.ApplicationContext;
import rx.Observable;
import rx.Subscriber;

public class GoogleAuthHelper {

    private static final String SCOPE = String.format(
            "oauth2:server:client_id:%s:api_scope:https://www.googleapis.com/auth/userinfo.profile"
            + " https://www.googleapis.com/auth/userinfo.email",
            BuildConfig.GOOGLE_API_SERVER_CLIENT_ID);

    private final Context mContext;

    @Inject
    public GoogleAuthHelper(@ApplicationContext Context context) {
        mContext = context;
    }

    public String retrieveAuthToken(Account account)
            throws GoogleAuthException, IOException {

        String token = GoogleAuthUtil.getToken(mContext, account, SCOPE);
        // Token needs to be clear so we make sure next time we get a brand new one. Otherwise this
        // may return a token that has already been used by the API and because it's a one time
        // token it won't work.
        GoogleAuthUtil.clearToken(mContext, token);
        return token;
    }

    public Observable<String> retrieveAuthTokenAsObservable(final Account account) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(retrieveAuthToken(account));
                    subscriber.onCompleted();
                } catch (Throwable error) {
                    subscriber.onError(error);
                }
            }
        });
    }
}
