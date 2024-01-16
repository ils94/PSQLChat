package com.droidev.postgresqlchat;

import com.google.gson.annotations.SerializedName;

public class ImgurResponse {
    @SerializedName("data")
    private ImgurData data;

    public ImgurData getData() {
        return data;
    }
}

