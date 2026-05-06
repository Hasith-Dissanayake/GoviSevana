package lk.javainstitute.govisevana.helper;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.IOException;

import lk.javainstitute.govisevana.model.ApiService;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import org.json.JSONObject;

public class ImageUploadHelper {

    private static final String BASE_URL = "https://api.cloudinary.com/v1_1/dfqobmeaj/";
    private static final String UPLOAD_PRESET = "ml_default";


    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        String filePath = getRealPathFromURI(context, imageUri);
        if (filePath == null) {
            callback.onFailure("Invalid file path!");
            return;
        }

        File file = new File(filePath);
        RequestBody requestFile = RequestBody.create(MediaType.parse(getMimeType(context, imageUri)), file);
        MultipartBody.Part body = MultipartBody.Part.createFormData("file", file.getName(), requestFile);

        RequestBody uploadPreset = RequestBody.create(MediaType.parse("text/plain"), UPLOAD_PRESET);

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        Call<ResponseBody> call = apiService.uploadImage(body, uploadPreset);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        //  Image URL
                        String imageUrl = jsonObject.getString("secure_url");
                        callback.onSuccess(imageUrl);
                    } else {
                        callback.onFailure("Upload failed: " + response.errorBody().string());
                    }
                } catch (IOException | org.json.JSONException e) {
                    callback.onFailure("Error parsing response: " + e.getMessage());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                callback.onFailure("Cloudinary Upload Error: " + t.getMessage());
            }
        });
    }


    private static String getRealPathFromURI(Context context, Uri uri) {
        Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
        if (cursor == null) return uri.getPath();
        cursor.moveToFirst();
        int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        String filePath = index != -1 ? cursor.getString(index) : null;
        cursor.close();
        return filePath;
    }


    private static String getMimeType(Context context, Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
            mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return mimeType;
    }


    public interface UploadCallback {
        void onSuccess(String imageUrl);
        void onFailure(String errorMessage);
    }
}
