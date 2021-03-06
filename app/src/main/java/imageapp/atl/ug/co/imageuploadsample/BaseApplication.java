package imageapp.atl.ug.co.imageuploadsample;

import android.app.Application;

import retrofit.RequestInterceptor;
import retrofit.RestAdapter;

/**
 * Created by dave on 5/22/2015.
 */
public class BaseApplication extends Application {

    //web service url...
    public static final String BASE_URL = "http://10.0.2.2:4041/api/";

    private NetworkInterceptor networkInterceptor;

    @Override
    public void onCreate() {
        super.onCreate();

        //the rest client...
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint(BASE_URL)
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .setRequestInterceptor(new RequestInterceptor() {
                    //interceptor to overrid the host name header for the application to localhost.
                    @Override
                    public void intercept(RequestFacade requestFacade) {
                        if (BASE_URL.contains("10.0.2.2"))
                            requestFacade.addHeader("Host", "localhost");
                    }
                })
                .build();

        //applying the interceptor..
        networkInterceptor = restAdapter.create(NetworkInterceptor.class);

    }


    public NetworkInterceptor getNetworkInterceptor() {
        return networkInterceptor;
    }
}
