/*
 * Copyright (C) 2025 KamiKaonashi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.lineageos.settings.performance;

import android.os.Bundle;
import android.os.Handler;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragment;
import androidx.preference.SwitchPreference;
import org.lineageos.settings.R;

public class PerformanceFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String KEY_PERFORMANCE_MODE = "performance_mode";
    private static final String KEY_GPU_STATUS = "gpu_status";
    private static final String KEY_LITTLE_CPU_STATUS = "little_cpu_status";
    private static final String KEY_BIG_CPU_STATUS = "big_cpu_status";
    
    private SwitchPreference mPerformanceModePreference;
    private Preference mGpuStatusPreference;
    private Preference mLittleCpuStatusPreference;
    private Preference mBigCpuStatusPreference;
    private PerformanceUtils mPerformanceUtils;
    private Handler mHandler = new Handler();

    private Runnable mUpdateRunnable = new Runnable() {
        @Override
        public void run() {
            updateGpuStatus();
            updateLittleCpuStatus();
            updateBigCpuStatus();
            mHandler.postDelayed(this, 3000);
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.performance_settings, rootKey);
        mPerformanceUtils = new PerformanceUtils();
        
        mPerformanceModePreference = (SwitchPreference) findPreference(KEY_PERFORMANCE_MODE);
        if (mPerformanceModePreference != null) {
            boolean isPerformanceMode = mPerformanceUtils.isPerformanceModeEnabled();
            mPerformanceModePreference.setChecked(isPerformanceMode);
            mPerformanceModePreference.setOnPreferenceChangeListener(this);
            updateSummary(isPerformanceMode);
        }
        
        mGpuStatusPreference = findPreference(KEY_GPU_STATUS);
        mLittleCpuStatusPreference = findPreference(KEY_LITTLE_CPU_STATUS);
        mBigCpuStatusPreference = findPreference(KEY_BIG_CPU_STATUS);

        updateGpuStatus();
        updateLittleCpuStatus();
        updateBigCpuStatus();

        mHandler.post(mUpdateRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandler.removeCallbacks(mUpdateRunnable);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (KEY_PERFORMANCE_MODE.equals(preference.getKey())) {
            boolean enabled = (Boolean) newValue;
            mPerformanceUtils.setPerformanceMode(enabled);
            updateSummary(enabled);

            mHandler.postDelayed(this::updateGpuStatus, 500);
            mHandler.postDelayed(this::updateLittleCpuStatus, 500);
            mHandler.postDelayed(this::updateBigCpuStatus, 500);
            return true;
        }
        return false;
    }

    private void updateSummary(boolean enabled) {
        if (mPerformanceModePreference != null) {
            String summary = enabled ? 
                getString(R.string.performance_mode_enabled_summary) : 
                getString(R.string.performance_mode_disabled_summary);
            mPerformanceModePreference.setSummary(summary);
        }
    }
    
    private void updateGpuStatus() {
        if (mGpuStatusPreference != null) {
            String gpuStatus = mPerformanceUtils.getGpuStatus();
            mGpuStatusPreference.setSummary(gpuStatus);
        }
    }

    private void updateLittleCpuStatus() {
        if (mLittleCpuStatusPreference != null) {
            String cpuStatus = mPerformanceUtils.getLittleCpuStatus();
            mLittleCpuStatusPreference.setSummary(cpuStatus);
        }
    }

    private void updateBigCpuStatus() {
        if (mBigCpuStatusPreference != null) {
            String cpuStatus = mPerformanceUtils.getBigCpuStatus();
            mBigCpuStatusPreference.setSummary(cpuStatus);
        }
    }
}
