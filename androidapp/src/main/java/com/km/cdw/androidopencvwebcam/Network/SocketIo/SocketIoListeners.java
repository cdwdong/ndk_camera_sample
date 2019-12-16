package com.km.cdw.androidopencvwebcam.Network.SocketIo;

import android.util.Log;

import com.km.cdw.androidopencvwebcam.Preference.SharedPreferenceController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import io.socket.emitter.Emitter;

public class SocketIoListeners {
    public static final String TAG = "SocketIoListeners";
    private final SharedPreferenceController mController;

    public SocketIoListeners(SharedPreferenceController manager) {
        mController = manager;
    }
    private Emitter.Listener onConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
        }
    };
    private Emitter.Listener onDisconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
    private Emitter.Listener responseSettingDataListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = (JSONObject)args[0];
            Map<String, String> data = new HashMap<>();
            String key;

            try {
                key = "filter";
                data.put(key, obj.getString(key));
                key = "detect";
                data.put(key, obj.getString(key));
                mController.putSettingData(data);
                Log.d(TAG, "receive data: " + data.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.e(TAG, "OCCURRED EXCEPTION : " + e);

            }
        }
    };

    public Emitter.Listener getOnConnectListener() {
        return onConnectListener;
    }

    public Emitter.Listener getOnDisconnectListener() {
        return onDisconnectListener;
    }

    public Emitter.Listener getResponseSettingDataListener() {
        return responseSettingDataListener;
    }
}
