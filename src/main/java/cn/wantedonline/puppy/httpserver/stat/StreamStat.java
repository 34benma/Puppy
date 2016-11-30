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

import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;

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
public class StreamStat implements ChannelEvent {
    private StreamStatEntry inbound = new StreamStatEntry("inbound"); //收包
    private StreamStatEntry outbound = new StreamStatEntry("outbound"); //发包

    public StreamStatEntry getInbound() {
        return inbound;
    }

    public StreamStatEntry getOutbound() {
        return outbound;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {

    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {

    }

    public class StreamStatEntry {
        private String name;
        private volatile long max;
        private AtomicLong allBytes = new AtomicLong(0); //流量大小
        private AtomicLong allNum = new AtomicLong(0); //次数统计

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
    }
}
