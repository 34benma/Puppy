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

package cn.wantedonline.puppy.httpserver.util.ch.qos.logback;

import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by louiswang on 16/10/31.
 */
public class FixNameMemAppender<E> extends UnsynchronizedAppenderBase<E> {
    public static interface FixNameLog {
        public abstract void log(String msg);
        public abstract String getLoggerName();
    }

    private static final Map<String, FixNameLog> FIX_NAME_LOG_MAP = new LinkedHashMap<>();

    protected Layout<E> layout;

    public static void register(FixNameLog fnl) {
        String name = fnl.getLoggerName();
        synchronized (FIX_NAME_LOG_MAP) {
            if (FIX_NAME_LOG_MAP.containsKey(name)) {
                throw new IllegalAccessError("cant register FixNameLog:[" + name + "]" + fnl);
            }
            FIX_NAME_LOG_MAP.put(name, fnl);
        }
    }

    @Override
    protected void append(E e) {
        if (e instanceof LoggingEvent) {
            LoggingEvent le = (LoggingEvent) e;
            String loggerName = le.getLoggerName();
            FixNameLog fnl = FIX_NAME_LOG_MAP.get(loggerName);
            if (null != fnl) {
                String msg = layout.doLayout(e);
                fnl.log(msg);
            }
        }
    }
}
