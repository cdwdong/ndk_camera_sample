package com.km.cdw.ndkcamerasample.Util;

import android.app.Activity;
import android.media.Image;
import android.os.Environment;

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
        mFile = createNewFile();
    }
    private File createNewFile() {
        return new File(mMainActivity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/Camera2SampleApp_" + new Date(System.currentTimeMillis()) + ".yuv");
    }
    public void save(byte[] srcImageBuffer) {
        OutputStream outputStream = null;
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(mFile));
            outputStream.write(srcImageBuffer);
        }catch(IOException e) {
            e.printStackTrace();
        }finally {
            if(null != outputStream) {
                try { outputStream.close(); }
                catch (IOException e) { e.printStackTrace();}
            }
        }
    }


    public String getSaveFilePath() { return mFile.getPath(); }

}
