package com.km.cdw.androidopencvwebcam.ImageProcess.FileSystem;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;

public class ImageSaver {
    private final String TAG = this.getClass().getName();
    Image mImage;
    File mFile;
    Activity mMainActivity;

    public ImageSaver(Activity activity) {
        mMainActivity = activity;
        replaceOldFile();
    }
    public void scanFile() {
        MediaScannerConnection.scanFile(mMainActivity, new String[]{mFile.getAbsolutePath()}, null, null);

    }
    public void replaceOldFile() {
        mFile = new File(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/Camera2SampleApp_" + new Date(System.currentTimeMillis()) + ".jpeg");
    }
    public void save(byte[] srcImageBuffer) {
        OutputStream outputStream = null;
        replaceOldFile();
        Log.e(TAG, "save file path : " + getSaveFile());
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            outputStream.write(srcImageBuffer);
        }catch(IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Occurred Exception : " + e);
        }finally {
            if(null != outputStream) {
                try { outputStream.close(); }
                catch (IOException e) { e.printStackTrace();}
            }
        }
    }
    public void saveBitmap(Bitmap bitmap) {
        FileOutputStream out = null;

        if(bitmap != null) {
            replaceOldFile();
            Log.e(TAG, "save file path : " + getSaveFile());
            //save
            try {
                out = new FileOutputStream(getSaveFile());
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            }catch (IOException e) {
                Log.e(TAG, "Occurred Exception : " + e);
            }
        }

    }


    public File getSaveFile() { return mFile; }

}
