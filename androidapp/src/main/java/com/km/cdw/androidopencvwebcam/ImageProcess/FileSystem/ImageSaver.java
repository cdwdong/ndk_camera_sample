package com.km.cdw.androidopencvwebcam.ImageProcess.FileSystem;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class ImageSaver {
    private final String TAG = this.getClass().getName();
    File mFile;
    Context mContext;

    public ImageSaver(Context context) {
        mContext = context;
        replaceFile();
    }
    public void scanFile() {
        MediaScannerConnection.scanFile(mContext, new String[]{mFile.getAbsolutePath()}, null, null);
    }
    public void replaceFile() {
        mFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                + "/KmOpenCvImage_" + System.currentTimeMillis() + ".jpeg");
    }
    public void save(byte[] srcImageBuffer) {
        OutputStream outputStream = null;
        replaceFile();
        Log.d(TAG, "save file path : " + getSaveFile());
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
            replaceFile();
            Log.d(TAG, "save file path : " + getSaveFile());
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
