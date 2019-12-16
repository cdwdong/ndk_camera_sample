package com.km.cdw.androidopencvwebcam.Preference;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.util.Map;

public class SharedPreferenceController {
    public static final String TAG = "SharedPreferenceController";
    private final Context mContext;
    private final SharedPreferences mSharedPreferences;
    public SharedPreferenceController(Context context) {
        mContext = context;
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
    }
    public void getSettingData(Map<String, String> data) {
        data.put("filter", mSharedPreferences.getString("filter", "filter_default"));
        data.put("detect", mSharedPreferences.getString("detect", "detect_default"));
        Log.d(TAG, "CUR PREFERENCE : " + data.toString());
    }
    public void putSettingData(Map<String, String> data) {
        mSharedPreferences.edit()
                .putString("filter", data.get("filter"))
                .putString("detect", data.get("detect"))
                .apply();
        Log.d(TAG, "CUR PREFERENCE : " + mSharedPreferences.getAll());

    }
}
