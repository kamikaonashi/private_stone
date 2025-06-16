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
    
    private static final String PERFORMANCE_GOVERNOR = "performance";
    private static final String DEFAULT_GOVERNOR = "schedutil";

    public boolean isPerformanceModeEnabled() {
        try {
            String policy0Governor = readFile(POLICY0_GOVERNOR_PATH);
            String policy6Governor = readFile(POLICY6_GOVERNOR_PATH);
            return PERFORMANCE_GOVERNOR.equals(policy0Governor.trim()) && 
                   PERFORMANCE_GOVERNOR.equals(policy6Governor.trim());
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
