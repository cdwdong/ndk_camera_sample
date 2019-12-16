package com.km.cdw.androidopencvwebcam.Gallery;

import android.content.Context;
import android.graphics.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.OnScaleGestureListener;
import android.view.View;
import android.widget.ImageView;

public class ImageMoveNZoomListener implements View.OnTouchListener {
    private static final String TAG = "ImageMoveNZoomListener";
    private static final int TRANSLATION_LIMIT = 400;
    private final Context mContext;
    private final ScaleGestureDetector mScaleGestureDetector;

    private float mScaleFactor = 1.f;
    private float mBeginX = 1.f;
    private float mBeginY = 1.f;

    private ImageView mImageView;

    private DebounceScaleGestureListener mDebouncePinchZoomListener = new DebounceScaleGestureListener(20);

    public ImageMoveNZoomListener(Context context) {
        mContext = context;
        mScaleGestureDetector = new ScaleGestureDetector(mContext, mDebouncePinchZoomListener);
    }

    //ImageView onTouch event also sent to ScaleGestureDetector
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Matrix positionMatrix = new Matrix();
        mImageView = (ImageView)v;
        mScaleGestureDetector.onTouchEvent(event);
        
        int action = event.getAction();
        if(action == MotionEvent.ACTION_DOWN) {
            mBeginX = event.getX();
            mBeginY = event.getY();
        } else if(action == MotionEvent.ACTION_MOVE) {
            float dx = (event.getX() - mBeginX)*0.1f*mScaleFactor;
            float dy = (event.getY() - mBeginY)*0.1f*mScaleFactor;

            mImageView.setX(mImageView.getTranslationX() + dx);
            mImageView.setY(mImageView.getTranslationY() + dy);

        } else if(action == MotionEvent.ACTION_UP) {
            Log.d(TAG, "TranslationX : " + mImageView.getTranslationX() + "TranslationY : " + mImageView.getTranslationY());

            //확대된만큼 이동 한계를 지정
            float trans_limit_multiply_x = TRANSLATION_LIMIT * mScaleFactor;
            float trans_limit_multiply_y = TRANSLATION_LIMIT * mScaleFactor;
            if(mImageView.getTranslationX() > trans_limit_multiply_x) {
                mImageView.setTranslationX(trans_limit_multiply_x);
            }
            if(mImageView.getTranslationX() < -trans_limit_multiply_x) {
                mImageView.setTranslationX(-trans_limit_multiply_x);
            }
            if(mImageView.getTranslationY() > trans_limit_multiply_y) {
                mImageView.setTranslationY(trans_limit_multiply_y);
            }
            if(mImageView.getTranslationY() < -trans_limit_multiply_y) {
                mImageView.setTranslationY(-trans_limit_multiply_y);
            }
        }
        return true;
    }

    class DebounceScaleGestureListener implements OnScaleGestureListener {
        private int min_interval_msec;
        private long prev_time;

        public DebounceScaleGestureListener(int min_interval_msec) {
            this.min_interval_msec = min_interval_msec;
            prev_time = SystemClock.elapsedRealtime();
        }

        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            long current_time = SystemClock.elapsedRealtime();
            if(current_time - prev_time > min_interval_msec) {
                onDebounceScale(detector);
                prev_time = current_time;
            }
            return true;
        }
        public void onDebounceScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            mImageView.setScaleX(mScaleFactor);
            mImageView.setScaleY(mScaleFactor);
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            if(mScaleFactor < 0.9f) {
                mScaleFactor = 0.9f;
            } else if (mScaleFactor > 3.f) {
                mScaleFactor = 3.f;
            }
            mImageView.setScaleX(mScaleFactor);
            mImageView.setScaleY(mScaleFactor);
        }
    }
}
