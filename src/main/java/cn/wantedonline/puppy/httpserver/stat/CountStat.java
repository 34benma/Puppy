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
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.util.DateStringUtil;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <pre>
 *     基本的请求，响应次数等统计,基于次数
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/11/30.
 */
@Component
public class CountStat extends BaseChannelEvent {
    private AtomicLong totalRegChannel = new AtomicLong(0); //总共注册的通道数
    private AtomicLong totalUnregChannel = new AtomicLong(0); //总共去注册通道数
    private AtomicLong totalActiveChannel = new AtomicLong(0); //通道激活总数
    private AtomicLong totalInActiveChannel = new AtomicLong(0); //通道去激活总数
    private AtomicLong totalReqCount = new AtomicLong(0); //接受Http请求总次数
    private AtomicLong totalRespCount = new AtomicLong(0); //响应Http请求总次数
    private AtomicLong exceptionCount = new AtomicLong(0); //发生异常的总次数

    public CountStatSnapshot tickCountStatSnapshot() {
        CountStatSnapshot snapshot = new CountStatSnapshot();
        snapshot.setTotalRegChannel(totalRegChannel);
        snapshot.setTotalUnregChannel(totalUnregChannel);
        snapshot.setTotalActiveChannel(totalActiveChannel);
        snapshot.setTotalInActiveChannel(totalInActiveChannel);
        snapshot.setTotalReqCount(totalReqCount);
        snapshot.setTotalRespCount(totalRespCount);
        snapshot.setExceptionCount(exceptionCount);
        return snapshot;
    }

    public CountStatSnapshot tickAndReset() {
        CountStatSnapshot snapshot = tickCountStatSnapshot();
        reset();
        return snapshot;
    }

    public class CountStatSnapshot {
        private AtomicLong totalRegChannel = new AtomicLong(0); //总共注册的通道数
        private AtomicLong totalUnregChannel = new AtomicLong(0); //总共去注册通道数
        private AtomicLong totalActiveChannel = new AtomicLong(0); //通道激活总数
        private AtomicLong totalInActiveChannel = new AtomicLong(0); //通道去激活总数
        private AtomicLong totalReqCount = new AtomicLong(0); //接受Http请求总次数
        private AtomicLong totalRespCount = new AtomicLong(0); //响应Http请求总次数
        private AtomicLong exceptionCount = new AtomicLong(0); //发生异常的总次数
        private Date date = new Date();

        public CountStatSnapshot() {
            this.date = new Date();
        }

        public AtomicLong getTotalRegChannel() {
            return totalRegChannel;
        }

        public void setTotalRegChannel(AtomicLong totalRegChannel) {
            this.totalRegChannel = totalRegChannel;
        }

        public AtomicLong getTotalUnregChannel() {
            return totalUnregChannel;
        }

        public void setTotalUnregChannel(AtomicLong totalUnregChannel) {
            this.totalUnregChannel = totalUnregChannel;
        }

        public AtomicLong getTotalActiveChannel() {
            return totalActiveChannel;
        }

        public void setTotalActiveChannel(AtomicLong totalActiveChannel) {
            this.totalActiveChannel = totalActiveChannel;
        }

        public AtomicLong getTotalInActiveChannel() {
            return totalInActiveChannel;
        }

        public void setTotalInActiveChannel(AtomicLong totalInActiveChannel) {
            this.totalInActiveChannel = totalInActiveChannel;
        }

        public AtomicLong getTotalReqCount() {
            return totalReqCount;
        }

        public void setTotalReqCount(AtomicLong totalReqCount) {
            this.totalReqCount = totalReqCount;
        }

        public AtomicLong getTotalRespCount() {
            return totalRespCount;
        }

        public void setTotalRespCount(AtomicLong totalRespCount) {
            this.totalRespCount = totalRespCount;
        }

        public AtomicLong getExceptionCount() {
            return exceptionCount;
        }

        public void setExceptionCount(AtomicLong exceptionCount) {
            this.exceptionCount = exceptionCount;
        }

        @Override
        public String toString() {
            return "CountStatSnapshot{" +
                    "date=" + DateStringUtil.DEFAULT.format(date) +
                    ", totalRegChannel=" + totalRegChannel +
                    ", totalUnregChannel=" + totalUnregChannel +
                    ", totalActiveChannel=" + totalActiveChannel +
                    ", totalInActiveChannel=" + totalInActiveChannel +
                    ", totalReqCount=" + totalReqCount +
                    ", totalRespCount=" + totalRespCount +
                    ", exceptionCount=" + exceptionCount +
                    '}';
        }
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        totalRegChannel.incrementAndGet();
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        totalUnregChannel.incrementAndGet();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        totalActiveChannel.incrementAndGet();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        totalInActiveChannel.incrementAndGet();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        exceptionCount.incrementAndGet();
    }

    public void requestReceived(ChannelHandlerContext ctx, ContextAttachment attach) {
        totalReqCount.incrementAndGet();
    }

    public void responseSended(ChannelHandlerContext ctx, ContextAttachment attach) {
        totalRespCount.incrementAndGet();
    }

    public long getTotalRegChannel() {
        return totalRegChannel.get();
    }

    public void setTotalRegChannel(long totalRegChannel) {
        this.totalRegChannel.set(totalRegChannel);
    }

    public long getTotalUnregChannel() {
        return totalUnregChannel.get();
    }

    public void setTotalUnregChannel(long totalUnregChannel) {
        this.totalUnregChannel.set(totalUnregChannel);
    }

    public long getTotalActiveChannel() {
        return totalActiveChannel.get();
    }

    public void setTotalActiveChannel(long totalActiveChannel) {
        this.totalActiveChannel.set(totalActiveChannel);
    }

    public long getTotalInActiveChannel() {
        return totalInActiveChannel.get();
    }

    public void setTotalInActiveChannel(long totalInActiveChannel) {
        this.totalInActiveChannel.set(totalInActiveChannel);
    }

    public long getTotalReqCount() {
        return totalReqCount.get();
    }

    public void setTotalReqCount(long totalReqCount) {
        this.totalReqCount.set(totalReqCount);
    }

    public long getTotalRespCount() {
        return totalRespCount.get();
    }

    public void setTotalRespCount(long totalRespCount) {
        this.totalRespCount.set(totalRespCount);
    }

    public long getExceptionCount() {
        return exceptionCount.get();
    }

    public void setExceptionCount(long exceptionCount) {
        this.exceptionCount.set(exceptionCount);
    }

    @AfterConfig
    public void reset() {
        totalRegChannel.set(0);
        totalUnregChannel.set(0);
        totalActiveChannel.set(0);
        totalInActiveChannel.set(0);
        totalReqCount.set(0);
        totalRespCount.set(0);
        exceptionCount.set(0);
    }
}
