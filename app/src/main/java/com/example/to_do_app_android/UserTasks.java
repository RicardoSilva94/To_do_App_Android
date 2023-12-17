package com.example.to_do_app_android;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.SharedPreferences;

public class UserTasks extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private String token;
    private LinearLayout activitiesLayout;
    private Button addTaskButton;

    private List<Task> tasksList; // Sua lista de tarefas

    private Retrofit retrofit;

    private APIService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_tasks);

        Log.d("6","6");
        sharedPreferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        token = sharedPreferences.getString("token", "");

        activitiesLayout = findViewById(R.id.activitiesLayout);
        addTaskButton = findViewById(R.id.addTaskButton);

        addTaskButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UserTasks.this, AddTaskActivity.class);
                startActivity(intent);
            }
        });

        // Cria um interceptor para adicionar o token ao cabeçalho 'Authorization'
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.addInterceptor(logging);
        httpClient.addInterceptor(chain -> {
            Request originalRequest = chain.request();
            Request.Builder requestBuilder = originalRequest.newBuilder()
                    .method(originalRequest.method(), originalRequest.body());

            // Adiciona o token ao cabeçalho 'Authorization'
            if (token != null && !token.isEmpty()) {
                requestBuilder.addHeader("Authorization",  token);
                Log.d("7","7");
            }

            Request modifiedRequest = requestBuilder.build();
            return chain.proceed(modifiedRequest);
        });

        OkHttpClient client = httpClient.build();
        Log.d("8","8");
        // Inicializa o Retrofit com o cliente OkHttpClient e outras configurações
        retrofit = new Retrofit.Builder()
                .client(client)
                .baseUrl("http://10.0.2.2:8080/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        Log.d("9","9");
        apiService = retrofit.create(APIService.class);


        Call<List<Task>> tasksCall = apiService.getUserTasks();
        Log.d("10","10");
        tasksCall.enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    tasksList = response.body();
                    Log.d("UserTasks", "Número de tarefas recebidas: " + tasksList.size()); // Verifica o tamanho da lista
                    Log.d("TokenSent", "Token: " + token);
                    displayTasks(); // Exibe as tarefas recebidas
                } else {
                    Log.d("TokenSent", "Token: " + token);
                    Log.e("UserTasks", "Falha na obtenção das tarefas. Código de resposta: " + response.code() + " Mensagem de erro: " + response.message());
                    // Trate a falha na obtenção das tarefas
                    // Imprime a solicitação no Logcat
                    Log.d("Request", tasksCall.request().toString());
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                Log.e("UserTasks", "Erro na requisição: " + t.getMessage(),t);
                t.printStackTrace();
            }
        });
    }

    private void displayTasks() {
        for (Task task : tasksList) {
            // Cria um layout horizontal para conter o ID e a descrição da tarefa
            LinearLayout taskLayout = new LinearLayout(this);
            taskLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            taskLayout.setOrientation(LinearLayout.HORIZONTAL);

            // Cria um TextView para o ID da tarefa
            TextView idTextView = new TextView(this);
            idTextView.setText("ID: " + String.valueOf(task.getId()));
            idTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            idTextView.setPadding(16, 0, 16, 0); // Adicione padding para espaçamento

            // Cria um TextView para a descrição da tarefa
            TextView descriptionTextView = new TextView(this);
            descriptionTextView.setText("Description: " + task.getDescription());
            descriptionTextView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            descriptionTextView.setPadding(16, 0, 16, 0); // Adicione padding para espaçamento

            // Adiciona os TextViews ao layout da tarefa
            taskLayout.addView(idTextView);
            taskLayout.addView(descriptionTextView);

            // Adiciona o layout da tarefa ao LinearLayout principal
            activitiesLayout.addView(taskLayout);
            }
        }
    }


