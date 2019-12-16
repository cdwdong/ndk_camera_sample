package com.km.cdw.androidopencvwebcam.Gallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.os.AsyncTask;
import android.util.LruCache;
import android.widget.ImageView;

import java.io.File;


public class BitmapCacheManager {
    public static final int MAX_MEMORY = (int)(Runtime.getRuntime().maxMemory() / 1024);
    public static final int CACHE_SIZE = MAX_MEMORY / 8;

    private final LruCache<String, Bitmap> mMemoryCache;
    private Drawable drawable;

    public BitmapCacheManager() {
        mMemoryCache = new LruCache<String, Bitmap>(CACHE_SIZE) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }
    public BitmapCacheManager(int cacheSize) {
        if(cacheSize >= MAX_MEMORY) {
            throw new IllegalArgumentException("given cacheSize is Bigger then allow memory size");
        }
        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap value) {
                return value.getByteCount() / 1024;
            }
        };
    }

    public void addBitmapToMemoryCache(String key, Bitmap bitmap) {

        if (getBitmapFromMemCache(key) == null) {
            mMemoryCache.put(String.valueOf(key.hashCode()), bitmap);
        }
    }

    public Bitmap getBitmapFromMemCache(String key) {
        return mMemoryCache.get(String.valueOf(key.hashCode()));
    }
    public void delBitmapFromMemCache(String key) {
        if (getBitmapFromMemCache(key) != null) mMemoryCache.remove(String.valueOf(key.hashCode()));
    }

    public void loadBitmap(String resId, File file, ImageView imageView) {
        final String imageKey = String.valueOf(resId.hashCode());
        final Bitmap bitmap = getBitmapFromMemCache(imageKey);

        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            BitmapWorkerTask task = new BitmapWorkerTask(imageKey, file, imageView);
            task.execute();

        }
    }

    //AsyncTask for Decode Image File and apply image to ImageView
    class BitmapWorkerTask extends AsyncTask<Integer, Void, Bitmap> {

        private final String mHashId;
        private final File mFile;
        private final ImageView mImageView;


        public BitmapWorkerTask(String hashId, File file, ImageView imageView) {
            mHashId = hashId;
            mFile = file;
            mImageView = imageView;
        }
        @Override
        protected Bitmap doInBackground(Integer... params) {
            if(mImageView.isEnabled()) {
                final Bitmap bitmap = BitmapFactory.decodeFile(mFile.toString());
                final Bitmap thumbnail = ThumbnailUtils.extractThumbnail(bitmap, 300, 300);

                addBitmapToMemoryCache(String.valueOf(mHashId), thumbnail);
                return thumbnail;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if(bitmap != null) {
                mImageView.setImageBitmap(bitmap);
            }
        }
    }
}
