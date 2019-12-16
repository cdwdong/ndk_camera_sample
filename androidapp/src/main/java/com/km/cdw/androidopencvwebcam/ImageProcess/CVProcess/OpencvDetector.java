package com.km.cdw.androidopencvwebcam.ImageProcess.CVProcess;

import androidx.annotation.NonNull;

import com.km.cdw.androidopencvwebcam.ThreadProcessor;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

public class OpencvDetector {
    public static final int DELAY = 1;

    private ThreadProcessor mThread = new ThreadProcessor();
    private MatOfRect mResultRectMat = new MatOfRect();
    private Rect[] mResultRects;
    private Mat mTargetMat = new Mat();

    private CascadeClassifier mClassifier = new CascadeClassifier();
    private String mLoadFilePath = null;

    private Runnable mCoroutin = new Runnable() {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {
                    if(mLoadFilePath == null) {
                        continue;
                    }
                    mClassifier.load(mLoadFilePath);
                    Mat gray = new Mat();
                    if( mTargetMat == null || mTargetMat.empty()) {
                        continue;
                    }
                    Imgproc.cvtColor(mTargetMat, gray, Imgproc.COLOR_BGR2GRAY);

                    mClassifier.detectMultiScale(gray, mResultRectMat);
                    gray = null;
                    mTargetMat = null;

                    mResultRects = mResultRectMat.toArray();

                    Thread.sleep(DELAY);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    };

    public void setTarget(Mat m) {
        mTargetMat = m;

    }
    public Rect[] getResultArr() {
        return mResultRects;
    }
    public MatOfRect getResultRectMat() {
        return mResultRectMat;
}
    public void startDetector() {
        mThread.setParams(mCoroutin, "OpencvDetectorThread");
        mThread.startThread(true);
    }
    public void stopDetector() {
        try {
            mThread.stopThread();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
    public void setHaarcascadesFile(String path) {
        mLoadFilePath = path;
    }
    public void resetDetector() {
        mLoadFilePath = null;
        mResultRects = null;
    }

    @NonNull
    @Override
    public String toString() {
        return " LoadFilePath : " + mLoadFilePath;
    }
}
