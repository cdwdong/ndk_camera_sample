package com.km.cdw.ndkcamerasample.Util;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import com.km.cdw.ndkcamerasample.MainActivity;


public class ActivityUtil {
    private static final String TAG = ActivityUtil.class.getCanonicalName();
    private Activity mActivity;
    public ActivityUtil(Activity mainActivity) {
        if (mainActivity  != null) {
            mActivity = mainActivity;
        }
        else {
            Log.e(TAG, ": Activity is not Set");
        }
    }
    public void showToast(final String message) {
        if (message != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(mActivity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
    public void requestPermissions(String[] permissionList) {
        boolean permissionIsDenied = true;

        while (permissionIsDenied) {
            for(String permission : permissionList ) {
                if(ActivityCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED) {
                    permissionIsDenied = false;
                }else if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
                    showToast(permission + " 권한이 필요합니다.");
                    mActivity.finish();
                    return;
                }
            }
            if(permissionIsDenied) {
                ActivityCompat.requestPermissions(mActivity, permissionList, MainActivity.REQUEST_PERMISSION);
            }
        }

    }
}
