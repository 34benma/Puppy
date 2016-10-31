/*
 * Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.wantedonline.puppy.httpserver.util.ch.qos.logback;

import ch.qos.logback.core.Layout;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.TreeMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;

/**
 * Created by wangcheng on 2016/10/31.
 */
public class FixSizeMemAppender<E> extends UnsynchronizedAppenderBase<E> {
    public static class FixSizeLog {
        private String loggerName;
        private String[] bufferedLog;
        private AtomicInteger index = new AtomicInteger(0);
        private int size;

        protected Layout<E> layout;
        protected int size = 100;
        protected int log_size = 500;
        private Executor exe = Executors.newSingleThreadExecutor();

        private FixSizeLog() {}

        public FixSizeLog(String loggerName, int size) {
            this.loggerName = loggerName;
            this.size = size;
            this.bufferedLog = new String[size];
        }

        public void log(String msg) {
            bufferedLog[index.getAndIncrement() % size] = msg;
        }

        @Override
        public String toString() {
            return sub(0,size);
        }

        public String tail(int num) {
            int end = index.get();
            int begin = end - num;
            if (begin < 0) {
                begin = 0;
            }
            return sub(begin, end);
        }

        public String sub(int begin, int end) {
            if (begin < 0 || end > size) {
                throw new IndexOutOfBoundsException();
            }
            StringBuilder sb = new StringBuilder();
            int offset = index.get();
            if (offset <= size) {
                end = Math.min(end, offset);
                offset = 0;
            }
            for (int i = begin; i < end; i++) {
                int idx = (i+offset)%size;
                String item = bufferedLog[idx];
                if (null != item) {
                    sb.append(item);
                }
            }
            return sb.toString();
        }

        public String getLoggerName() {
            return loggerName;
        }

        public void setSize(int size) {}
    }

    private static final Map<String, FixSizeLog> FIX_SIZE_LOG_MAP = new TreeMap<>();

    public static FixSizeLog getFixSizeLog(String loggerName) {
        return FIX_SIZE_LOG_MAP.get(loggerName);
    }

    public static FixSizeLog getFixSizeLog(Logger log) {
        return FIX_SIZE_LOG_MAP.get(log.getName());
    }

    public static Collection<FixSizeLog> getAllLog() {
        return FIX_SIZE_LOG_MAP.values();
    }

    public static class ScaleableLog extends FixSizeLog {
        private String loggerName;
        private Concurrent
    }
}
