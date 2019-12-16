package com.km.cdw.androidopencvwebcam;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.ImageFormat;
import android.graphics.Point;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Size;
import android.view.Display;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.km.cdw.androidopencvwebcam.ImageProcess.CVProcess.OpencvProcessor.ProcessConfig;
import com.km.cdw.androidopencvwebcam.ImageProcess.CameraImageConverter;
import com.km.cdw.androidopencvwebcam.ImageProcess.FileSystem.ImageSaver;
import com.km.cdw.androidopencvwebcam.ImageProcess.Km_GLSurfaceView;
import com.km.cdw.androidopencvwebcam.Network.SocketIo.SocketIoListeners;
import com.km.cdw.androidopencvwebcam.Network.SocketIo.SocketIoManager;
import com.km.cdw.androidopencvwebcam.Preference.SharedPreferenceController;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;

import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    private final static String TAG = "MainActivity";
    public final static int REQUEST_PERMISSION = 1;
    public final static int INTENT_REQUEST_CODE = 1;

    private FrameLayout mFL_CameraView;
    private Km_GLSurfaceView mConvertedView;
    private FrameLayout.LayoutParams mConvertViewParams;

    private static final Object sLock = new Object();
    private boolean mCameraOpenFlag = false;
    private Camera_API mCamera;
    private CameraImageConverter mImageConverter;
    private ImageSaver mImageSaver;

    private View mBtn_capture;
    private View mBtn_gallery;
    private View mBtn_option;

    private HashMap<String, String> mHaarcascadesFiles;

    private String mSetting_filter;
    private String mSetting_detector;
    private Size[] mCameraAvailableSizes;

    private SharedPreferences mSharedSettings;
    private SocketIoManager mSocketIoManager = SocketIoManager.getInstance();
    private SharedPreferenceController mController;
    private SharedPreferences.OnSharedPreferenceChangeListener mPreferenceWatcher = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Log.d(TAG, "preference changed");
            settingByPreferences();
        }
    };

    private int mWidth = 720;
    private int mHeight = 1280;
    private final static int mCameraPosition = 0;
    private final static int mPxl_fmt = ImageFormat.YUV_420_888;

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
    private SurfaceHolder.Callback mCallback_openCameraSync = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            startCamera();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopCamera();
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        askPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO, Manifest.permission.INTERNET});

        //init Data
        mHaarcascadesFiles = new HashMap<>();
        mHaarcascadesFiles.put("detect_face", "opencv/haarcascades/haarcascade_frontalface_default.xml");
        mHaarcascadesFiles.put("detect_body", "opencv/haarcascades/haarcascade_lowerbody.xml");
        mHaarcascadesFiles.put("detect_eyes", "opencv/haarcascades/haarcascade_eye.xml");
        loadAssetFile();

        //Size Configuration
        Point mSize = new Point();
        Display mDisplay = getWindowManager().getDefaultDisplay();
        mDisplay.getSize(mSize);
        mWidth = mSize.x;
        mHeight = mSize.y;

        //View Params Setting
        mConvertViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.UNSPECIFIED_GRAVITY);
        mConvertViewParams.width = mWidth;
        mConvertViewParams.height = mHeight;
        mConvertViewParams.gravity = Gravity.END | Gravity.FILL_VERTICAL;

        //Init Field
        mConvertedView = new Km_GLSurfaceView(this);
        mCamera = new Camera_API(this);
        mImageConverter = new CameraImageConverter();
        //File Saver Setting
        mImageSaver = new ImageSaver(this);
        mSharedSettings = PreferenceManager.getDefaultSharedPreferences(this);

        //Image Converter Setting
        mConvertedView.setRenderer(mImageConverter.getRenderer());
        mConvertedView.getHolder().addCallback(mCallback_openCameraSync);
        mSharedSettings.registerOnSharedPreferenceChangeListener(mPreferenceWatcher);

        //network
        mController = new SharedPreferenceController(this);
        SocketIoListeners listeners = new SocketIoListeners(mController);
        mSocketIoManager.onConnectListener(listeners.getOnConnectListener());
        mSocketIoManager.onDisconnectListener(listeners.getOnDisconnectListener());
        mSocketIoManager.onListener("response setting data", listeners.getResponseSettingDataListener());
        mSocketIoManager.onListener("response capture camera", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                cameraCapture();
            }
        });


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
        Log.e(TAG, "onResume");
        mSocketIoManager.connect();
        mSocketIoManager.requestGetSettingData();

        // Hide the status bar
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY // hide status bar and nav bar after a short delay, or if the user interacts with the middle of the screen
        );

        settingByPreferences();
        Log.e(TAG, "onResume");

    }

    @Override
    protected void onPause() {
        super.onPause();
        mSocketIoManager.disconnect();
    }

    public void settingByPreferences() {
        // Setting Detector, Filter
        mSetting_filter = mSharedSettings.getString("filter", "filter_default");
        mSetting_detector = mSharedSettings.getString("detect", "detect_default");
        Log.d(TAG, "filter : " + mSetting_filter + " detector : " + mSetting_detector);


        String[] filterValues = getResources().getStringArray(R.array.effect_values);
        String[] detectValues = getResources().getStringArray(R.array.detector_values);

        if(mSetting_filter.equals(filterValues[0])) {
            mImageConverter.setCvProcessFlag(ProcessConfig.DEFAULT);
        } else if(mSetting_filter.equals(filterValues[1])) {
            mImageConverter.setCvProcessFlag(ProcessConfig.CANNY);
        } else if(mSetting_filter.equals(filterValues[2])) {
            mImageConverter.setCvProcessFlag(ProcessConfig.BLUR);
        } else if(mSetting_filter.equals(filterValues[3])) {
            mImageConverter.setCvProcessFlag(ProcessConfig.EMBOSE);
        } else if(mSetting_filter.equals(filterValues[4])) {
            mImageConverter.setCvProcessFlag(ProcessConfig.SKETCH);
        } else {
            mImageConverter.setCvProcessFlag(ProcessConfig.DEFAULT);
        }

        if(mSetting_detector.equals(detectValues[0])) {
            mImageConverter.setCvHaarcascadesFile("");
        } else if(mSetting_detector.equals(detectValues[1])) {
            mImageConverter.setCvHaarcascadesFile(mHaarcascadesFiles.get("detect_face"));
        } else if(mSetting_detector.equals(detectValues[2])) {
            mImageConverter.setCvHaarcascadesFile(mHaarcascadesFiles.get("detect_body"));
        } else if(mSetting_detector.equals(detectValues[3])) {
            mImageConverter.setCvHaarcascadesFile(mHaarcascadesFiles.get("detect_eyes"));
        } else {
            mImageConverter.setCvHaarcascadesFile("");
        }
    }
    public void cameraCapture() {
        if(mCameraOpenFlag) {
            Bitmap frame = mImageConverter.getLastFrame();
            mImageSaver.saveBitmap(mImageConverter.getLastFrame());
            //rest send
            mSocketIoManager.requestPostPicture(frame);
            this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(MainActivity.this, "Image Saved : " + mImageSaver.getSaveFile(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void loadUi() {
        //UI Setting
        mFL_CameraView = findViewById(R.id.fl_camera);
        mFL_CameraView.addView(mConvertedView, mConvertViewParams);

        mBtn_capture = findViewById(R.id.btn_capture);
        mBtn_capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cameraCapture();
            }
        });
        mBtn_capture.bringToFront();

        mBtn_gallery = findViewById(R.id.btn_gallery);
        mBtn_gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GalleryActivity.class));
            }
        });
        mBtn_gallery.bringToFront();

        mBtn_option = findViewById(R.id.btn_option);
        mBtn_option.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(new Intent(MainActivity.this, CameraSettingsActivity.class), INTENT_REQUEST_CODE);
            }
        });
        mBtn_option.bringToFront();
    }

    public Size getOptimizedSize() {
        CameraManager cameraManager = (CameraManager) this.getSystemService(Activity.CAMERA_SERVICE);
        CameraCharacteristics cameraCharacter = null;
        String cameraId = null;

        try {
            cameraId = cameraManager.getCameraIdList()[mCameraPosition];
            cameraCharacter = cameraManager.getCameraCharacteristics(cameraId);
            mCameraAvailableSizes = cameraCharacter.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(mPxl_fmt);
            for(Size s: mCameraAvailableSizes) {
                Log.d(TAG, "Able SIZE : " + s);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Camera ERROR : " + e);
        }

        int optWidth = mWidth;
        int optHeight = mHeight;

        for(Size s: mCameraAvailableSizes) {
            if(s.getWidth() < optWidth && s.getHeight() < optHeight) {
                optWidth = s.getWidth();
                optHeight = s.getHeight();

                return new Size(optWidth, optHeight);
            }
        }
        return null;
    }

    public void startCamera(){
        Size optimizedSize = getOptimizedSize();

        if(optimizedSize == null) {
            Log.e(TAG, "NOT SUPPORT CAMERA" + "optimizedSize is null");
            return;
        }
        synchronized (sLock) {
            mImageConverter.openConverter(optimizedSize.getWidth(), optimizedSize.getHeight(), ImageFormat.YUV_420_888, 2);

            mCamera.setCameraParameter(mImageConverter.getImgReaderSurface(),mWidth, mHeight, mPxl_fmt, 60, mCameraPosition);
            mCamera.openCamera();
            mCameraOpenFlag = true;
        }
    }
    public void stopCamera() {
        mCameraOpenFlag = false;
        AsyncTask<Void, Void, Void> cameraCloseTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                synchronized (sLock) {
                    mCamera.closeCamera();
                    mCameraOpenFlag = false;
                    mImageConverter.closeConverter();
                }
                return null;
            }
        };
        cameraCloseTask.execute();

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
    public void loadAssetFile() {
        AssetManager assetManager = getResources().getAssets();
        String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
        String dstPath = null;
        File dstFile = null;

        byte[] buffer = new byte[1024];
        int len = 0;

        Log.d(TAG, "Caching File BaseDir : " + baseDir);

        InputStream inputStream = null;
        BufferedOutputStream outputStream = null;

        for( String key : mHaarcascadesFiles.keySet()) {
            String fileName = mHaarcascadesFiles.get(key);
            dstPath = baseDir + File.separator + fileName;
            try {
                inputStream = assetManager.open(fileName);
                dstFile = new File(dstPath);
                checkNmakeFile(dstFile);

                outputStream = new BufferedOutputStream(new FileOutputStream(dstFile));

                while((len = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0 , len);
                }
                Log.d(TAG, "Caching File to : " + dstPath);
                mHaarcascadesFiles.put(key, dstPath);


            } catch ( IOException e) {
                Log.e(TAG, "Occurred Exception : " + e);
                e.printStackTrace();
            } finally {
                try {
                    if(inputStream != null) {
                        inputStream.close();
                        inputStream = null;
                    }
                    if(outputStream != null) {
                        outputStream.flush();
                        outputStream.close();
                        outputStream = null;
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Occurred Exception : " + e);
                    e.printStackTrace();
                }
            }
        }
    }
    public void checkNmakeFile(File file) {
        String path;
        StringBuilder dirPath = new StringBuilder();
        String[] temp;
        File tempFile;

        if(!file.exists()) {
            path = file.getAbsolutePath();
            temp = path.split("/");
            temp = Arrays.copyOfRange(temp, 0, temp.length-1);
            for(String e: temp) {
                dirPath.append(e + "/");
            }
            tempFile = new File(dirPath.toString());
            tempFile.mkdirs();

            try {
                file.createNewFile();
            }catch (IOException e ) {
                e.printStackTrace();
            }
        }
    }

}
