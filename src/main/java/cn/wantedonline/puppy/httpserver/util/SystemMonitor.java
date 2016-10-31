/*
 *  Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */

package cn.wantedonline.puppy.httpserver.util;

import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.util.Map;

/**
 * Created by louiswang on 16/10/31.
 */
public class SystemMonitor {
    private SystemMonitor() {}

    private static final Logger logger = Log.getLogger();
    private static final Logger logger_loadaverage = Log.getLoggerWithSuffix("loadaverage");
    private static Map<Long, Long> calcThreadsCpuTime;
    private static boolean loadAverageHigh = true;

    public static boolean initThreadCpuTimeMonitor(int interval) {
        if (interval < 0) {
            return false;
        }
        final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        boolean cpuTimeEnable = false;


    }
}
