package com.km.cdw.androidopencvwebcam.Network.SocketIo;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class SocketIoManager {
    public static final String TAG = "SocketIoManager";
    private static final String URL = "http://192.168.10.100:3000";

    private static final int SUCCESS = 0;
    private static final int FAIL = 1;

    private int mSleepTime = 512;
    private Socket socket;
    private Handler mHandler;
    private HandlerThread mHandlerThread;

    //if connected fail, try connect repeat until connect success
    private Runnable mTask = new Runnable() {
        @Override
        public void run() {

            try {
                while (!socket.connected()) {
                    socket.connect();
                    Thread.sleep(mSleepTime);
                }
            } catch (InterruptedException e) {
                Log.d(TAG, "Thread OUT");
            }
        }
    };

    public static SocketIoManager getInstance() {
        return new SocketIoManager();
    }

    private SocketIoManager() {
        try {
            socket = IO.socket(URL);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
    public void onListener(String event, Emitter.Listener listener) {
        socket.on(event, listener);
    }
    public void onConnectListener(Emitter.Listener listener) {
        socket.on(Socket.EVENT_CONNECT, listener);
    }
    public void onDisconnectListener(Emitter.Listener listener) {
        socket.on(Socket.EVENT_DISCONNECT, listener);
    }
    public void requestPostSettingData(Map<String, String> data) {
        JSONObject obj = new JSONObject();
        for(String key: data.keySet()) {
            try {
                obj.put(key, data.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        socket.emit("request post setting data", obj);
    }
    public void requestGetSettingData() {
        socket.emit("request get setting data");
    }
    public void requestPostPicture(Bitmap bitmap) {
        if(bitmap == null) return;
        JSONObject obj = new JSONObject();

        //encode picture
        try (ByteArrayOutputStream pictureByteStream = new ByteArrayOutputStream()) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 30, pictureByteStream);
            byte[] pictureRowBytes = pictureByteStream.toByteArray();
            String encodedPicture = Base64.encodeToString(pictureRowBytes, Base64.DEFAULT);

            obj.put("picture_data", encodedPicture);
            obj.put("status", SUCCESS);
            socket.emit("request send picture",obj);
        }catch (IOException | JSONException e) {
            e.printStackTrace();
        }
    }
    public void connect() {
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
        mHandler.post(mTask);
    }
    public void disconnect() {
        if(socket.connected()) {
            socket.disconnect();
        }
        mHandlerThread.quitSafely();
        mHandlerThread = null;
        mHandler = null;
    }
}
