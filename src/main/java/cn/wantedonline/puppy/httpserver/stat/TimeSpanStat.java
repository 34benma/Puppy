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

package cn.wantedonline.puppy.httpserver.stat;

import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.Log;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 *     编解码等各种处理时长统计，基于时间维度
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 2016/11/29
 */
@Component
public class TimeSpanStat extends BaseChannelEvent {
    private Logger log = Log.getLogger(TimeSpanStat.class);

    @Config(resetable = true)
    public int slow_decode_threshold = 100;
    @Config(resetable = true)
    public int slow_encode_threshold = 500;
    @Config(resetable = true)
    public int slow_req_threshold = 1000;

    private TimeSpanStatEntry allTSS; // 全部统计
    private TimeSpanStatEntry decodeTSS; // 解码统计
    private TimeSpanStatEntry encodeTSS; // 编码统计
    private TimeSpanStatEntry processTSS; // 业务统计
    private TimeSpanStatEntry okTSS; // 处理完毕

    public TimeSpanSnapshot tickTimeSpanSnapshot() {
        TimeSpanSnapshot snapshot = new TimeSpanSnapshot();
        snapshot.setAllTSS(allTSS);
        snapshot.setDecodeTSS(decodeTSS);
        snapshot.setEncodeTSS(encodeTSS);
        snapshot.setOkTSS(okTSS);
        snapshot.setProcessTSS(processTSS);
        return snapshot;
    }

    public TimeSpanSnapshot tickAndReset() {
        TimeSpanSnapshot snapshot = tickTimeSpanSnapshot();
        reset();
        return snapshot;
    }

    public class TimeSpanSnapshot {
        private Date date;
        private TimeSpanStatEntry allTSS; // 全部统计
        private TimeSpanStatEntry decodeTSS; // 解码统计
        private TimeSpanStatEntry encodeTSS; // 编码统计
        private TimeSpanStatEntry processTSS; // 业务统计
        private TimeSpanStatEntry okTSS; // 处理完毕

        public TimeSpanSnapshot() {
            this.date = new Date();
        }

        public TimeSpanStatEntry getAllTSS() {
            return allTSS;
        }

        public void setAllTSS(TimeSpanStatEntry allTSS) {
            this.allTSS = allTSS;
        }

        public TimeSpanStatEntry getDecodeTSS() {
            return decodeTSS;
        }

        public void setDecodeTSS(TimeSpanStatEntry decodeTSS) {
            this.decodeTSS = decodeTSS;
        }

        public TimeSpanStatEntry getEncodeTSS() {
            return encodeTSS;
        }

        public void setEncodeTSS(TimeSpanStatEntry encodeTSS) {
            this.encodeTSS = encodeTSS;
        }

        public TimeSpanStatEntry getProcessTSS() {
            return processTSS;
        }

        public void setProcessTSS(TimeSpanStatEntry processTSS) {
            this.processTSS = processTSS;
        }

        public TimeSpanStatEntry getOkTSS() {
            return okTSS;
        }

        public void setOkTSS(TimeSpanStatEntry okTSS) {
            this.okTSS = okTSS;
        }

        @Override
        public String toString() {
            return "TimeSpanSnapshot{" +
                    "date=" + DateStringUtil.DEFAULT.format(date) +
                    ", allTSS=" + allTSS +
                    ", decodeTSS=" + decodeTSS +
                    ", encodeTSS=" + encodeTSS +
                    ", processTSS=" + processTSS +
                    ", okTSS=" + okTSS +
                    '}';
        }
    }

    @Override
    public void writeBegin(ContextAttachment attach) {
        attach.markWriteBegin();
        processTSS.record(attach.getEncode(), attach.getProcess(), attach.getRequest().getUri(), attach);
    }

    @Override
    public void writeEnd(ContextAttachment attach) {
        HttpResponse resp = attach.getResponse();
        if (resp == null) {// https 情况，一开始不会有 resp
            return;
        }
        // TODO:这里没有处理chunk的情况
        long incr = attach.markWriteEnd();
        long complete = attach.getComplete();

        if (incr == -1) {
            // 说明是第一次写
            encodeTSS.record(complete, attach.getEncode(), attach.getRequest().getUri(),attach);
            allTSS.record(complete, attach.getDecode(), attach.getRequest().getUri(),attach);
        } else {
            encodeTSS.record(incr, complete, attach.getEncode(), attach.getRequest().getUri(), attach);
            allTSS.record(incr, complete, attach.getDecode(), attach.getRequest().getUri(), attach);
            if (resp.getStatus().equals(HttpResponseStatus.OK)) {
                okTSS.record(incr, complete, attach.getDecode(), attach.getRequest().getUri(), attach);
            }
        }
    }

