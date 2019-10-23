package com.km.cdw.androidopencvwebcam;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.km.cdw.androidopencvwebcam.ImageProcess.CVProcess.OpencvProcessor.ProcessConfig;
import com.km.cdw.androidopencvwebcam.ImageProcess.CameraImageConverter;
import com.km.cdw.androidopencvwebcam.ImageProcess.GLRendering.Km_GLSurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    public final static int REQUEST_PERMISSION = 1;
    private FrameLayout mFL_CameraView;
    //private TextureView mPreView;
    private SurfaceView mPreView;
    private Km_GLSurfaceView mConvertedView;
    private FrameLayout.LayoutParams mPreviewParams;
    private FrameLayout.LayoutParams mConvertViewParams;
    private Camera_API mCamera;
    private CameraImageConverter mImageConverter;
    private Button mBtn_click;
    private Button mBtn_capture;
    private RadioGroup mRadioGroup;

    int mWidth = 480;
    int mHeight = 600;

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
        System.loadLibrary("opencv_java4");
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OpenCV", "OpenCV loaded successfully");
                    loadUi();

                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    public View.OnClickListener mCaptureButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            mImageConverter.capture();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        //Set App Design
//        requestWindowFeature(Window.FEATURE_NO_TITLE);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
//                WindowManager.LayoutParams.FLAG_FULLSCREEN);
//        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
//                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
//        //set app orientation
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        setContentView(R.layout.activity_main);

        askPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE});

        //Size Configuration
        Point mSize = new Point();
        Display mDisplay = getWindowManager().getDefaultDisplay();
        mDisplay.getSize(mSize);
        mWidth = mSize.x;
        mHeight = mSize.y;

        //View Params Setting
        mPreviewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY);
        mPreviewParams.width = mWidth/2;
        mPreviewParams.height = mHeight;
        mPreviewParams.gravity = Gravity.START | Gravity.FILL_VERTICAL;
        mConvertViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY);
        mConvertViewParams.width = mWidth/2;
        mConvertViewParams.height = mHeight;
        mConvertViewParams.gravity = Gravity.END | Gravity.FILL_VERTICAL;

        //Init Field
        mPreView = new SurfaceView(this);
        mConvertedView = new Km_GLSurfaceView(this);
        mCamera = new Camera_API(this);
//        mPreView = new TextureView(this);;
        mImageConverter = new CameraImageConverter();

        //Image Converter Setting
        mConvertedView.setRenderer(mImageConverter.getRenderer());

        //File Saver Setting
        mImageConverter.setSaver(this);

        //opencv init
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mImageConverter.openConverter(mWidth, mHeight, ImageFormat.YUV_420_888, 2);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopCamera();
        mImageConverter.closeConverter();
    }
    public void loadUi() {
        //UI Setting
        mFL_CameraView = findViewById(R.id.fl_camera);
        mFL_CameraView.addView(mPreView, mPreviewParams);
        mFL_CameraView.addView(mConvertedView, mConvertViewParams);

        mBtn_click = findViewById(R.id.btn_on);
        mBtn_click.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCamera();
            }
        });
        mBtn_click.bringToFront();

        mBtn_capture = findViewById(R.id.btn_capture);
        mBtn_capture.setOnClickListener(mCaptureButtonListener);
        mBtn_capture.bringToFront();

        mRadioGroup = findViewById(R.id.filter_radio_group);
        mRadioGroup.check(R.id.default_btn);
        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.default_btn:{
                        mImageConverter.setCvProcessFlag(ProcessConfig.DEFAULT);
                    }break;
                    case R.id.canny_btn:{
                        mImageConverter.setCvProcessFlag(ProcessConfig.CANNY);
                    }break;
                    case R.id.blur_btn:{
                        mImageConverter.setCvProcessFlag(ProcessConfig.BLUR);
                    }break;
                    case R.id.embose_btn:{
                        mImageConverter.setCvProcessFlag(ProcessConfig.EMBOSE);
                    }break;
                    case R.id.sketch_btn:{
                        mImageConverter.setCvProcessFlag(ProcessConfig.SKETCH);
                    }break;
                }

            }
        });
        mRadioGroup.bringToFront();

    }

    public void startCamera(){
        //mCamera.setCameraParameter(new Surface(mPreView.getSurfaceTexture()), mImageConverter.getImgReaderSurface(),mWidth, mHeight, ImageFormat.YUV_420_888, 20);
        mCamera.setCameraParameter(mPreView.getHolder().getSurface(), mImageConverter.getImgReaderSurface(),mWidth, mHeight, ImageFormat.YUV_420_888, 60);
        mCamera.openCamera();

    }
    public void stopCamera() {
        mCamera.closeCamera();
    }
    public void showToast(final String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        }
    }
    public boolean askPermissions(String[] permissionList) {
        for ( String permission : permissionList ) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                    showToast(permission + " 권한이 필요합니다.");
                    finish();
                }
                ActivityCompat.requestPermissions(this, permissionList, REQUEST_PERMISSION);
                return false;
            }
        }
        return true;

    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

}
