package com.km.cdw.androidopencvwebcam;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.km.cdw.androidopencvwebcam.Gallery.GalleryViewAdapter;

public class GalleryActivity extends AppCompatActivity {

    private GalleryViewAdapter mAdapter;
    private Button mBtn_back;
    private Button mBtn_del;
    private GridView mGrid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);
        RetainFragment retain = RetainFragment.findOrCreateRetainFragment(getSupportFragmentManager());
        mAdapter = retain.getAdapter();
        if(mAdapter == null) {
            retain.setAdapter(new GalleryViewAdapter(this));
            mAdapter = retain.getAdapter();
            mAdapter.loadContentFileList();
            mAdapter.loadImageViews();
        }

        mBtn_back = findViewById(R.id.btn_back);
        mBtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        mBtn_del = findViewById(R.id.btn_delete);
        mBtn_del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("gall del", "deleting");
                mAdapter.deleteAllElements();
            }
        });
        mGrid = findViewById(R.id.gridView_gallery);
        mGrid.setAdapter(mAdapter);
        mGrid.setOnItemClickListener(mAdapter);
        mGrid.invalidateViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
    //RetainFragment를 사용하여 그리드뷰 이미지들을 재로딩 방지, 이미지뷰들은 GalleryViewAdapter에 저장됨
    public static class RetainFragment extends Fragment {
        private static final String TAG = "RetainFragment";
        private GalleryViewAdapter mAdapter;

        public RetainFragment() {}

        //FragmentManager를 통해서 기존에 프래그먼트가 존재하면 불러온다
        public static RetainFragment findOrCreateRetainFragment(FragmentManager fm) {
            RetainFragment fragment = (RetainFragment) fm.findFragmentByTag(TAG);
            if (fragment == null) {
                fragment = new RetainFragment();
                fm.beginTransaction().add(fragment, TAG).commit();
            }
            return fragment;
        }

        public GalleryViewAdapter getAdapter() {
            return mAdapter;
        }
        public void setAdapter(GalleryViewAdapter adapter) {
            mAdapter = adapter;
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setRetainInstance(true);
        }
    }
}
