package com.km.cdw.androidopencvwebcam.ImageProcess;

import java.nio.ByteBuffer;

public class YUVImgData {
    private ByteBuffer yData;
    private ByteBuffer uData;
    private ByteBuffer vData;
    private int y_size = 0;
    private int u_size = 0;
    private int v_size;
    private int w = 0;
    private int h = 0;
    private int yuv_fmt = 0;
    private IYUVImgDataControl mControlFunc = null;

    public YUVImgData(byte[] yBytes, byte[] uBytes, byte[] vBytes, int ySize, int uSize, int vSize, int width, int height, int format)
    {
        if(yBytes != null) {
            yData = ByteBuffer.allocateDirect(ySize);
            yData.put(yBytes);
            yData.rewind();
        }
        if(uBytes != null) {
            uData = ByteBuffer.allocateDirect(uSize);
            uData.put(uBytes);
            uData.rewind();
        }

        y_size = ySize;
        u_size = uSize;
        v_size = vSize;
        w = width;
        h = height;
        yuv_fmt = format;
    }
    public YUVImgData(ByteBuffer yBuf, ByteBuffer uBuf, ByteBuffer vBuf, int ySize, int uSize, int vSize, int width, int height, int format)
    {
        if(yBuf != null) {
            yData = ByteBuffer.allocateDirect(ySize);
            yData.put(yBuf);
            yData.rewind();
        }
        if(uBuf != null) {
            uData = ByteBuffer.allocateDirect(uSize);
            uData.put(uBuf);
            uData.rewind();
        }
        if(vBuf != null) {
            uData = ByteBuffer.allocateDirect(vSize);
            uData.put(vBuf);
            uData.rewind();
        }

        y_size = ySize;
        u_size = uSize;
        v_size = vSize;
        w = width;
        h = height;
        yuv_fmt = format;
    }
    public ByteBuffer getYData(){
        return yData;
    }

    public ByteBuffer getUData() {
        return uData;
    }
    public ByteBuffer getVData() {
        return vData;
    }

    public int getFormat() {
        return yuv_fmt;
    }

    public int getHeight() {
        return h;
    }

    public int getWidth() {
        return w;
    }

    public int getYSize() {
        return y_size;
    }

    public int getUSize() {
        return u_size;
    }

    public int getvSize() {
        return v_size;
    }

    public void setDataControlFunc(IYUVImgDataControl func) {
        mControlFunc = func;
    }

    public IYUVImgDataControl getDataControlFunc() {
        return mControlFunc;
    }

    public interface IYUVImgDataControl {
        void releaseData();
    }
}
