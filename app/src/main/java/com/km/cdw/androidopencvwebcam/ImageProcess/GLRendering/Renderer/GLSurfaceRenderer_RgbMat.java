package com.km.cdw.androidopencvwebcam.ImageProcess.GLRendering.Renderer;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.km.cdw.androidopencvwebcam.ImageProcess.FileSystem.ImageSaver;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayDeque;
import java.util.concurrent.Semaphore;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceRenderer_RgbMat implements GLSurfaceView.Renderer {
    private static final String TAG = "GLSurfaceRenderer";
    private final String vertexShaderCode =
            "attribute vec4 v_position;" +
            "attribute vec2 v_colour;" +
            "varying vec2 v_texCoord;" +
            "void main()" +
            "{" +
                "gl_Position = v_position;" +
                "v_texCoord = v_colour;" +
            "}";
    /*
     * fragment shader string
     */
    private final String fragmentShaderCode =
            "precision mediump float;" +
            "varying vec2 v_texCoord;" +
            "uniform sampler2D s_texture;" +
            "void main() {" +
                "gl_FragColor = texture2D(s_texture, v_texCoord);" +
            "}";
    private int mWidth = 0;
    private int mHeight = 0;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;
    private int mProgram;
    private int mVertexHandle = -1;
    private int mColourHandle = -1;
    private int mTextureHandle = -1;
    private int mMVPMatrixHandle = -1;
    private int[] mTexIDs = new int[1];


    long start_time = 0;
    long cur_time   = 0;
    long prev_time  = 0;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final float mVertex[] = {
            -1.0f,  1.0f, 0.0f,
            1.0f,	1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
            1.0f, -1.0f, 0.0f
    };
    //texture rotate 90degree, reverse by y axis
    private final float mCoord[] = {
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f,
            0.0f, 0.0f,
            1.0f, 0.0f,
            0.0f, 1.0f,
            1.0f, 1.0f,
//            //back camera position
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            1.0f, 1.0f,
//            1.0f, 0.0f,
    };
    private float mvpMatrix[];
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private ArrayDeque<Mat> mWorkQueue;
    private Semaphore mWorkQueueLock;
    private Mat mMatData = null;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mVertexBuffer = createDirectFloatBuf(mVertex);
        mCoordBuffer = createDirectFloatBuf(mCoord);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mProgram = createProgram(vertexShaderCode, fragmentShaderCode);


        mVertexHandle = GLES20.glGetAttribLocation(mProgram, "v_position");
        checkGLError("GetAttr : v_position");
        mColourHandle = GLES20.glGetAttribLocation(mProgram, "v_colour");
        checkGLError("GetAttr : v_colour");
        mTextureHandle = GLES20.glGetUniformLocation(mProgram, "s_texture");
        checkGLError("GetUniform : Texture");

        GLES20.glGenTextures(1, mTexIDs, 0);
        checkGLError("GenTextures");

        GLES20.glUseProgram(mProgram);
        checkGLError("useProgram");

        GLES20.glViewport(0,0, mWidth = width, mHeight = height);
        checkGLError("Viewport");
    }

    @Override
    public void onDrawFrame(GL10 gl) {
        //count time
        if(start_time == 0){
            start_time = System.currentTimeMillis();
        }
        cur_time = System.currentTimeMillis() - start_time;

        if(mWorkQueueLock.tryAcquire()) {
            if(!mWorkQueue.isEmpty()) {
                Log.e(TAG,"work_queue size : "+mWorkQueue.size());
                mMatData = mWorkQueue.pop();
            }
        }
        if(mMatData != null){

            GLES20.glClearColor(0.5f, 0.2f, 0.1f, 0.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            checkGLError("Clear");

            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            checkGLError("Effect");

            GLES20.glUniform1i(mTextureHandle, 0);

            for (int i=0; i < mTexIDs.length; i++) {
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexIDs[i]);
                //            checkGLError("BindTex : " + i);

                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
                GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
                //            checkGLError("TexParameter : " + i);
            }
            checkGLError("TexPara");


            Log.e(TAG, "Mat Info : " + mMatData + mMatData.total());
//            //Draw Mat Native
            drawMatNative(mMatData.width(), mMatData.height(), mMatData.getNativeObjAddr(), mTexIDs[0]);


            GLES20.glEnableVertexAttribArray(mVertexHandle);
            GLES20.glVertexAttribPointer(mVertexHandle, 3, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
            GLES20.glEnableVertexAttribArray(mColourHandle);
            GLES20.glVertexAttribPointer(mColourHandle, 2, GLES20.GL_FLOAT, false, 0, mCoordBuffer);

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
            checkGLError("DrawArrays");

        }
        if(prev_time > 0){
//            Log.e(TAG,"start ("+start_time+") cur:(7"+cur_time+")   diff : "+(cur_time-prev_time) );
        }
        prev_time = cur_time;
    }
    public void setWorkQueue(ArrayDeque<Mat> queue, Semaphore queueLock) {
        mWorkQueue = queue;
        mWorkQueueLock = queueLock;
    }
    public int createProgram(String vertexSource, String fragmentSource) {
        Log.e(TAG, "createProgram");
        int vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexSource);
        int fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentSource);
        int glProgram = GLES20.glCreateProgram();
        checkGLError("glCreateProgram");
        GLES20.glAttachShader(glProgram, vertexShader);
        GLES20.glAttachShader(glProgram, fragmentShader);
        GLES20.glLinkProgram(glProgram);

        return glProgram;
    }
    public int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        checkGLError("glCreateShader type:" +shaderType);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compiled, 0);
        if(compiled[0] == 0) {
            Log.e(TAG, "Could not compile shader, type : " + shaderType);
        }
        return shader;
    }
    public void checkGLError(String op) {
        while(GLES20.glGetError() != GLES20.GL_NO_ERROR) {
            Log.e(TAG, op + " : GL_ERROR");
        }
    }
    public FloatBuffer createDirectFloatBuf(float[] farr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(farr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        FloatBuffer fb = bb.asFloatBuffer();
        fb.put(farr);
        fb.rewind();

        return fb;
    }
    public Mat getCurrentFrame() {
        return mMatData;
    }
    public Bitmap getCurrentFrameBitmap() {
        if(mMatData == null) {
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(mMatData.width(), mMatData.height(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mMatData, bitmap);

        return bitmap;
    }

    public native boolean drawMatNative(int w, int h, long matAddr, int texId);
}
