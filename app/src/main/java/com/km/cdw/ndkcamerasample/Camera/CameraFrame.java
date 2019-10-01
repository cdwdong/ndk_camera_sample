package com.km.cdw.ndkcamerasample.Camera;

import android.graphics.ImageFormat;
import android.util.Log;

import com.km.cdw.ndkcamerasample.MainActivity;

import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

public class CameraFrame {
//    private static final String TAG = CameraYUVFrame.class.getCanonicalName();
    private int mImageFormat;
    private int mWidth;
    private int mHeight;
    private Mat mRgba;


    public CameraFrame(int width, int height, int format) {
        mWidth = width;
        mHeight = height;
        mImageFormat = format;
        mRgba = new Mat();
    }
//    public Mat getGrayFrame() {
//        if(MainActivity.DEBUG_MODE) {
//            Log.e(TAG, "mWidth : " + mWidth + "mHeight : " + mHeight);
//        }
//        return mYFrameData.submat(0,mWidth, 0, mHeight);
//    }
//    public Mat getRGBFrame() {
//        if(MainActivity.DEBUG_MODE) {
//            Log.e(TAG, "Y width : " + mYFrameData.width() + "Y height : " + mYFrameData.height());
//            Log.e(TAG, "UV width : " + mUVFrameData.width() + "UV height : " + mUVFrameData.height());
//        }
//
//        if (mImageFormat == ImageFormat.NV21)
//            Imgproc.cvtColor(mYFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21, 4);
//        else if (mImageFormat == ImageFormat.YUV_420_888) {
//            Imgproc.cvtColorTwoPlane(mYFrameData, mUVFrameData, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
//        } else
//            throw new IllegalArgumentException("Preview Format can be NV21");
//
//        return mRgba;
//    }
    public void releaseRgba() {
        mRgba.release();
    }

}

