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

package cn.wantedonline.puppy.httpserver.component;

import cn.wantedonline.puppy.httpserver.common.CmdMappers;
import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/22.
 */
public class ContextAttachment implements ChannelFutureListener, Comparable<ContextAttachment> {

    private HttpRequest request;
    private HttpResponse response;
    private ChannelHandlerContext channelHandlerContext;
    private CmdMappers.CmdMeta cmdMeta;
    private long channelOpenTime;
    private long lastReadTime;
    private long lastWriteTime;

    private long decode;
    private long process;
    private long encode;
    private long complete;

    private volatile Thread processThread;

    private Set<Object> closeable;

    private List<Throwable> throwables;

    private volatile boolean running;

    public void registerThrowable(Throwable ex) {
        if (AssertUtil.isNotEmptyCollection(throwables)) {
            throwables = new ArrayList<>(1);
        }
        throwables.add(ex);
    }

    public boolean isProcessing() {
        return running;
    }

    public boolean isNotProcessing() {
        return !running;
    }

    public long markLastReadTime() {
        lastReadTime = System.currentTimeMillis();
        return lastReadTime;
    }

    public void markWriteBegin() {
        this.encode = this.lastWriteTime = System.currentTimeMillis();
    }

    private synchronized void _registerCloseable(Object obj) {
        if (AssertUtil.isEmptyCollection(closeable)) {
            closeable = new HashSet<>(1);
        }
        closeable.add(obj);
    }

    public void registerCloseable(Object obj) {
        _registerCloseable(obj);
    }

    public void registerCloseable(Thread t) {
        Thread.interrupted();
        _registerCloseable(t);
    }

    public synchronized void unRegisterCloseable(Object obj) {
        if (AssertUtil.isNotEmptyCollection(closeable)) {
            closeable.remove(obj);
        }
    }

    public synchronized void registerProcessThread() {
        Thread.interrupted();
        this.processThread = Thread.currentThread();
    }

    public synchronized void unRegisterProcessThread() {
        this.processThread = null;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    public HttpRequest getRequest() {
        return request;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public void setCmdMeta(CmdMappers.CmdMeta cmdMeta) {
        this.cmdMeta = cmdMeta;
    }

    public CmdMappers.CmdMeta getCmdMeta() {
        return cmdMeta;
    }

    public ContextAttachment(ChannelHandlerContext ctx) {
        this.channelHandlerContext = ctx;
        this.channelOpenTime = this.lastReadTime = this.lastWriteTime = System.currentTimeMillis();
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {

    }

    @Override
    public int compareTo(ContextAttachment o) {
        return 0;
    }

    public void registerNewMessage(HttpResponse response) {
        this.response = response;
        this.process = response.getCreateTime();
    }

    public void registerNewMessage(HttpRequest request) {
        this.request = request;
        this.decode = request.getCreateTime();
        this.lastReadTime = this.decode;
    }
}
