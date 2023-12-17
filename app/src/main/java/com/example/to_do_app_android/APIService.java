package com.example.to_do_app_android;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers({
            "Content-Type: application/json; charset=utf8",
            "Accept: application/json"
    })
    @POST("login")
    Call<Void> userLogin(@Body LoginData loginData);

    @POST("user") // Endpoint para registo de user
    Call<Void> userRegister(@Body RegisterData registerData);

    // Novo método para buscar as tarefas do user
    @GET("task/user")
    Call<List<Task>> getUserTasks();
}
