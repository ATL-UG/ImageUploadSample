package imageapp.atl.ug.co.imageuploadsample;

import retrofit.Callback;
import retrofit.http.Headers;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by dave on 5/22/2015.
 */
public interface NetworkInterceptor {

    @Multipart
    @Headers({"Accept: application/json"})
    @POST("/values")
    String postValues(@Part("value") String value, @Part("file")TypedFile file);

}
