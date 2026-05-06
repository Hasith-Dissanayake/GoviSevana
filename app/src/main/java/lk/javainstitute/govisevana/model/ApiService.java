package lk.javainstitute.govisevana.model;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface ApiService {
    @Multipart
    @POST("image/upload")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part file,
            @Part("upload_preset") RequestBody uploadPreset
    );
}
