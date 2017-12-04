package com.yaxi.tcphttp.http;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

/**
 * Created by Administrator on 2017/11/29.
 */

public interface ServerApi {



    @GET("{path}")
    Call<String> sendRequest(@Path("path") String content);


    @GET("{path}/{path2}")
    Call<String> sendRequest2(@Path("path") String content,@Path("path2") String content2);

}
