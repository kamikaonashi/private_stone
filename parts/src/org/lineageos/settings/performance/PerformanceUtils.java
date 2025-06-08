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

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class PerformanceUtils {

    private static final String POLICY0_GOVERNOR_PATH = "/sys/devices/system/cpu/cpufreq/policy0/scaling_governor";
    private static final String POLICY6_GOVERNOR_PATH = "/sys/devices/system/cpu/cpufreq/policy6/scaling_governor";
    private static final String POLICY0_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpufreq/policy0/scaling_cur_freq";
    private static final String POLICY6_CUR_FREQ_PATH = "/sys/devices/system/cpu/cpufreq/policy6/scaling_cur_freq";
    
    private static final String PERFORMANCE_GOVERNOR = "performance";
    private static final String DEFAULT_GOVERNOR = "schedutil";

    private static final String GPU_MAX_CLOCK_PATH = "/sys/class/kgsl/kgsl-3d0/max_clock_mhz";
    private static final String GPU_MIN_CLOCK_PATH = "/sys/class/kgsl/kgsl-3d0/min_clock_mhz";
    private static final String GPU_DEFAULT_PWRLEVEL_PATH = "/sys/class/kgsl/kgsl-3d0/default_pwrlevel";
    private static final String GPU_FORCE_CLK_ON_PATH = "/sys/class/kgsl/kgsl-3d0/force_clk_on";
    private static final String GPU_FORCE_RAIL_ON_PATH = "/sys/class/kgsl/kgsl-3d0/force_rail_on";

    private static final String GPU_MAX_FREQ = "840";
    private static final String GPU_MIN_FREQ_DEFAULT = "266";
    private static final String GPU_DEFAULT_POWER_LEVEL = "5";

    public boolean isPerformanceModeEnabled() {
        try {
            String policy0Governor = readFile(POLICY0_GOVERNOR_PATH);
            String policy6Governor = readFile(POLICY6_GOVERNOR_PATH);
            boolean cpuPerformance = PERFORMANCE_GOVERNOR.equals(policy0Governor.trim()) && 
                                   PERFORMANCE_GOVERNOR.equals(policy6Governor.trim());

            String gpuForceClk = readFile(GPU_FORCE_CLK_ON_PATH);
            boolean gpuPerformance = "1".equals(gpuForceClk.trim());
            
            return cpuPerformance && gpuPerformance;
        } catch (Exception e) {
            return false;
        }
    }

    public void setPerformanceMode(boolean enabled) {
        String cpuGovernor = enabled ? PERFORMANCE_GOVERNOR : DEFAULT_GOVERNOR;
        try {
            writeFile(POLICY0_GOVERNOR_PATH, cpuGovernor);
            writeFile(POLICY6_GOVERNOR_PATH, cpuGovernor);
        } catch (Exception e) {
            // Intentionally ignoring
        }

        try {
            if (enabled) {
                writeFile(GPU_FORCE_CLK_ON_PATH, "1");
                writeFile(GPU_FORCE_RAIL_ON_PATH, "1");
                writeFile(GPU_DEFAULT_PWRLEVEL_PATH, "0");
                writeFile(GPU_MIN_CLOCK_PATH, GPU_MAX_FREQ);
            } else {
                writeFile(GPU_FORCE_CLK_ON_PATH, "0");
                writeFile(GPU_FORCE_RAIL_ON_PATH, "0");
                writeFile(GPU_DEFAULT_PWRLEVEL_PATH, GPU_DEFAULT_POWER_LEVEL);
                writeFile(GPU_MIN_CLOCK_PATH, GPU_MIN_FREQ_DEFAULT);
            }
        } catch (Exception e) {
            // Intentionally ignoring
        }
    }
    
    public String getGpuStatus() {
        try {
            String currentMinFreq = readFile(GPU_MIN_CLOCK_PATH);
            String maxFreq = readFile(GPU_MAX_CLOCK_PATH);
            String forceClk = readFile(GPU_FORCE_CLK_ON_PATH);
            String pwrLevel = readFile(GPU_DEFAULT_PWRLEVEL_PATH);
            
            return String.format("Min: %s MHz, Max: %s MHz, Force CLK: %s, Power Level: %s", 
                               currentMinFreq.trim(), maxFreq.trim(), forceClk.trim(), pwrLevel.trim());
        } catch (Exception e) {
            return "GPU status unavailable";
        }
    }

    public String getLittleCpuStatus() {
        try {
            String curFreqHz = readFile(POLICY0_CUR_FREQ_PATH);
            String governor = readFile(POLICY0_GOVERNOR_PATH);

            int curFreqMHz = Integer.parseInt(curFreqHz.trim()) / 1000;
            
            return String.format("Current: %d MHz, Governor: %s", curFreqMHz, governor.trim());
        } catch (Exception e) {
            return "Little CPU status unavailable";
        }
    }

    public String getBigCpuStatus() {
        try {
            String curFreqHz = readFile(POLICY6_CUR_FREQ_PATH);
            String governor = readFile(POLICY6_GOVERNOR_PATH);

            int curFreqMHz = Integer.parseInt(curFreqHz.trim()) / 1000;
            
            return String.format("Current: %d MHz, Governor: %s", curFreqMHz, governor.trim());
        } catch (Exception e) {
            return "Big CPU status unavailable";
        }
    }

    private String readFile(String path) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(path));
            return reader.readLine();
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }

    private void writeFile(String path, String value) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            writer.write(value);
            writer.flush();
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
