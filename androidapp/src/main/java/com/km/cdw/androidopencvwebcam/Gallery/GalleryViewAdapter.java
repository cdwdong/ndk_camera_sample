package com.km.cdw.androidopencvwebcam.Gallery;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.km.cdw.androidopencvwebcam.Gallery.FileSystem.GalleryFileReader;
import com.km.cdw.androidopencvwebcam.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.NoSuchElementException;


public class GalleryViewAdapter extends BaseAdapter implements AdapterView.OnItemClickListener {
    public static final String TAG = "GalleryViewAdapter";

    private final Activity mActivity;
    private ArrayList<File> mContentFiles;
    private HashMap<File, ImageView> mImageViewMap;
    private BitmapCacheManager mThumbnailCache;
    private BitmapCacheManager mSourceImageCache;
    private GalleryFileReader mFileReader;

    private ImageView.OnTouchListener mPinchZoomListener;

    private Drawable mLoading_thumbnail;

    public GalleryViewAdapter(Activity activity) {

        mActivity = activity;
        mFileReader = new GalleryFileReader();
        mThumbnailCache = new BitmapCacheManager();
        mSourceImageCache = new BitmapCacheManager();

        mPinchZoomListener = new ImageMoveNZoomListener(mActivity);

        File baseDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        mFileReader.setBaseDir(baseDir);
        mLoading_thumbnail = mActivity.getDrawable(R.drawable.ic_broken_image_black_24dp);


        mImageViewMap = new HashMap<>();
    }

    // return List SIze
    public int loadContentFileList() {
        mFileReader.readGalleryFiles();
        mContentFiles = mFileReader.getFileList();

        return mContentFiles.size();
    }
    public void loadImageViews() {
        for(File f: mContentFiles) {
            ImageView imageview = new ImageView(mActivity);
            imageview.setTag(f.toString());
            imageview.setPadding(8,8,8,8);
            imageview.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageview.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.MATCH_PARENT,GridView.LayoutParams.MATCH_PARENT));
            mImageViewMap.put(f, imageview);

//            imageview.setImageDrawable(mLoading_thumbnail);
            mThumbnailCache.loadBitmap(f.toString(), f, imageview);
        }
    }

    //interface adapter
    @Override
    public int getCount() {
        if(mContentFiles == null) return 0;
        return mContentFiles.size();
    }

    @Override
    public Object getItem(int position) {
        if(mContentFiles.size() <= position) {
            throw new NoSuchElementException("해당 원소가 없음");
        }
        return mContentFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d("gridview","getview ("+position+")");

        return mImageViewMap.get(mContentFiles.get(position));
    }

    //interface OnClickEvent
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "Occurred Click");
        GridView gridView = (GridView)parent;
        ImageView imageView = (ImageView)view;

        int resId = position;

        showImageViewOnDialog(resId);

    }

    public void showImageViewOnDialog(final int resId) {
        Log.d(TAG, "showImageViewOnDialog() : resId : " + resId + " FileName : " + mContentFiles.get(resId).getName());
        Log.d(TAG, "ContentFilesSize : " + mContentFiles.size() + " gridSize : " + getCount());

        final File key = mContentFiles.get(resId);
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        ImageView imageView = new ImageView(mActivity);
        Bitmap bitmap = mSourceImageCache.getBitmapFromMemCache(key.toString());

        //Image Caching
        if(bitmap == null) {
            bitmap = BitmapFactory.decodeFile(mContentFiles.get(resId).getAbsolutePath());
            mSourceImageCache.addBitmapToMemoryCache(key.toString(), bitmap);
        }
        imageView.setImageBitmap(bitmap);
        imageView.setOnTouchListener(mPinchZoomListener);

        //Show Image Dialog
        builder.setTitle(mContentFiles.get(resId).getName());
        builder.setView(imageView);
        builder.setNeutralButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setPositiveButton("Delete",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Log.d(TAG, "delete_file_info : resId : " + resId + " FileName : " + mContentFiles.get(resId).getName());
                File target = mContentFiles.get(resId);
                //if cant not delete file, raise alert message
                if(!target.delete()) {
                    AlertDialog.Builder errorAlertBuilder = new AlertDialog.Builder(mActivity);
                    errorAlertBuilder.setMessage("Can't not Delete File");
                    errorAlertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
                    errorAlertBuilder.show();
                    // if success del, reflect thumbnail cache
                } else { deleteCacheElements(key); }
                loadContentFileList();
                notifyDataSetChanged();
            }
        });
        builder.show();
    }
    //delete only cache
    public void deleteCacheElements(File key) {
        mImageViewMap.remove(key);
        mThumbnailCache.delBitmapFromMemCache(key.toString());
        mSourceImageCache.delBitmapFromMemCache(key.toString());
    }
    public void deleteAllElements() {
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(mActivity);
        confirmBuilder.setMessage("Are you sure delete all picture?");
        confirmBuilder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                for(File e: mContentFiles) {
                    e.delete();
                    deleteCacheElements(e);
                    loadContentFileList();
                    notifyDataSetChanged();
                }
            }
        });
        confirmBuilder.setNegativeButton("Not yet", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        confirmBuilder.show();
    }
}
