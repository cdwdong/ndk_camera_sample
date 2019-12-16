package com.km.cdw.androidopencvwebcam.ImageProcess;

import android.content.Context;
import android.opengl.GLSurfaceView;

public class Km_GLSurfaceView extends GLSurfaceView {
    private static final String TAG = "GLRenderUtil";
    public Km_GLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }
}
