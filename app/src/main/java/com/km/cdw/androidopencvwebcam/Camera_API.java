package com.km.cdw.androidopencvwebcam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class Camera_API {

    final String TAG = "Camera_API";
    Activity mActivity;

    CameraDevice mCameraDeivce;
    CameraManager mCameraManager;
    CameraCaptureSession mCameraCaptureSession;
    CaptureRequest.Builder mCameraRequest;
    CameraCharacteristics mCameraCharacter;

    Handler mCameraHandler;
    HandlerThread mCameraHandlerThread;


    //preview
    Surface mPreviewSurface;
    Surface mConvertSurface;

    private int mCameraPosition = 0;
    private final TextureView.SurfaceTextureListener mSurfaceTextureListener
            = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
//            configureTransform(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

    };
    private int mWidth;
    private int mHeight;
    private int mFmt;

    public Camera_API(Activity activity) {
        mActivity = activity;
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {

        if (mCameraHandler == null) {
            mCameraHandlerThread = new HandlerThread("Camera_Handler");
            mCameraHandlerThread.start();

            mCameraHandler = new Handler(mCameraHandlerThread.getLooper());
        }


        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = mCameraManager.getCameraIdList()[mCameraPosition];
            mCameraCharacter = mCameraManager.getCameraCharacteristics(cameraId);
            Log.e(TAG, "Able SIZE : " + mCameraCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(mFmt));
            if(mCameraDeivce == null) {
                mCameraManager.openCamera(cameraId, mCameraState, null);
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void setCameraParameter(Surface previewSurface, Surface convertSurface, int width, int height, int pix_fmt, int fps){
        mPreviewSurface = previewSurface;
        mConvertSurface = convertSurface;
        mWidth = width;
        mHeight = height;
        mFmt = pix_fmt;
    }

    public void closeCamera(){
        if(mCameraDeivce != null) {
            mCameraDeivce.close();
            mCameraDeivce = null;
        }
        if(mCameraHandlerThread != null) {
            mCameraHandlerThread.quitSafely();
            mCameraHandlerThread = null;
            mCameraHandler = null;
        }

    }

    CameraDevice.StateCallback mCameraState = new CameraDevice.StateCallback(){

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            mCameraDeivce = camera;
            createRequest();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {

        }
    };

    private void createRequest(){
        if (mCameraDeivce == null) {
            Log.e(TAG, "CameraDeivce is not set");
            return;
        }
        if(mPreviewSurface == null) return;

        try {
            ArrayList<Surface> previewSurfaceArr = new ArrayList<>();
            mCameraRequest = mCameraDeivce.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mCameraRequest.addTarget(mPreviewSurface);
            if(mConvertSurface != null)
                mCameraRequest.addTarget(mConvertSurface);
            previewSurfaceArr.add(mPreviewSurface);
            if(mConvertSurface != null)
                previewSurfaceArr.add(mConvertSurface);

            mCameraDeivce.createCaptureSession(previewSurfaceArr,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Log.e(TAG, "Create Session Success");
                            mCameraCaptureSession = session;
                            try {
                                mCameraCaptureSession.setRepeatingRequest(mCameraRequest.build(), null, mCameraHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.e(TAG, "Create Session Fail");

                        }
                    }, mCameraHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, " : createCameraSession() : " + e);
            e.printStackTrace();
        }
    }
    public Size compactCamSize(CameraCharacteristics character) {
        Size[] ableSizes = character.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(mFmt);
        int resultW = 0,resultH = 0;
        for(Size s: ableSizes) {
            if(mWidth < s.getWidth() || mHeight < s.getHeight()) {
                break;
            }
            resultW = s.getWidth();
            resultH = s.getHeight();
        }
        return new Size(resultW, resultH);
    }
}
