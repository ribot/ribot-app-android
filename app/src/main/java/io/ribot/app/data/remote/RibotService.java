package io.ribot.app.data.remote;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.List;

import io.ribot.app.data.model.CheckIn;
import io.ribot.app.data.model.CheckInRequest;
import io.ribot.app.data.model.Ribot;
import io.ribot.app.data.model.Venue;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;
import retrofit.http.Body;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.POST;
import rx.Observable;

public interface RibotService {

    String ENDPOINT = "https://api.ribot.io/";
    String AUTH_HEADER = "Authorization";

    @POST("auth/sign-in")
    Observable<SignInResponse> signIn(@Body SignInRequest signInRequest);

    @GET("venues")
    Observable<List<Venue>> getVenues(@Header(AUTH_HEADER) String authorization);

    @POST("check-ins")
    Observable<CheckIn> checkIn(@Header(AUTH_HEADER) String authorization,
                                @Body CheckInRequest checkInRequest);


    /******** Instance class that sets up a new ribot services *******/
    class Instance {

        public static RibotService newRibotService() {
            Gson gson = new GsonBuilder()
                    .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                    .create();
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(RibotService.ENDPOINT)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                    .build();
            return retrofit.create(RibotService.class);
        }
    }

    class Util {
        // Build API authorization string from a given access token.
        public static String buildAuthorization(String accessToken) {
            return "Bearer " + accessToken;
        }
    }

    /******** Specific request and response models ********/
    class SignInRequest {
        public String googleAuthorizationCode;

        public SignInRequest(String googleAuthorizationCode) {
            this.googleAuthorizationCode = googleAuthorizationCode;
        }
    }

    class SignInResponse {
        public String accessToken;
        public Ribot ribot;
    }

}