    @AfterConfig
    public void reset() {
        decodeTSS = new TimeSpanStatEntry("decode",slow_decode_threshold, true, log);
        processTSS = new TimeSpanStatEntry("process", slow_req_threshold, true, log);
        encodeTSS = new TimeSpanStatEntry("encode", slow_encode_threshold, true, log);
        allTSS = new TimeSpanStatEntry("all",slow_req_threshold, true, log);
        okTSS = new TimeSpanStatEntry("200OK", slow_req_threshold, true, log);
    }

    public TimeSpanStatEntry getAllTSS() {
        return allTSS;
    }

    public TimeSpanStatEntry getDecodeTSS() {
        return decodeTSS;
    }

    public TimeSpanStatEntry getEncodeTSS() {
        return encodeTSS;
    }

    public TimeSpanStatEntry getProcessTSS() {
        return processTSS;
    }

    public TimeSpanStatEntry getOkTSS() {
        return okTSS;
    }

    public class TimeSpanStatEntry {
        private AtomicLong totalCount = new AtomicLong(); // 处理总次数
        private AtomicLong totalSpan = new AtomicLong(); // 处理总时长
        private Logger log;
        private volatile long maxSpan;
        private String name = "";
        private AtomicLong slowCount = new AtomicLong(); // 慢的总个数
        private AtomicLong slowSpan = new AtomicLong(); // 慢的总时长
        private int slowThreshold;
        private boolean warn;

        public TimeSpanStatEntry(String name, int slowThreshold, boolean warn, Logger log) {
            this.name = name;
            this.slowThreshold = slowThreshold;
            this.log = log;
            this.warn = warn;
        }

        public TimeSpanStatEntry(String name, Logger log) {
            this(name, 1000, true, log);
        }

        public long getTotalCount() {

            return totalCount.get();
        }

        public long getTotalSpan() {
            return totalSpan.get();
        }

        public long getSlowCount() {
            return slowCount.get();
        }

        public long getSlowSpan() {
            return slowSpan.get();
        }

        /**
         * 提取最大时长
         */
        public long getMaxSpan() {
            return maxSpan;
        }

        public void record(long end, long begin, String uri, Object arg) {
            if (begin <= 0 || end <= 0) {
                return;
            }
            totalCount.incrementAndGet();
            long span = end - begin;
            totalSpan.addAndGet(span);
            if (span >= slowThreshold) {
                slowCount.incrementAndGet();
                slowSpan.addAndGet(span);
                if (warn) {
                    warn(end, begin, uri, arg);
                }
            }
            if (span > maxSpan) {
                maxSpan = span;
            }
        }

        /**
         * 针对不是一次发送的情况
         * @param incr
         * @param end
         * @param begin
         * @param uri
         * @param arg
         */
        public void record(long incr, long end, long begin, String uri, Object arg) {
            if (incr <= 0 || end <= 0 || begin <= 0) {
                return;
            }
            long span = end - begin;
            long lastSpan = span - incr;
            totalSpan.addAndGet(incr);
            if (span >= slowThreshold) {
                if (lastSpan >= slowThreshold) {
                    slowSpan.addAndGet(incr);
                    if (warn) {
                        warn(end, begin, uri, arg);
                    }
                } else {
                    slowSpan.addAndGet(span);
                    slowCount.incrementAndGet();
                }
            }
            if (span > maxSpan) {
                maxSpan = span;
            }
        }

        @Override
        public String toString() {
            return "TimeSpanStatEntry{" +
                    "name='" + name + '\'' +
                    ", totalCount=" + totalCount +
                    ", totalSpan=" + totalSpan +
                    ", maxSpan=" + maxSpan +
                    ", slowCount=" + slowCount +
                    ", slowSpan=" + slowSpan +
                    ", slowThreshold=" + slowThreshold +
                    '}';
        }

        protected void warn(long end, long begin, String uri, Object arg) {
            log.warn("SLOW_PROCESS FOR {}:{}:{} [{}ms]\n", uri, new Object[]{
                    name,
                    arg,
                    end - begin
            });


        }

        public boolean isNeedReset() {
            return totalCount.get() < 0 || totalSpan.get() < 0;
        }

    }

}
