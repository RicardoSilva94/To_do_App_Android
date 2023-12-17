package com.example.to_do_app_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;
    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonSignUp;

    private Retrofit retrofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);

        // Referenciando os elementos da interface
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonSignUp = findViewById(R.id.buttonSignUp);

        // Configurando o Retrofit com interceptor para adicionar o token de autorização
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request originalRequest = chain.request();
                Request.Builder requestBuilder = originalRequest.newBuilder()
                        .method(originalRequest.method(), originalRequest.body());

                // Obter o token do armazenamento local
                String token = getTokenFromLocalStorage();
                if (token != null && !token.isEmpty()) {
                    requestBuilder.addHeader("Authorization", token); // Adicione o token ao cabeçalho
                }

                Request modifiedRequest = requestBuilder.build();

                // Continua com a execução da requisição
                return chain.proceed(modifiedRequest);
            }
        });

        OkHttpClient client = httpClient.build();

        retrofit = new Retrofit.Builder()
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(client) // Define o cliente OkHttpClient com o interceptor no Retrofit
                .build();


        // Configurando o clique do botão Login
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                Log.d("1","1");

                // Criar um objeto LoginData com os dados de login
                LoginData loginData = new LoginData(username, password);

                Log.d("2","2");

                // Chamar o método de login da API usando Retrofit
                APIService apiService = retrofit.create(APIService.class);
                Call<Void> call = apiService.userLogin(loginData);

                Log.d("3","3");
                call.enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        Log.d("4","4");
                        if (response.isSuccessful()) {
                            Log.d("5","5");
                            String token = response.headers().get("Authorization");
                            if (token != null) {
                                Log.d("TokenReceived", "Token: " + token); // Adicionando um log para verificar o token recebido
                                saveTokenToLocalStorage(token);
                                Intent intent = new Intent(MainActivity.this, UserTasks.class);
                                startActivity(intent);
                            } else {
                                Log.e("LoginError", "Token is null");
                                Toast.makeText(MainActivity.this, "Falha no login", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                String errorBody = response.errorBody().string();
                                Log.e("LoginError", "Error Body: " + errorBody);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(MainActivity.this, "Falha no login", Toast.LENGTH_SHORT).show();
                        }
                    }


                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Log.e("RequestFailure", "Failed to execute the request: " + t.getMessage());
                        t.printStackTrace();
                        Toast.makeText(MainActivity.this, "Erro: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Configurando o clique do botão Sign Up
        buttonSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, SignUp.class);
                startActivity(intent);
            }
        });
    }

    // Método para salvar o token no armazenamento local
    private void saveTokenToLocalStorage(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("token", token);
        editor.apply();
    }

    // Método para obter o token do armazenamento local
    private String getTokenFromLocalStorage() {
        return sharedPreferences.getString("token", "");
    }
}

