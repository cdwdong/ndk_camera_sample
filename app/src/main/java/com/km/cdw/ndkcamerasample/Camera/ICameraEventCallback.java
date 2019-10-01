package com.km.cdw.ndkcamerasample.Camera;

import android.util.Size;
import android.view.Surface;

import com.km.cdw.ndkcamerasample.Camera.UI.CameraView;

public interface ICameraEventCallback {
    void initCamera(CameraView view);
    void openCamera(Surface surface);
    void closeCamera();
    void takePicture();


}
