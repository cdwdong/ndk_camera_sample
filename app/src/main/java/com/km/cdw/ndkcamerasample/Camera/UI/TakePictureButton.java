package com.km.cdw.ndkcamerasample.Camera.UI;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.View;

import com.km.cdw.ndkcamerasample.Camera.ICameraEventCallback;

public class TakePictureButton extends AppCompatButton {
private ICameraEventCallback mCallback;

    public TakePictureButton(Context context) {
        super(context);
    }

    public TakePictureButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public TakePictureButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setCallback(ICameraEventCallback callback) {
        this.mCallback = callback;
    }

    public class CustomClickEventListener implements OnClickListener {

        @Override
        public void onClick(View v) {
            mCallback.takePicture();
        }
    }
}
