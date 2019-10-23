package com.km.cdw.androidopencvwebcam.ImageProcess.GLRendering;

import android.content.Context;
import android.opengl.GLSurfaceView;

import com.km.cdw.androidopencvwebcam.ImageProcess.GLRendering.Renderer.GLSurfaceRenderer_NV12;

public class Km_GLSurfaceView extends GLSurfaceView {
    private static final String TAG = "GLRenderUtil";
    private GLSurfaceRenderer_NV12 mRenderer;
    public Km_GLSurfaceView(Context context) {
        super(context);
        setEGLContextClientVersion(2);
    }
}
