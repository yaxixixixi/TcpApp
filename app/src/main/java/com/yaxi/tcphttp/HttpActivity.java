package com.yaxi.tcphttp;

import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yaxi.tcphttp.http.Request;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class HttpActivity extends AppCompatActivity {

    private static final String TAG = HttpActivity.class.getSimpleName();
    private TextView show;
    private EditText content;

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http);
        show = findViewById(R.id.show);
        content = findViewById(R.id.content);
    }

    public void requestHttp(View view){
        String contentStr = content.getText().toString().trim();
        if (contentStr.equals("")){
            Toast.makeText(this, "没有请求内容", Toast.LENGTH_SHORT).show();
            return;
        }
        sendRequest(contentStr);
    }

    public void sendRequest(String contentStr){
        Request.sendRequest(contentStr, new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, final Response<String> response) {
                Log.i(TAG, "onResponse: "+response.body());

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String showText = show.getText().toString();
                        StringBuilder builder = new StringBuilder(showText);
                        builder.append(">>new respond:\n");
                        builder.append(response.body());
                        show.setText(builder.toString());
                    }
                });
            }

            @Override
            public void onFailure(Call<String> call, final Throwable t) {
                Log.e(TAG, "onFailure: ", t);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        String showText = show.getText().toString();
                        StringBuilder builder = new StringBuilder(showText);
                        builder.append(">>new respond:\n");
                        builder.append(t.toString());
                        show.setText(builder.toString());
                    }
                });
            }
        });
    }




}
