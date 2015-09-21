package io.ribot.app.data.remote;


import retrofit.GsonConverterFactory;
import retrofit.Retrofit;
import retrofit.RxJavaCallAdapterFactory;

public class RetrofitHelper {

    public RibotsService newRibotsService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(RibotsService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(RibotsService.class);
    }

}
