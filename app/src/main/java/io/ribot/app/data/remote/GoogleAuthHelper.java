package io.ribot.app.data.remote;

import android.accounts.Account;
import android.content.Context;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;

public class GoogleAuthHelper {

    private static final String GOOGLE_API_SERVER_CLIENT_ID =
            "***REMOVED***";
    private static final String SCOPE = "oauth2:server:client_id:" + GOOGLE_API_SERVER_CLIENT_ID
            + ":api_scope:https://www.googleapis.com/auth/userinfo.profile"
            + " https://www.googleapis.com/auth/userinfo.email";

    public String retrieveAuthToken(Context context, Account account)
            throws GoogleAuthException, IOException {
        String token = GoogleAuthUtil.getToken(context, account, SCOPE);
        // Token needs to be clear so we make sure next time we get a brand new one. Otherwise this
        // may return a token that has already been used by the API and because it's a one time
        // token it won't work.
        GoogleAuthUtil.clearToken(context, token);
        return token;
    }

    public Observable<String> retrieveAuthTokenAsObservable(final Context context,
                                                            final Account account) {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                try {
                    subscriber.onNext(retrieveAuthToken(context, account));
                    subscriber.onCompleted();
                } catch (Throwable error) {
                    subscriber.onError(error);
                }
            }
        });
    }
}
