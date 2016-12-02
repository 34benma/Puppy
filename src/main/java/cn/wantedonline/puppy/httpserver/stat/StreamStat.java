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

import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.util.DateStringUtil;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 *     流量统计，基于收发包的统计
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/11/30.
 */
@Component
public class StreamStat extends BaseChannelEvent {
    private StreamStatEntry inbound = new StreamStatEntry("inbound"); //收包
    private StreamStatEntry outbound = new StreamStatEntry("outbound"); //发包

    public StreamStatEntry getInbound() {
        return inbound;
    }

    public StreamStatEntry getOutbound() {
        return outbound;
    }

    /**
     * 打点一个快照数据
     * @return
     */
    public StreamStatSnapshot tickStreamStatSnapshot() {
        return new StreamStatSnapshot(inbound, outbound);
    }

    public StreamStatSnapshot tickAndReset() {
        StreamStatSnapshot snapshot = tickStreamStatSnapshot();
        reset();
        return snapshot;
    }

    public class StreamStatSnapshot {
        private Date date;
        private StreamStatEntry inbound;
        private StreamStatEntry outbound;

        public StreamStatSnapshot(StreamStatEntry inbound, StreamStatEntry outbound) {
            this.date = new Date();
            this.inbound = inbound;
            this.outbound = outbound;
        }

        public StreamStatSnapshot(Date date, StreamStatEntry inbound, StreamStatEntry outbound) {
            this.date = date;
            this.inbound = inbound;
            this.outbound = outbound;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public StreamStatEntry getInbound() {
            return inbound;
        }

        public void setInbound(StreamStatEntry inbound) {
            this.inbound = inbound;
        }

        public StreamStatEntry getOutbound() {
            return outbound;
        }

        public void setOutbound(StreamStatEntry outbound) {
            this.outbound = outbound;
        }

        @Override
        public String toString() {
            return "StreamStatSnapshot{" +
                    "date=" + DateStringUtil.DEFAULT.format(date) +
                    ", inbound=" + inbound +
                    ", outbound=" + outbound +
                    '}';
        }
    }

    @AfterConfig
    public void reset() {
        inbound.reset();
        outbound.reset();
    }

    public class StreamStatEntry {
        private String name;
        private volatile long max;
        private AtomicLong allBytes = new AtomicLong(0); //流量大小
        private AtomicLong allNum = new AtomicLong(0); //次数统计
        private double avgBytes = 0.0;  //平均包大小

        public StreamStatEntry(String name) {
            this.name = name;
            max = 0;
        }

        public void record(long bytes) {
            allNum.incrementAndGet();
            allBytes.addAndGet(bytes);
            if (bytes > max) { max = bytes; }
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getAllBytes() {
            return allBytes.get();
        }

        public void setAllBytes(long allBytes) {
            this.allBytes.set(allBytes);
        }

        public long getAllNum() {
            return allNum.get();
        }

        public void setAllNum(long allNum) {
            this.allNum.set(allNum);
        }

        public double getAvgBytes() {
            return allBytes.get() * 1.0d / allNum.get();
        }

        public void setAvgBytes(double avgBytes) {
            this.avgBytes = avgBytes;
        }

        public void reset() {
            max = 0;
            allBytes.set(0);
            allNum.set(0);
            avgBytes = 0.0d;
        }

        @Override
        public String toString() {
            return "StreamStatEntry{" +
                    "name='" + name + '\'' +
                    ", max=" + max +
                    ", allBytes=" + allBytes +
                    ", allNum=" + allNum +
                    ", avgBytes=" + avgBytes +
                    '}';
        }
    }
}
