package com.droidev.postgresqlchat;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ImgurUploader {

    private static final String IMGUR_API_BASE_URL = "https://api.imgur.com/3/";
    private static final String IMGUR_CLIENT_ID = "API KEY"; // Replace with your Imgur API key

    public static void uploadImage(Context context, Uri imageUri, UploadCallback callback) {
        new UploadImageAsyncTask(context, imageUri, callback).execute();
    }

    private static class UploadImageAsyncTask extends AsyncTask<Void, Void, String> {
        @SuppressLint("StaticFieldLeak")
        private final Context context;
        private final Uri imageUri;
        private final UploadCallback callback;

        public UploadImageAsyncTask(Context context, Uri imageUri, UploadCallback callback) {
            this.context = context;
            this.imageUri = imageUri;
            this.callback = callback;
        }

        @Override
        protected String doInBackground(Void... voids) {
            File imageFile = new File(Objects.requireNonNull(imageUri.getPath()));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(IMGUR_API_BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(getOkHttpClient(context))
                    .build();

            ImgurApiService imgurApiService = retrofit.create(ImgurApiService.class);

            RequestBody requestBody = RequestBody.create(MediaType.parse("image/*"), imageFile);

            MultipartBody.Part filePart = MultipartBody.Part.createFormData("image", imageFile.getName(), requestBody);

            Call<ImgurResponse> call = imgurApiService.uploadImage("Bearer " + IMGUR_CLIENT_ID, filePart);
            try {
                retrofit2.Response<ImgurResponse> response = call.execute();
                if (response.isSuccessful() && response.body() != null) {
                    String imageUrl = response.body().getData().getLink();
                    Log.d("ImgurUploader", "Image uploaded successfully. Image link: " + imageUrl);
                    return imageUrl; // Return the image URL
                } else {
                    Log.e("ImgurUploader", "Failed to upload image. Response code: " + response.code());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if (callback != null) {
                callback.onImageUploaded(imageUrl);
            }
        }
    }

    private static OkHttpClient getOkHttpClient(Context context) {

        TinyDB tinyDB = new TinyDB(context);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(chain -> {
            Request original = chain.request();
            Request.Builder requestBuilder = original.newBuilder()
                    .header("Authorization", "Client-ID " + tinyDB.getString("ImgurAPI"));

            Request request = requestBuilder.build();
            return chain.proceed(request);
        });

        httpClient.addInterceptor(new okhttp3.logging.HttpLoggingInterceptor().setLevel(okhttp3.logging.HttpLoggingInterceptor.Level.BODY));

        return httpClient.build();
    }

    public interface UploadCallback {
        void onImageUploaded(String imageUrl);
    }
}

