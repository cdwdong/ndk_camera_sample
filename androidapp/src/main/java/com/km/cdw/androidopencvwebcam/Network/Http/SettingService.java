package com.km.cdw.androidopencvwebcam.Network.Http;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface SettingService {
    @GET("/setting")
    Call<GsonSettingType> getSettingValues();
    @POST("/setting")
    Call<GsonSettingType> postSettingValues(@Body GsonSettingType reqData);
}