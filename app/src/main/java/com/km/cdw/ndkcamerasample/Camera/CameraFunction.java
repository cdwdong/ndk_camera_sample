package com.km.cdw.ndkcamerasample.Camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import com.km.cdw.ndkcamerasample.CameraFragment;
import com.km.cdw.ndkcamerasample.MainActivity;
import com.km.cdw.ndkcamerasample.Util.ImageSaver;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;


public class CameraFunction {

    private static final String TAG = CameraFunction.class.getCanonicalName();

    private int mHeight;
    private int mWidth;
    private Activity mMainActivity;
    private CameraManager mCameraManager;
    private CameraDevice mCameraDeivce;
    private CameraDevice.StateCallback mCameraDeviceCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice onOpened");
            mCameraDeivce = camera;
            try {
                createCameraSession();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            Log.e(TAG, "CameraDevice onDisconnected");
            if(mCameraDeivce == null) return;
            mCameraDeivce.close();
            mCameraDeivce = null;

        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            Log.e(TAG, "CameraDevice onError");
            if(mCameraDeivce == null) return;
            mCameraDeivce.close();
            mCameraDeivce = null;

        }
    };
    private CameraCaptureSession mCaptureSession;
    private ImageReader mImageReader;
    private byte[] mYuvBuffer;
    private ImageReader.OnImageAvailableListener mImageReaderListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Log.e(TAG, ": ImageReader has Image taken");
            Image tempImage = reader.acquireLatestImage();
            if(tempImage != null){
                int img_width = tempImage.getWidth();
                int img_height = tempImage.getHeight();

                ByteBuffer yByteBuffer = tempImage.getPlanes()[0].getBuffer();
                ByteBuffer uByteBuffer = tempImage.getPlanes()[1].getBuffer();
                ByteBuffer vByteBuffer = tempImage.getPlanes()[2].getBuffer();


                int y_stride = tempImage.getPlanes()[0].getRowStride();
                int u_stride = tempImage.getPlanes()[1].getRowStride();
                int v_stride = tempImage.getPlanes()[2].getRowStride();

                int ySize = yByteBuffer.remaining();
                int uSize = uByteBuffer.remaining();
                int vSize = vByteBuffer.remaining();

//                //y copy
//                yByteBuffer.get(mYuvBuffer, 0, ySize);
//                uByteBuffer.get(mYuvBuffer, ySize, uSize);
//                mYuvBuffer[ySize+uSize-1] = mYuvBuffer[ySize+uSize-2];

                Log.e("imagesave","U_size : "+uSize+"  uStride: "+u_stride + "ySize : "+ySize);
                yByteBuffer.get(mYuvBuffer, 0, ySize);
                int driveIndex = 0;
                byte[] uResultBytes = new byte[uSize/2];
                byte[] vResultBytes = new byte[uSize/2];

                // uvuvuvuv.... -> uuuu....vvvv.... Transforming
                //메모리가 더들더라도 for문 하나라서 시간절약
                for (int i = 0, j = 0;driveIndex < uSize - 1; driveIndex++) {
                    if(driveIndex % 2 == 0) {
                        uByteBuffer.get(uResultBytes, i,1);
                        i++;
                    }else {
                        uByteBuffer.get(vResultBytes, j,1);
                        j++;
                    }
                }
                System.arraycopy(uResultBytes, 0, mYuvBuffer, ySize, uSize/2);
                System.arraycopy(vResultBytes, 0, mYuvBuffer, ySize + uSize/2, uSize/2);
                mYuvBuffer[ySize + uSize/2 - 1] =  mYuvBuffer[ySize + uSize/2 - 2];
                mImageSaver.save(mYuvBuffer);
                tempImage.close();
            }

        }
    };
    private Surface mPreviewSurface;
    private int mCameraPosition = 0;
    private CameraCharacteristics mCameraCharacter;
    private Handler mHandler;
    private ImageSaver mImageSaver;

    private CameraFunction(Activity owner, Handler handler) {
        mMainActivity = owner;
        mHandler = handler;

        mYuvBuffer   = new byte[mWidth*mHeight*3/2];
        mImageSaver = new ImageSaver(mMainActivity);

        mCameraManager = (CameraManager) mMainActivity.getSystemService(Context.CAMERA_SERVICE);

    }
    public static CameraFunction createNewCamera(Activity owner, Handler handler) {
        if(handler == null) {
            Log.e(TAG, "Handler is not set");
            return null;
        }
        if(owner == null) {
            Log.e(TAG, "Activity is not set");
            return null;
        }
        return new CameraFunction(owner, handler);
    }

    public void openCamera(Surface preViewSurface, int cameraPosition, int outWidth, int outHeight) {
        mCameraPosition = cameraPosition;
        mPreviewSurface = preViewSurface;
        mWidth = outWidth; mHeight = outHeight;
        mImageReader = ImageReader.newInstance(mWidth,mHeight, CameraFragment.C_IMAGE_FORMAT,2);
        mImageReader.setOnImageAvailableListener(mImageReaderListener,mHandler);
        try {
            String cameraId = mCameraManager.getCameraIdList()[mCameraPosition];
            mCameraCharacter = mCameraManager.getCameraCharacteristics(cameraId);

            Log.e(TAG, " : Camera Hardware Level is supported? : "
                    + isHardwareLevelSupported(mCameraCharacter, CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3));
            Log.e(TAG, cameraId + " :Camera is Connected");

            mCameraManager.openCamera(cameraId, mCameraDeviceCallback, mHandler);
        } catch (CameraAccessException | SecurityException e) {
            Log.e(TAG, " : openCamera() : " + e);
            e.printStackTrace();
        }

    }
    private void showCameraPreview() {
        try {
            if (mCameraDeivce == null) {
                Log.e(TAG, "CameraDeivce is not set");
                return;
            }
            if (mCaptureSession == null) {
                Log.e(TAG, "CaptureSession is not set");
                return;
            }

            CaptureRequest.Builder requestBuilder = mCameraDeivce.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            requestBuilder.addTarget(mPreviewSurface);
            requestBuilder.set(CaptureRequest.CONTROL_MODE, CaptureRequest.CONTROL_MODE_AUTO);

            mCaptureSession.setRepeatingRequest(requestBuilder.build(), null, mHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, " : showCameraPreview() : " + e);
            e.printStackTrace();
        }

    }
    private void createCameraSession() {
        if (mCameraDeivce == null) {
            Log.e(TAG, "CameraDeivce is not set");
            return;
        }
        try {
            mCameraDeivce.createCaptureSession(Arrays.asList(mPreviewSurface, mImageReader.getSurface()),
                    new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Create Session Success");
                    mCaptureSession = session;
                    showCameraPreview();

                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                    Log.e(TAG, "Create Session Fail");

                }
            }, mHandler);
        } catch (CameraAccessException e) {
            Log.e(TAG, " : createCameraSession() : " + e);
            e.printStackTrace();
        }
    }
    public void takePicture() {
        if(mCaptureSession == null) {
            Log.e(TAG, "takePicture() : mCaptureSeesion is null");
            return;
        }
        try {
            CaptureRequest.Builder requestBuilder = mCameraDeivce.createCaptureRequest(CameraDevice.TEMPLATE_VIDEO_SNAPSHOT);
            requestBuilder.addTarget(mImageReader.getSurface());
//            requestBuilder.set(CaptureRequest.CONTROL_MODE,CaptureRequest.CONTROL_MODE_AUTO);
            requestBuilder.set(CaptureRequest.CONTROL_AF_MODE,CaptureRequest.CONTROL_AF_MODE_AUTO);
            mCaptureSession.capture(requestBuilder.build(), null, mHandler);

            //back to PreView mode
            mCaptureSession.stopRepeating();
            showCameraPreview();

        } catch (CameraAccessException e) {
            Log.e(TAG, " : takePicture() : " + e);
            e.printStackTrace();
        }
    }

    public void closeCamera() {

        if(mCaptureSession != null) mCaptureSession.close();
        if(mCameraDeivce != null) mCameraDeivce.close();
        if(mImageReader != null) mImageReader.close();
    }
    private File createNewFile() {
        return new File(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/Camera2SampleApp_" + new Date(System.currentTimeMillis()) + ".yuv");
    }

    boolean isHardwareLevelSupported(CameraCharacteristics characteristics, int requiredLevel) {
        final int[] sortedHwLevels = {
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LEGACY,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_LIMITED,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_FULL,
                CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL_3
        };
        int deviceLevel = characteristics.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
        Log.e(TAG, "Device Camera Level : " + deviceLevel);
        if (requiredLevel == deviceLevel) {
            return true;
        }

        for (int sortedlevel : sortedHwLevels) {
            if (sortedlevel == requiredLevel) {
                return true;
            } else if (sortedlevel == deviceLevel) {
                return false;
            }
        }
        return false;
    }
    public Size[] getCameraSupportSizes(int cameraPosition) {
        try  {
            String cameraId = mCameraManager.getCameraIdList()[cameraPosition];
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameraId);


            Size[] sizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(CameraFragment.C_IMAGE_FORMAT);

            if(MainActivity.DEBUG_MODE) {
                if(sizes == null) {
                    Log.e(TAG, ": getOptimizeSize() : no available camera size");
                    return null;
                }
                for(Size s : sizes) {
                    Log.e(TAG,"available size : " + s);
                }
            }

            return sizes;
        }catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;

    }

}
