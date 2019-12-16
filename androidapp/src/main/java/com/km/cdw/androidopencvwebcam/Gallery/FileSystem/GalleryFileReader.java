package com.km.cdw.androidopencvwebcam.Gallery.FileSystem;

import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GalleryFileReader {
    public static final String TAG = "GalleryFileReader";
    private File mBaseDir;
    private ArrayList<File> mFileList;

    public GalleryFileReader() {}

    public void setBaseDir(File baseDir) {
        mBaseDir = baseDir;
    }

    //read files from baseDirectory File
    public void readGalleryFiles() {
        File[] list = mBaseDir.listFiles();
        mFileList = new ArrayList<>();

        for(int i =0; i < list.length; i++) {
            File file = list[i];
            Log.d(TAG, "file name : " + file.getName());
            if(file == null || !file.exists()) continue;
            String fname = file.getName();
            //find only jpeg or jpg
            Pattern p = Pattern.compile("jpeg\\s*$");
            Matcher matcher = p.matcher(fname);
            if(file.exists() && file.isFile() && matcher.find()) {
                mFileList.add(file);
                Log.d(TAG, "passed file name : " + file.getName());
            }
        }
    }

    public ArrayList<File> getFileList() {
        return mFileList;
    }

}
