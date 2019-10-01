package com.km.cdw.ndkcamerasample.Camera.UI;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.km.cdw.ndkcamerasample.Camera.ICameraEventCallback;
import com.km.cdw.ndkcamerasample.CameraFragment;
import com.km.cdw.ndkcamerasample.MainActivity;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback2 {
    private final String TAG = this.getClass().getName();
    private SurfaceHolder mHolder;
    private Handler mHandler;
    private ICameraEventCallback mCameraCallback;
    private ImageReader mPreviewImageReader;
    private int mSurfaceWidth;
    private int mSurfaceHeight;

    public CameraView(Context context) {
        super(context);

    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SurfaceHolder getSurfaceHolder(){
        if(mHolder != null){ return mHolder; }
        return null;
    }
    public void setCameraCallback(ICameraEventCallback callback) {
        mCameraCallback = callback;
    }
    public void setHandler(Handler handler) {mHandler = handler;}
    public void configHolder() { getHolder().addCallback(this);}
    public void initCameraView(Handler threadHandler) {
        setHandler(threadHandler);
        configHolder();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, " : surfaceCreated()");
        mHolder = holder;
        mCameraCallback.initCamera(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, " : surfaceChanged()");
        mHolder = holder;
        mSurfaceWidth = width;
        mSurfaceHeight = height;


        mPreviewImageReader = ImageReader.newInstance(mSurfaceWidth, mSurfaceHeight,
                CameraFragment.C_IMAGE_FORMAT, 2);
        mPreviewImageReader.setOnImageAvailableListener(mPreviewImageReaderListener, mHandler);

        if(MainActivity.DEBUG_MODE) {
            Log.e(TAG, "view width : " + mSurfaceWidth
                    + "view height : " + mSurfaceHeight);
        }

        mCameraCallback.openCamera(mPreviewImageReader.getSurface());
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, " : surfaceDestroyed()");
        if(mCameraCallback != null) mCameraCallback.closeCamera();
        if(mPreviewImageReader != null) mPreviewImageReader.close();
        mPreviewImageReader = null;
    }
    @Override
    public void surfaceRedrawNeeded(SurfaceHolder holder) {

    }
    private ImageReader.OnImageAvailableListener mPreviewImageReaderListener =  new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();
            if(image == null) return;

            Canvas viewCanvas = null;
            Bitmap bitmap = Bitmap.createBitmap(mSurfaceWidth,mSurfaceHeight, Bitmap.Config.ARGB_8888);



            switch (image.getFormat()) {
                case CameraFragment.C_IMAGE_FORMAT:
//                    buffer = image.getPlanes()[0].getBuffer();
//                    bytes = new byte[buffer.remaining()];
//                    buffer.get(bytes);
//                    Log.e(TAG, "bitmap width : " + image.getWidth() + "bitmap heigth : " + image.getPlanes()[0].getBuffer().capacity());
//                    Log.e(TAG, "bitmap width : " + bitmap.getWidth() + "bitmap heigth : " + bitmap.getHeight());
//                    buffer.rewind();
//                    bitmap.copyPixelsFromBuffer(buffer);

                    Image.Plane yPlane = image.getPlanes()[0];
                    Image.Plane uvPlane = image.getPlanes()[1];

                    ByteBuffer yBuffer = yPlane.getBuffer();
                    ByteBuffer uvBuffer = uvPlane.getBuffer();
                    int yBufferSize = yBuffer.remaining();
                    int uvBufferSize = uvBuffer.remaining();
                    byte[] yuvByteArray = new byte[yBufferSize + 1 + uvBufferSize];


                    yBuffer.get(yuvByteArray, 0, yBufferSize);
                    uvBuffer.get(yuvByteArray, yBufferSize + 1, uvBufferSize);

                    ByteBuffer yuvBuffer = ByteBuffer.allocateDirect(yBufferSize + 1 + uvBufferSize);
                    if(!MainActivity.DEBUG_MODE) {
                        Log.e(TAG, "yuvBuffer remain : " + yuvBuffer);
                    }
                    yuvBuffer.put(yuvByteArray);
                    yuvBuffer.rewind();

                    if(!MainActivity.DEBUG_MODE) {
                        Log.e(TAG,  "image width : " + image.getWidth() + " image height : " + image.getHeight());
                        Log.e(TAG, "yPlane Size : " + yPlane.getRowStride() + "uvPlane Size : " + uvPlane.getRowStride());
                        Log.e(TAG, "yBufferSize : " + yBufferSize + " uvPlane Size : " + uvBufferSize);
                        Log.e(TAG, "yuvBuffer remain : " + yuvBuffer.remaining() + " Cap : " +yuvBuffer.capacity());
                    }

                    Mat mat = new Mat(mSurfaceHeight + mSurfaceHeight/2, mSurfaceWidth, CvType.CV_8UC1, yuvBuffer);
                    Imgproc.cvtColor(mat, mat, Imgproc.COLOR_YUV2GRAY_NV21);


                    Utils.matToBitmap(mat, bitmap);


                    //Draw Bitmap on Canvas
                    viewCanvas = mHolder.lockCanvas();
                    viewCanvas.drawColor(0, android.graphics.PorterDuff.Mode.CLEAR);

                    viewCanvas.drawBitmap(bitmap, new Rect(0,0,bitmap.getWidth(), bitmap.getHeight()),
                            new Rect((viewCanvas.getWidth() - bitmap.getWidth()) / 2,
                                    (viewCanvas.getHeight() - bitmap.getHeight()) / 2,
                                    (viewCanvas.getWidth() - bitmap.getWidth()) / 2 + bitmap.getWidth(),
                                    (viewCanvas.getHeight() - bitmap.getHeight()) / 2 + bitmap.getHeight()), null);
                    mHolder.unlockCanvasAndPost(viewCanvas);
                    break;

                default:
                    Log.e(TAG, " : NOT SUPPORT IMAGE FORMAT");
            }
            image.close();
        }
    };


}
