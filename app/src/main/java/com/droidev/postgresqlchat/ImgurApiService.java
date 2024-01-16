package com.droidev.postgresqlchat;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImgurApiService {
    @Multipart
    @POST("image")
    Call<ImgurResponse> uploadImage(
            @Header("Authorization") String authorization,
            @Part MultipartBody.Part image
    );
}
