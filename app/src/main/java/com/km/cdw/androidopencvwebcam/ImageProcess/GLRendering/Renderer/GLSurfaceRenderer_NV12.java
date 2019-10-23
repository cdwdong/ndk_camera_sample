package com.km.cdw.androidopencvwebcam.ImageProcess.GLRendering.Renderer;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.util.Log;

import com.km.cdw.androidopencvwebcam.ImageProcess.YUVImgData;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayDeque;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLSurfaceRenderer_NV12 implements GLSurfaceView.Renderer{
    private static final String TAG = "GLSurfaceRenderer_NV12";
    private final String vertexShaderCode =
                "attribute vec4 v_position;" +
                "attribute vec2 v_colour;" +
                "varying vec2 v_texCoord;" +
                "void main()" +
                "{" +
                    "gl_Position = vec4(v_position.xyz,1.0);" +
                    "v_texCoord = v_colour;" +
                "}";
    /*
     * fragment shader string
     */
    private final String fragmentShaderCode_NV21 =
            "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D yTexture;" +
                    "uniform sampler2D uvTexture;" +
                    "const mat3 yuv2rgb = mat3(" +
                    "1, 0, 1.2802," +
                    "1, -0.214821, -0.380589," +
                    "1, 2.127982, 0" +
                    ");" +

                    "void main() {" +
                    "vec3 yuv = vec3(" +
                    //y
                    "1.1643 * (texture2D(yTexture, v_texCoord).r - 0.0627)," +
                    //u
                    "texture2D(uvTexture, v_texCoord).a - 0.5," +
                    //v
                    "texture2D(uvTexture, v_texCoord).r - 0.5" +
                    ");" +
                    "vec3 rgb = yuv * yuv2rgb;" +
                    "gl_FragColor = vec4(rgb, 1.0);" +
                    "}";
    private final String fragmentShaderCode_NV12 =
                    "precision mediump float;" +
                    "varying vec2 v_texCoord;" +
                    "uniform sampler2D yTexture;" +
                    "uniform sampler2D uvTexture;" +
                    "const mat3 yuv2rgb = mat3(" +
                        "1, 0, 1.2802," +
                        "1, -0.214821, -0.380589," +
                        "1, 2.127982, 0" +
                    ");" +

                    "void main() {" +
                        "vec3 yuv = vec3(" +
                        //y
                        "1.1643 * (texture2D(yTexture, v_texCoord).r - 0.0627)," +
                            //u
                            "texture2D(uvTexture, v_texCoord).r - 0.5," +
                            //v
                            "texture2D(uvTexture, v_texCoord).a - 0.5" +
                        ");" +
                        "vec3 rgb = yuv * yuv2rgb;" +
                        "gl_FragColor = vec4(rgb, 1.0);" +
//                        "gl_FragColor = 1.1643 * (texture2D(yTexture, v_texCoord).r - 0.0627)"+
                    "}";
    private int mWidth = 0;
    private int mHeight = 0;

    ByteBuffer mY_Buffer;
    ByteBuffer mUV_Buffer;

    private FloatBuffer mVertexBuffer;
    private FloatBuffer mCoordBuffer;
    private int mProgram;
    private int mVertexHandle = -1;
    private int mColourHandle = -1;
    private int mYHandle = -1;
    private int mUVHandle = -1;
    private int mMVPMatrixHandle = -1;
    private int[] mTexIDs = new int[2];


    long start_time = 0;
    long cur_time   = 0;
    long prev_time  = 0;
    // number of coordinates per vertex in this array
    static final int COORDS_PER_VERTEX = 3;
    private final float mVertex_NV21[] = {
            -1.0f,  1.0f, 0.0f,
             1.0f,	1.0f, 0.0f,
            -1.0f, -1.0f, 0.0f,
             1.0f, -1.0f, 0.0f
    };
    //texture rotate 90degree, reverse by y axis
    private final float mCoord_NV21[] = {
//            1.0f, 1.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            0.0f, 0.0f,
//            //original camera position
//            0.0f, 0.0f,
//            1.0f, 0.0f,
//            0.0f, 1.0f,
//            1.0f, 1.0f,
            //back camera rotate position
            0.0f, 1.0f,
            0.0f, 0.0f,
            1.0f, 1.0f,
            1.0f, 0.0f,
    };
    private float mvpMatrix[];
    float color[] = { 0.2f, 0.709803922f, 0.898039216f, 1.0f };

    private ArrayDeque<YUVImgData> mWorkQueue;
    private YUVImgData mYUVdata = null;

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mVertexBuffer = createDirectFloatBuf(mVertex_NV21);
        mCoordBuffer = createDirectFloatBuf(mCoord_NV21);

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        mProgram = createProgram(vertexShaderCode, fragmentShaderCode_NV12);


        mVertexHandle = GLES20.glGetAttribLocation(mProgram, "v_position");
        checkGLError("GetAttr : v_position");
        mColourHandle = GLES20.glGetAttribLocation(mProgram, "v_colour");
        checkGLError("GetAttr : v_colour");

        mYHandle = GLES20.glGetUniformLocation(mProgram, "yTexture");
        checkGLError("GetUniform : yTexture");
        mUVHandle = GLES20.glGetUniformLocation(mProgram, "uvTexture");
        checkGLError("GetUniform : uvTexture");

        GLES20.glGenTextures(2, mTexIDs, 0);
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

        if(!mWorkQueue.isEmpty()) {
            Log.e(TAG,"work_queue size : "+mWorkQueue.size());
            mYUVdata = mWorkQueue.pop();
        }
        if(mYUVdata != null){

            GLES20.glClearColor(0.5f, 0.2f, 0.1f, 0.0f);
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            checkGLError("Clear");

            GLES20.glDisable(GLES20.GL_CULL_FACE);
            GLES20.glEnable(GLES20.GL_BLEND);
            GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);
            checkGLError("Effect");

            GLES20.glUniform1i(mYHandle, 0);
            GLES20.glUniform1i(mUVHandle, 1);

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

            mY_Buffer = mYUVdata.getYData();
            mUV_Buffer = mYUVdata.getUData();

            if(mY_Buffer != null) {
                //Y
                GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
                checkGLError("ActiveTexture Y");
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexIDs[0]);
                checkGLError("BindTexture Y");
                GLES20.glUniform1i(mYHandle, 0);
                checkGLError("getUniformTexture Y");
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE, mYUVdata.getWidth(), mYUVdata.getHeight(),
                        0, GLES20.GL_LUMINANCE, GLES20.GL_UNSIGNED_BYTE, mY_Buffer);
                checkGLError("TexImage2D Y");
            }
            if(mUV_Buffer  != null) {
                // UV
                GLES20.glActiveTexture(GLES20.GL_TEXTURE1);
                        checkGLError("ActiveTexture UV");
                GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTexIDs[1]);
                        checkGLError("BindTexture UV");
                GLES20.glUniform1i(mUVHandle, 1);
                        checkGLError("getUniformTexture UV");
                GLES20.glPixelStorei( GLES20.GL_UNPACK_ALIGNMENT, 1 );
                GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_LUMINANCE_ALPHA, mYUVdata.getWidth()/2, mYUVdata.getHeight()/2,
                        0, GLES20.GL_LUMINANCE_ALPHA, GLES20.GL_UNSIGNED_BYTE, mUV_Buffer);
                        checkGLError("TexImage2D UV");
            }

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
    public void setWorkQueue(ArrayDeque<YUVImgData> queue) {
        mWorkQueue = queue;
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
    public ShortBuffer createDirectShortBuf(short[] sarr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(sarr.length * 4);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer sb = bb.asShortBuffer();
        sb.put(sarr);
        sb.rewind();

        return sb;
    }
}

