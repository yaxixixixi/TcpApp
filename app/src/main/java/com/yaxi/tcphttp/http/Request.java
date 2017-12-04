package com.yaxi.tcphttp.http;

import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by Administrator on 2017/11/29.
 */

public class Request {

    public static final String BASE_URL = "http://192.168.0.138:32325/";


    private static Retrofit retrofit;
    private static ServerApi ser;


    private static final String TAG = Request.class.getSimpleName();

    static {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .build();
        ser = retrofit.create(ServerApi.class);
    }


    public static void sendRequest(String content, Callback<String> callback) {
        Call<String> call = ser.sendRequest(content);
        call.enqueue(callback);
    }

    public static void sendRequest2(String content,String content2, Callback<String> callback) {
        Call<String> call = ser.sendRequest2(content,content2);
        call.enqueue(callback);
    }
}
