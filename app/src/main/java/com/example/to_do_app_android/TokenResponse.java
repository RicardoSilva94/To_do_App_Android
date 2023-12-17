package com.example.to_do_app_android;

import com.google.gson.annotations.SerializedName;

public class TokenResponse {
    @SerializedName("Authorization")
    private String token;

    public String getToken() {
        return token;
    }
}
