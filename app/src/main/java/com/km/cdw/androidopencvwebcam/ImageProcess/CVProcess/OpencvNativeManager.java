package com.km.cdw.androidopencvwebcam.ImageProcess.CVProcess;

public class OpencvNativeManager {
    public OpencvNativeManager() {
    }

    public native boolean convertYUV2RGB(int w, int h, byte[] srcBytes, int[] dstInts);
    public native boolean convertRGB2YUV(int w, int h, int[] srcInts, byte[] dstBytes);
    public native boolean convertRGB2eachYUV(int w, int h, int[] srcInts, byte[] yBytes, byte[] uBytes, byte[] vBytes);
    public native boolean convertEachYUV2RGB(int w, int h, byte[] y_bytes, byte[] u_bytes, byte[] v_bytes, int[] dst_ints);
    public native boolean convertEachYUVsp2RGB(int w, int h, byte[] y_bytes, byte[] u_bytes, int[] dst_ints);
    public native boolean processEmbose(int w, int h, byte[] srcBytes, byte[] dstBytes);
}
