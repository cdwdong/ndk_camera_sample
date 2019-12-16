package com.km.cdw.androidopencvwebcam;

public class ThreadProcessor {
    private Thread mThread;
    private Runnable mRunnable;
    private String mName;

    public void setParams(Runnable r, String name) {
        mRunnable = r;
        mName = name;
    }
    public void startThread(boolean isDeamon) {
        mThread = new Thread(mRunnable, mName);
        mThread.setDaemon(isDeamon);
        mThread.start();
    }
    public void stopThread() throws InterruptedException {
        if(mThread != null){
            mThread.interrupt();
            mThread.join();
            mThread = null;
        }
    }
    public boolean isAlive() {
        return mThread.isAlive();
    }
}
