package com.km.cdw.androidopencvwebcam.Network.Http;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class HttpReqSendTask implements Runnable {
    public static final String TAG = "HttpReqSendTask";
    public static final int GET_SETTING = 0;
    public static final int POST_SETTING = 1;

    private static final String BASE_PATH = "http://192.168.10.100:3000/";
    private SettingService mSettingService;
    private Context mContext;
    private int mReqType;
    private GsonSettingType mReqData;

    private HttpReqSendTask(Context context, SettingService settingService, int reqType, GsonSettingType data) {
        mContext = context;
        mSettingService = settingService;
        mReqType = reqType;
        mReqData = data;
    }


    @Override
    public void run() {
        if(mReqType == GET_SETTING) {
            Call<GsonSettingType> call = mSettingService.getSettingValues();
            Response<GsonSettingType> res = null;
            GsonSettingType data = null;
            SharedPreferences preferences = null;

            try {
                res = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Occurred Exception : " + e);
                return;
            }

            data = res.body();
            if(data == null) {
                Log.e(TAG, "NO DATA");
                return;
            }
            Log.e(TAG, "GET REQ: data.filter : " + data.filter + " data.detect : " + data.detect);
            if(data.filter != null && data.detect != null) {
                preferences = PreferenceManager.getDefaultSharedPreferences(mContext);
                preferences.edit()
                        .putString("filter", data.filter)
                        .putString("detect", data.detect)
                        .apply();
            }
        } else if(mReqType == POST_SETTING) {
            Call<GsonSettingType> call = mSettingService.postSettingValues(mReqData);
            Response<GsonSettingType> res = null;
            GsonSettingType data = null;
            SharedPreferences preferences = null;

            try {

                res = call.execute();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Occurred Exception : " + e);
                return;
            }

            data = res.body();
            if(data == null) {
                Log.e(TAG, "NO DATA");
                return;
            }
            Log.e(TAG, "POST REQ: data.result : " + data.result);
        }
    }
    public static void executeGetReqTask(Context context) {
        HttpReqSendTask.Builder builder = new HttpReqSendTask.Builder()
                .setContext(context)
                .setReqType(HttpReqSendTask.GET_SETTING)
                .setReqData(new GsonSettingType());
        AsyncTask.execute(builder.build());
    }
    public static void executePostReqTask(Context context, GsonSettingType data) {
        HttpReqSendTask.Builder builder = new HttpReqSendTask.Builder()
                .setContext(context)
                .setReqType(HttpReqSendTask.POST_SETTING)
                .setReqData(data);
        AsyncTask.execute(builder.build());
    }

    public static class Builder {
        private SettingService mSettingService = null;
        private Context mContext = null;
        private int mReqType = -1;
        private GsonSettingType mReqData;

        public Builder() {
            Retrofit retrofit = new Retrofit.Builder().baseUrl(BASE_PATH)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            mSettingService = retrofit.create(SettingService.class);
        }
        public Builder setContext(Context context) {
            mContext = context;
            return this;
        }
        public Builder setReqType(int reqType) {
            mReqType = reqType;
            return this;
        }
        public Builder setReqData(GsonSettingType reqData) {
            mReqData = reqData;
            return this;
        }
        public HttpReqSendTask build() {
            if(mContext == null || mSettingService == null || mReqType == -1 || mReqData == null)
                throw new IllegalArgumentException("build error: MISSING PROPERTY FOR MAKING TASK");
            return new HttpReqSendTask(mContext,mSettingService,mReqType,mReqData);
        }

    }
}
