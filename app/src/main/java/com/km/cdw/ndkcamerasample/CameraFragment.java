package com.km.cdw.ndkcamerasample;


import android.graphics.ImageFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;

import com.km.cdw.ndkcamerasample.Camera.CameraFunction;
import com.km.cdw.ndkcamerasample.Camera.ICameraEventCallback;
import com.km.cdw.ndkcamerasample.Camera.UI.CameraView;
import com.km.cdw.ndkcamerasample.Camera.UI.TakePictureButton;


/**
 * A simple {@link Fragment} subclass.
 */
public class CameraFragment extends Fragment {

    public static final int C_IMAGE_FORMAT = ImageFormat.YUV_420_888;
    public final String TAG = this.getClass().getName();

    private TakePictureButton mButton;
    private CameraView mCameraView;
    private CameraFunction mCameraFunction;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    private int mCameraPosition;
    private int mOutWidthSize, mOutHeightSize;

    public CameraFragment() {
        // Required empty public constructor
        mCameraPosition = 1;
        mOutWidthSize = 1080; mOutHeightSize = 1920;
    }

    private void startCameraHandlerThread() {
        Log.e(TAG, "Start Camera Thread");
        mHandlerThread = new HandlerThread("Camera_Background_Thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());
    }
    private void stopCameraHandlerThread() {
        Log.e(TAG, "Stop Camera Thread");
        mHandlerThread.quitSafely();
        try {
            mHandlerThread.join();
            mHandlerThread = null;
            mHandler = null;
        } catch (InterruptedException e) {
            Log.e(TAG, " : stopCameraHandlerThread() : " + e);
            e.printStackTrace();
        }
    }
    public Size optimizeSize(int targetWidth, int targetHeight) {
        if(mCameraFunction == null) return null;
        Size[] sizes = mCameraFunction.getCameraSupportSizes(mCameraPosition);
        int optimizedWidth = 0, optimizedHeight = 0;

        for(Size s : sizes) {
            //임시로 반대임
            if(s.getHeight() <= targetWidth && s.getWidth() <= targetHeight)  {
                optimizedWidth = s.getWidth();
                optimizedHeight = s.getHeight();
                break;
            }

        }
        return new Size(optimizedWidth, optimizedHeight);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_camera, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mButton = view.findViewById(R.id.take_button);
        mCameraView = view.findViewById(R.id.camera_view);
        mCameraView.setCameraCallback(mCameraCallback);
    }

    @Override
    public void onResume() {
        super.onResume();
        startCameraHandlerThread();
        mCameraFunction = CameraFunction.createNewCamera(getActivity(), mHandler);
        mCameraView.initCameraView(mHandler);

    }

    public ICameraEventCallback mCameraCallback = new ICameraEventCallback() {

        @Override
        public void initCamera(CameraView view) {
            Size optimizedSize;
            mCameraView.getSurfaceHolder().setKeepScreenOn(true);
            optimizedSize = optimizeSize(view.getWidth(), view.getHeight());
            view.getSurfaceHolder().setFixedSize(optimizedSize.getWidth(),optimizedSize.getHeight());

        }

        @Override
        public void openCamera(Surface surface) {
            mCameraFunction.openCamera(surface, mCameraPosition, mOutWidthSize, mOutHeightSize);
        }

        @Override
        public void closeCamera() {
            mCameraFunction.closeCamera();
            stopCameraHandlerThread();

        }

        @Override
        public void takePicture() {
            mCameraFunction.takePicture();

        }

    };
}
