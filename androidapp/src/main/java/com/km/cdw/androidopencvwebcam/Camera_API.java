package com.km.cdw.androidopencvwebcam;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import androidx.annotation.NonNull;

import java.util.ArrayList;


public class Camera_API {

    public static final String TAG = "Camera_API";


    private Activity mActivity;

    private CameraDevice mCameraDeivce;
    private CameraManager mCameraManager;
    private CameraCaptureSession mCameraCaptureSession;
    private CaptureRequest.Builder mCameraRequest;
    private CameraCharacteristics mCameraCharacter;

    private Handler mCameraHandler;
    private HandlerThread mCameraHandlerThread;

    //preview
    private Surface mPreviewSurface;
    private Surface mConvertSurface;

    private int mCameraPosition = 0;
    private int mWidth;
    private int mHeight;
    private int mFmt;

    public Camera_API(Activity activity) {
        mActivity = activity;
    }

    @SuppressLint("MissingPermission")
    public void openCamera() {
        Log.e(TAG, "openCamera");
        if (mCameraHandler == null) {
            mCameraHandlerThread = new HandlerThread("Camera_Handler");
            mCameraHandlerThread.start();

            mCameraHandler = new Handler(mCameraHandlerThread.getLooper());
        }


        mCameraManager = (CameraManager) mActivity.getSystemService(Context.CAMERA_SERVICE);

        try {
            String cameraId = mCameraManager.getCameraIdList()[mCameraPosition];
            mCameraCharacter = mCameraManager.getCameraCharacteristics(cameraId);
            mCameraManager.openCamera(cameraId, mCameraState, mCameraHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    public void setCameraParameter(Surface convertSurface, int width, int height, int pix_fmt, int fps, int position){
        mConvertSurface = convertSurface;

        mWidth = width;
        mHeight = height;
        mFmt = pix_fmt;
        mCameraPosition = position;
    }

    public void closeCamera(){

        if (null != mCameraCaptureSession) {
            mCameraCaptureSession.close();
            mCameraCaptureSession = null;
        }
        if (mCameraDeivce != null) {
            mCameraDeivce.close();
            mCameraDeivce = null;
        }
        if (mCameraHandlerThread != null) {
            mCameraHandlerThread.quitSafely();
            mCameraHandlerThread = null;
            mCameraHandler = null;
        }


    }

    private CameraDevice.StateCallback mCameraState = new CameraDevice.StateCallback(){

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
            Log.d(TAG, "CameraDeivce is not set");
            return;
        }

        try {
            ArrayList<Surface> previewSurfaceArr = new ArrayList<>();
            mCameraRequest = mCameraDeivce.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            if(mPreviewSurface != null) {
                mCameraRequest.addTarget(mPreviewSurface);
                previewSurfaceArr.add(mPreviewSurface);
            }
            if(mConvertSurface != null) {
                mCameraRequest.addTarget(mConvertSurface);
                previewSurfaceArr.add(mConvertSurface);
            }

            mCameraDeivce.createCaptureSession(previewSurfaceArr,
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured(CameraCaptureSession session) {
                            Log.d(TAG, "Create Session Success");
                            mCameraCaptureSession = session;
                            try {
                                mCameraCaptureSession.setRepeatingRequest(mCameraRequest.build(), null, mCameraHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onConfigureFailed(CameraCaptureSession session) {
                            Log.d(TAG, "Create Session Fail");

                        }
                    }, mCameraHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, " : createCameraSession() : " + e);
            e.printStackTrace();
        }
    }
    public Size compactCamSize() {
        CameraManager cameraManager = (CameraManager) mActivity.getSystemService(Activity.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacter = null;
        String cameraId = null;

        Size optimizedSize = null;

        try {
            cameraId = cameraManager.getCameraIdList()[mCameraPosition];
            cameraCharacter = cameraManager.getCameraCharacteristics(cameraId);
            Size[] cameraAvailableSizes = cameraCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(mFmt);
            for(Size s: cameraAvailableSizes) {
                Log.d(TAG, "Able SIZE : " + s);
            }
            int optWidth = mWidth;
            int optHeight = mHeight;

            for(Size s: cameraAvailableSizes) {
                if(s.getWidth() < optWidth && s.getHeight() < optHeight) {
                    optWidth = s.getWidth();
                    optHeight = s.getHeight();

                    return new Size(optWidth, optHeight);
                }
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera ERROR : " + e);
        }
        return null;


    }
}
