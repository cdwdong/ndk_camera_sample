package com.km.cdw.androidopencvwebcam.ImageProcess.CVProcess;

import android.util.Log;

import com.km.cdw.androidopencvwebcam.ImageProcess.YUVImgData;

import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;


public class OpencvProcessor {
    private static final String TAG = "OpencvProcessor";
    private int mWidth;
    private int mHeight;
    private int mFormat;
    private ProcessConfig mProcessFlag = ProcessConfig.DEFAULT;

    public enum ProcessConfig {
        DEFAULT,
        CANNY,
        BLUR,
        EMBOSE,
        SKETCH
    }

    private ArrayDeque<YUVImgData> mProcessQueue;
    private ArrayDeque<Mat> mRenderQueue;
    private Semaphore mProcessQueueLock;
    private Semaphore mRenderQueueLock;

    private Thread mThread;

    private final OpencvNativeManager sNativeAssembly = new OpencvNativeManager();
    private Mat mTargetMat;

    public OpencvProcessor() {

    }
//    public OpencvProcessor setQueueParam(ArrayDeque<YUVImgData> processQueue, ArrayDeque<YUVImgData> renderQueue) {
//        mProcessQueue = processQueue;
//        mRenderQueue = renderQueue;
//
//        return this;
//    }
    public OpencvProcessor setQueueParam(ArrayDeque<YUVImgData> processQueue, ArrayDeque<Mat> renderQueue, Semaphore processQueueLock, Semaphore renderQueueLock) {
        mProcessQueue = processQueue;
        mRenderQueue = renderQueue;
        mProcessQueueLock = processQueueLock;
        mRenderQueueLock = renderQueueLock;

        return this;
    }
    public OpencvProcessor processCanny() {
        Mat cannyMat = new Mat();
        Mat grey = new Mat();
        int threshold = 500;

        Imgproc.cvtColor(mTargetMat, grey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.Canny(grey, cannyMat, threshold, threshold*2, 5);
        mTargetMat.release();
        mTargetMat = cannyMat;


        return this;
    }
    public OpencvProcessor processBlur() {
        Mat blurMat = new Mat();

        Imgproc.blur(mTargetMat, blurMat, new Size(9,9));
        mTargetMat.release();
        mTargetMat = blurMat;


        return this;
    }
    public OpencvProcessor processEmbose() {
        Mat resultMat = new Mat();
        Mat mask = new Mat(3, 3, CvType.CV_32F, new Scalar(0));

        mask.put(0,0, -1.);
        mask.put(2,2, 1.);

        Imgproc.filter2D(mTargetMat, resultMat, CvType.CV_16S, mask);
        resultMat.convertTo(resultMat, CvType.CV_8U, 1, 128);

        mTargetMat = resultMat;

        return this;

    }
    public OpencvProcessor processSketch() {
        Mat grey, edge, resultMat;
        int threshold = 128;
        grey = new Mat();
        edge = new Mat();
        resultMat = new Mat();

        Imgproc.cvtColor(mTargetMat, grey, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(grey, grey, new Size(3,3) );
        Imgproc.Canny(grey, edge, threshold, threshold*2, 5);

        resultMat.create(mTargetMat.size(), mTargetMat.type());
        resultMat.setTo(Scalar.all(96));
        mTargetMat.copyTo(resultMat, edge);

        mTargetMat = resultMat;

        return this;

    }

    //dont use
    public OpencvProcessor processWaterColor() {
        int dsize = 7;
        double sigma = 32;

        int iterate = 3;
        Mat t1, t2;

        t1 = mTargetMat.clone();
        t2 = new Mat();
        for(int i =0; i < iterate; i++) {
            if(i % 2 == 0)
                Imgproc.bilateralFilter(t1, t2, dsize, sigma, sigma);
            else
                Imgproc.bilateralFilter(t2, t1, dsize, sigma, sigma);
        }
        if (iterate % 2 == 0)
            mTargetMat = t1;
        else
            mTargetMat = t2;

        return this;
    }
//    public YUVImgData getYuvProcessResult() {
//        int[] rgbInts = new int[mWidth*mHeight];
//
//        byte[] yBytes = new byte[mWidth*mHeight];
//        byte[] uvBytes = new byte[mWidth*mHeight/2];
//
//
//        Log.e(TAG, "convertRGB2eachYUV");
//        Mat yuvMat = new Mat();
//        Imgproc.cvtColor(mTargetMat, yuvMat, Imgproc.COLOR_BGR2YUV_IYUV);
//        yuvMat.get(0,0, yBytes);
//        yuvMat.get(mHeight, mWidth, uvBytes);
//        Log.e(TAG, "converted Mat Size : " + yuvMat.size() + " " +  yuvMat.total() + " " + yuvMat.depth() + " " + yuvMat.channels());
//
//
//
//        int size = mWidth * mHeight;
//        return new YUVImgData(ByteBuffer.wrap(yBytes), ByteBuffer.wrap(uvBytes, 0, size/4), ByteBuffer.wrap(uvBytes, size/4, size/4), size, size/4, size/4, mWidth, mHeight, 0);
//    }
    public Mat getMatProcessResult() {
        Mat rgbMat = new Mat();
        Imgproc.cvtColor(mTargetMat, rgbMat, Imgproc.COLOR_BGR2RGB);

        return rgbMat;
    }

//    private Runnable mYuvPostCoroutine = new Runnable() {
//        @Override
//        public void run() {
//            while(true) {
//                if(!mProcessQueue.isEmpty()) {
//                    try {
//                        CameraImageConverter.mProcessQueueLock.acquire();
//                        YUVImgData imgData = mProcessQueue.pop();
//                        CameraImageConverter.mProcessQueueLock.release();
//
//                        setYuvDataParam_NV21(imgData.getYData(), imgData.getUData(), imgData.getVData(), imgData.getWidth(), imgData.getHeight(), imgData.getFormat());
////                        byte[] result = processCanny();
////                        int size = mWidth*mHeight;
////                        imgData = new YUVImgData(ByteBuffer.wrap(result, 0, size), ByteBuffer.allocate(size/2),null, size, size/2, 0, mWidth, mHeight, mFormat);
//                        imgData = getYuvProcessResult();
//                        mRenderQueue.add(imgData);
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        }
//    };
    private Runnable mMatPostCoroutine = new Runnable() {
        @Override
        public void run() {
            try {
                while(!Thread.currentThread().isInterrupted()) {

                    YUVImgData imgData = null;
//                  imgData = mProcessQueue.pop()

                    //get data from imagereader
//                    synchronized (mProcessQueue) {
//                        while(mProcessQueue.isEmpty()) {
//                            mProcessQueue.wait();
//                        }
//                        imgData = mProcessQueue.pop();
//                    }
                    Log.e(TAG, "ProcessQueueSize : " + mProcessQueue.size());
                    if(!mProcessQueue.isEmpty()) {
                        mProcessQueueLock.acquire();
                        imgData = mProcessQueue.pop();
                        while(mProcessQueue.size() > 1) {
                            mProcessQueue.pop();
                        }
                    }
                    if(imgData != null) {
                        //process
                        setYuvDataParam_NV21(imgData.getYData(), imgData.getUData(), imgData.getVData(), imgData.getWidth(), imgData.getHeight(), imgData.getFormat());
                        switch(mProcessFlag) {
                            case DEFAULT:{}break;
                            case CANNY:{ processCanny(); }break;
                            case BLUR:{ processBlur(); }break;
                            case EMBOSE:{ processEmbose(); }break;
                            case SKETCH:{ processSketch(); }break;
                        }
                        //put data to renderder
                        mRenderQueue.add(getMatProcessResult());
                        mRenderQueueLock.release();
                    }
                    Thread.sleep(1);

                }
            }catch (InterruptedException e) {
                Log.e(TAG, "Process Thread is Over");
                mProcessQueueLock.release();
                mRenderQueueLock.release();
            }

        }
    };

    public void setProcessFlag(ProcessConfig pc) {
        mProcessFlag = pc;
    }

    public void startProcessor() {
        mThread = new Thread(mMatPostCoroutine);
        mThread.setDaemon(true); //set deamon Thread
        mThread.start();
    }
    public void stopProcessor() {
        try {
            mThread.interrupt();
            mThread.join();
            mThread = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public OpencvProcessor setYuvDataParam_NV21(ByteBuffer yData, ByteBuffer uData, ByteBuffer vData, int w, int h, int format) {
        mWidth = w;
        mHeight = h;
        mFormat = format;

//        Log.e(TAG, "convertEachYUVsp2RGB");
//        byte[] yuvBytes = new byte[w*h*3/2];
//        yData.get(yuvBytes, 0 , w*h);
//        uData.get(yuvBytes, w*h, w*h/2 - 1);
//        Mat yuv_mat = new Mat(h*3/2, w, CvType.CV_8UC1);
//        yuv_mat.put(0,0, yuvBytes);
//        mTargetMat = new Mat();
//        Imgproc.cvtColor(yuv_mat, mTargetMat, Imgproc.COLOR_YUV2BGR_NV21);

        Mat y_mat = new Mat(h, w, CvType.CV_8UC1, yData);
        Mat uv_mat = new Mat(h / 2, w / 2, CvType.CV_8UC2, uData);
        mTargetMat = new Mat();
        Imgproc.cvtColorTwoPlane(y_mat, uv_mat, mTargetMat, Imgproc.COLOR_YUV2BGR_NV21);

        return this;

    }
}
