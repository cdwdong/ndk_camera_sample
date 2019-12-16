package com.km.cdw.androidopencvwebcam;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.km.cdw.androidopencvwebcam.Network.Http.GsonSettingType;
import com.km.cdw.androidopencvwebcam.Network.SocketIo.SocketIoListeners;
import com.km.cdw.androidopencvwebcam.Network.SocketIo.SocketIoManager;
import com.km.cdw.androidopencvwebcam.Preference.SharedPreferenceController;

import java.util.HashMap;
import java.util.Map;

public class CameraSettingsActivity extends AppCompatActivity {
    public static final String TAG = "CameraSettingsActivity";
    private View mBtn_back;
    private Intent mIntent;
    private SettingsFragment mSettingsFragment = new SettingsFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.settings, mSettingsFragment)
                .commit();
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mBtn_back = findViewById(R.id.btn_back_2);
        mBtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        GsonSettingType data = new GsonSettingType();
        data.filter = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("filter", "filter_default");
        data.detect = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("detect", "detect_default");
//        HttpReqSendTask.executePostReqTask(this, data);
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private SharedPreferenceController mController;
        private SocketIoManager mSocketIoManager = SocketIoManager.getInstance();
        private ListPreference mFilter;
        private ListPreference mDetect;
        private SharedPreferences.OnSharedPreferenceChangeListener mSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if(mFilter.getKey().equals(key)) {
                    mFilter.setValue(sharedPreferences.getString(key, "filter_default"));
                } else if(mDetect.getKey().equals(key)) {
                    mDetect.setValue(sharedPreferences.getString(key, "detect_default"));
                }
            }
        };
        private Preference.OnPreferenceChangeListener mPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                Map<String, String> data = new HashMap<>();
                mController.getSettingData(data);
                data.put(preference.getKey(), (String)newValue);
                mSocketIoManager.requestPostSettingData(data);

                return true;
            }
        };

        @Override
        public void onResume() {
            super.onResume();
            mSocketIoManager.connect();
            mSocketIoManager.requestGetSettingData();
        }

        @Override
        public void onPause() {
            super.onPause();
            mSocketIoManager.disconnect();
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            //network
            mController = new SharedPreferenceController(getContext());
            SocketIoListeners listeners = new SocketIoListeners(mController);
            mSocketIoManager.onConnectListener(listeners.getOnConnectListener());
            mSocketIoManager.onDisconnectListener(listeners.getOnDisconnectListener());
            mSocketIoManager.onListener("response setting data", listeners.getResponseSettingDataListener());

            mFilter = findPreference("filter");
            mDetect = findPreference("detect");

            mFilter.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                @Override
                public CharSequence provideSummary(ListPreference preference) {
                    String entry = "";
                    if(preference.getEntry() != null) {
                        entry = preference.getEntry().toString();
                    }

                    return entry + " Effect Mod";
                }
            });
            mDetect.setSummaryProvider(new Preference.SummaryProvider<ListPreference>() {
                @Override
                public CharSequence provideSummary(ListPreference preference) {
                    String entry = "";
                    if(preference.getEntry() != null) {
                        entry = preference.getEntry().toString();
                    }

                    return entry + " Detect Mod";
                }
            });
            getSharedPreferences().registerOnSharedPreferenceChangeListener(mSharedPreferenceChangeListener);

            mFilter.setOnPreferenceChangeListener(mPreferenceChangeListener);
            mDetect.setOnPreferenceChangeListener(mPreferenceChangeListener);



        }
        public SharedPreferences getSharedPreferences() {
            return getPreferenceManager().getSharedPreferences();
        }
    }

}