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

package cn.wantedonline.puppy.httpserver.component;

import cn.wantedonline.puppy.httpserver.cmd.CmdMappers;
import cn.wantedonline.puppy.httpserver.util.HumanReadableUtil;
import cn.wantedonline.puppy.httpserver.util.StringHelper;
import cn.wantedonline.puppy.util.CloseableHelper;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.EmptyChecker;
import cn.wantedonline.puppy.util.Log;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;

import java.util.*;

/**
 * <pre>
 * 开始解码		attach.decode (req.createTime)
 * 									} 解码用时
 * 开始业务处理	attach.process (resp.createTime)
 * 									} 业务处理用时
 * 开始编码		attach.encode
 * 									} 编码用时
 * 发送完成		attach.complete
 *
 * 2011-03-19 整理优化attach
 *
 * 在具体的业务disaptcher中
 *
 * channelOpen时,新建attach 这里直接注册一样生命周期的context  可选：[channelBound mark lastIoTime］[channelConn时,说明其准备 decode mark lastIoTime]
 * messageReceived时,说明其decode完毕,mark lastIoTime (这里能通过httpReq来找到其decode的开始时间,其resp的createTime就是跟当前lastIoTime一致),并注册最新的httpReq,httpResp(可以注册最新的messageEvent)
 *
 * disaptch时,注册其业务线程,说明此线程可能会有长时间业务操作
 * 业务处理完成时,注销其业务线程
 * 真正wirteResponse时,是其开始encode的信号,因此mark lastIoTime(写)
 *
 * writeComplete时,说明其已经encode完成,mark lastIoTime(写)
 *
 * channelClose时,直接注销此attach,这是其生命周期终点
 *
 *
 * 在外部打描器中,发现有业务线程,则通过判断cmdMeta的timeout来判断是否中断
 * 没有业务线程,则是判断其ioTime
 *
 */
public class ContextAttachment implements ChannelFutureListener, Comparable<ContextAttachment> {
    private static final Logger log = Log.getLogger();

    /**
     * 保存了最近一次当前channel的request对象
     */
    private HttpRequest request;
    /**
     * 保存了最近一次当前channel的request对象
     */
    private HttpResponse response;
    /**
     * attach跟ctx的生命周期是一致的,在初始化时绑定
     */
    private ChannelHandlerContext channelHandlerContext;
    // private MessageEvent messageEvent;
    /**
     * 保存最近一次在dispatch中使用的cmdMeta
     */
    private CmdMappers.CmdMeta cmdMeta;

    private long channelOpenTime;
    /**
     * 当前channel已知上次读时间戳
     */
    private long lastReadTime;
    /**
     * 当前channel已知上次写时间戳
     */
    private long lastWriteTime;

    public long markLastReadTime() {
        lastReadTime = System.currentTimeMillis();
        return lastReadTime;
    }

    /**
     * 设置其业务线程
     */
    private volatile Thread processThread;

    /*
     * decode process encode complete 是为了统计使用
     */
    private long decode;
    private long process;
    private long encode;
    private long complete;

    /**
     * <pre>
     * 保存最近一次请求在业务处理时需要额外close的对象
     *
     * 在TimeoutThreadInterrupter发生中断时,也会把此容器内的所有对象进行close
     */
    private Set<Object> closeable;
    /**
     * <pre>
     * 保存最近一次请求
     * 在处理过程中,发生错误时,都收集到这里
     */
    private List<Throwable> throwables;
    /**
     * 用于保存在业务流程中需要保存的中间对象，如果有多个，内部可以用Object[]/List<Object>/Map<String,Object>来表达
     */
    private Object innerAttach;
    private MessageEvent[] asyncMessageEventQueue;
    private int asyncMessageEventQueueCounter;
    private volatile boolean running;// attach从channelOpen 到channelWriteComplete，标记为running
    private boolean closeAfterOperationComplete = true;
    private int respOperationCompleteCount = 0; // 2012-06-01 当前attach正常处理了几个回包，用于打开keep-alive时，统计回包数
    /**
     * <pre>
     * http://www.blaze.io/mobile/http-pipelining-big-in-mobile/
     *
     * Recovery On “Connection: Close”
     *
     * When using pipelining, the server may always close the connection before all requests are fulfilled.
     * For example, if requests 1 and 2 are sent on the same connection, the response to request 1 may include
     * a “Connection: close” header. In this case, the browser will honor the close instruction and close the
     * connection, without a response to request 2, and resent request two on a different connection.
     * It’s worth noting that getting such a “Connection: Close” will not make Opera stop using pipelining on
     * that host, even if all future requests come back with such a “close” header.
     */
    private boolean userAgentTryPipelining = false;
    private static final String HTTP_HEADER_KEEP_ALIVE = "Keep-Alive";

    private AsyncCallbackAttach asyncCallbackAttach;

    public void setAsyncCallbackAttach(AsyncCallbackAttach asyncCallbackAttach) {
        this.asyncCallbackAttach = asyncCallbackAttach;
    }

    /**
     * 获取异步请求时发送的消息列表，按代码里面写的发送顺序提交
     */
    public List<SequenceMessage> getAsyncReqMessageList() {
        return null == asyncCallbackAttach ? Collections.<SequenceMessage> emptyList() : asyncCallbackAttach.getMessageList();
    }

    public void initAsyncMessageEventQueue(int concurrentNum) {
        if (concurrentNum > 0) {
            this.asyncMessageEventQueue = new MessageEvent[concurrentNum];
            this.asyncMessageEventQueueCounter = concurrentNum;
        }
    }

    /**
     * 保存并发获得的异步messageEvent,如果所有请求都有响应，则会返回true（由于有可能同时几个返回进来，此处应当进行同步处理）
     */
    public synchronized boolean asyncMessageEventReceived(MessageEvent e) {
        if (asyncMessageEventQueue == null) { // 如果没有初始化，则默认是1个
            initAsyncMessageEventQueue(1);
        }
        int idx = --asyncMessageEventQueueCounter;
        asyncMessageEventQueue[idx] = e;
        return idx == 0;
    }

    public MessageEvent[] getAsyncMessageEventQueue() {// TODO:现在暂时没有把message对应的 ChannelHandlerContext ctx存下来，以后有需求时，可以加上
        return asyncMessageEventQueue;
    }

    public void registerInnerAttach(Object _innerAttach) {
        this.innerAttach = _innerAttach;
    }

    @SuppressWarnings("unchecked")
    public <T> T getInnerAttach() {
        return (T) innerAttach;
    }

    public void registerThrowable(Throwable ex) {
        if (throwables == null) {
            throwables = new ArrayList<Throwable>(1);
        }
        throwables.add(ex);
    }

    /**
     * 返回是否在规定时间内处理完请求
     *
     * @param timeoutms 超时时长，单位ms
     */
    public boolean isTimeout(long timeoutms) {
        return System.currentTimeMillis() - decode > timeoutms;
    }

    /**
     * 如果到达attach的超时时间，需要做的事情，如果没有实现这个方法，连接会被系统自动断掉
     */
    public static interface TimeoutListener {

        /**
         * 请在方法实现中完成返回处理，请注意超时返回和系统业务处理返回同时发生的并发问题
         */
        public abstract void onTimeout();
    }

    TimeoutListener timeoutListener;

    /**
     * 注册一个超时请求的处理器
     */
    public void setOnTimeoutListener(TimeoutListener timeoutListener) {
        this.timeoutListener = timeoutListener;
    }

    /**
     * 返回是否有注册超时处理器
     */
    public boolean hasTimeoutListener() {
        return null != timeoutListener;
    }

    public void checkChannelOrThread() { // 业务线程自身check
        boolean shutdown = false;
        try {
            shutdown = !channelHandlerContext.getChannel().isOpen();
            if (shutdown) {
                throw ClosedChannelError.INSTANCE;
                // } else if (shutdown = (processThread != null && processThread.isInterrupted()) || Thread.currentThread().isInterrupted()) {
            }
            if (processThread != null) {
                shutdown = processThread.isInterrupted(); // 2012-04-16 zengdong 这里不能用 Thread.interrupt因为需要判断的是 正在处理的线程
                if (shutdown) {
                    throw ProcessTimeoutError.INSTANCE;
                }
            }
        } finally {
            if (shutdown) {
                // 连接已经关了,或者已经被中断了
                closeCloseable();
            }
        }
    }

    private synchronized void closeCloseable() {
        if (closeable != null) {
            for (Object obj : closeable) {
                if (obj instanceof Thread) {
                    ((Thread) obj).interrupt();
                } else {
                    CloseableHelper.closeSilently(obj);
                }
            }
        }
        closeable = null;
    }

    /**
     * 在通道关闭时通知此attach进行中断及关闭
     *
     * @return true表示是在 收包->发包中间被中断的(一般是由peer端)，false表示其他情况
     */
    // 这里要同步的原因是怕在判断间隙此thread被业务使用,造成interrupt一个正常的线程了
    public synchronized boolean interrupt(StringBuilder info) {// 外部sweeper来触发中断
        if (running && processThread != null) {
            processThread.interrupt();
            if (info != null) {
                info.append("[processThread]").append(processThread.getName());
            }
        }
        if (closeable != null && info != null) {
            info.append("[closeable]");
            for (Object obj : closeable) {
                info.append(obj).append(" ");
            }
        }
        closeCloseable();
        return running;
    }

    /**
     * 判断是否在业务处理
     */
    public boolean isProcessing() {
        // return processThread == null && EmptyChecker.isEmpty(closeable);
        // 2012-05-31 判断是否在业务处理，应该是从 收到req到发送了resp之间，也就是 running
        return running;
    }

    /**
     * 判断是否在业务处理
     */
    public boolean isNotProcessing() {
        // return processThread == null && EmptyChecker.isEmpty(closeable);
        // 2012-05-31 判断是否在业务处理，应该是从 收到req到发送了resp之间，也就是 running
        return !running;
    }

    public synchronized void _registerCloseable(Object obj) {
        if (closeable == null) {
            closeable = new HashSet<Object>(1);
        }
        closeable.add(obj);
    }

    public void registerCloseable(Object obj) {
        _registerCloseable(obj);
    }

    public void registerCloseable(Thread t) {
        Thread.interrupted();// 重置当前线程的中断标志位
        _registerCloseable(t);
    }

    public synchronized void unregisterCloseable(Object obj) {
        if (closeable != null) {
            closeable.remove(obj);
        }
    }

    public String getName() {
        StringBuilder sb = new StringBuilder();
        if (respOperationCompleteCount > 0) {
            sb.append("[").append(respOperationCompleteCount).append("]");
        }
        sb.append(cmdMeta);
        sb.append(request == null ? "" : "-" + Integer.toHexString(request.hashCode()));
        sb.append(channelHandlerContext.getChannel().getRemoteAddress());
        sb.append("/");
        getTimeSpanInfo(sb, System.currentTimeMillis(), false);
        return sb.toString();
    }

    public StringBuilder getTimeSpanInfo() {// 用于debug时打印出信息
        return getTimeSpanInfo(new StringBuilder(), System.currentTimeMillis(), true);
    }

    public StringBuilder getTimeInfo(StringBuilder tmp, String name, long time) {
        if (time > 0) {
            tmp.append(name).append(DateStringUtil.DEFAULT.format(new Date(time))).append("\t");
        }
        return tmp;
    }

    public StringBuilder getTimeSpanInfo(StringBuilder tmp, long now, boolean fmt) {
        long decode_end = decode > 0 ? decode : now;
        long process_end = process > 0 ? process : now;
        long encode_end = encode > 0 ? encode : now;
        long complete_end = complete > 0 ? complete : now;

        String stage = "N/A";
        if (complete > 0) {
            stage = "complete";
        } else if (encode > 0) {
            stage = "encode";
        } else if (process > 0) {
            stage = "process";
        } else if (decode > 0) {
            stage = "decode";
        }

        String timeSpanInfoFmt = fmt ? "%-8s %-7s(%s|%s,%s,%s)" : "%s:%s(%s|%s,%s,%s)";
        String all_str = fmt ? HumanReadableUtil.timeSpan(complete_end - decode_end) : complete_end - decode_end + "";
        String before_decode_str = decode_end - channelOpenTime + "";// 解码前浪费的时间
        String decode_str = process_end - decode_end + "";
        String process_str = encode_end - process_end + "";
        String encode_str = complete_end - encode_end + "";

        tmp.append(String.format(timeSpanInfoFmt, stage, all_str, before_decode_str, decode_str, process_str, encode_str));
        return tmp;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void markWriteBegin() {
        this.encode = this.lastWriteTime = System.currentTimeMillis();
    }

    /**
     * 标记写结束,并返回增量
     */
    public long markWriteEnd() {
        long ori = this.complete;
        this.complete = this.lastWriteTime = System.currentTimeMillis();
        return ori == 0 ? -1 : complete - ori;
    }

    public ContextAttachment(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
        this.channelOpenTime = this.lastReadTime = this.lastWriteTime = System.currentTimeMillis();// 初始化时间
    }

    public HttpRequest getRequest() {// TODO:此方法很多地方都要调用，看是否直接在这里面 checkChannelCLosed
        return request;
    }

    public HttpResponse getResponse() {
        return response;
    }

    public long getDecode() {
        return decode;
    }

    public long getProcess() {
        return process;
    }

    public long getEncode() {
        return encode;
    }

    public long getComplete() {
        return complete;
    }

    public CmdMappers.CmdMeta getCmdMeta() {
        return cmdMeta;
    }

    public void setCmdMeta(CmdMappers.CmdMeta cmdMeta) {
        this.cmdMeta = cmdMeta;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public List<Throwable> getThrowables() {
        return throwables;
    }

    /**
     * 注册新的resp
     */
    public void registerNewMessage(HttpResponse _response) {
        this.response = _response;
        this.process = _response.getCreateTime();
    }

    /**
     * 注册新的req
     */
    public synchronized boolean registerNewMessage(HttpRequest _request) {
        if (this.running) { // pipelining由于服务器不处理，不要清理attach了，不然会对现有的造成影响
            log.warn("userAgentTryPipelining:\n{}\n\nANOTHER:{}", getDetailInfo(), _request);
            this.userAgentTryPipelining = true;
        } else { // 如果不是pipelining方式，复用连接，就需要先对attach进行清理，清除上次请求的残余
            this.userAgentTryPipelining = false;
            this.asyncMessageEventQueueCounter = 0;
            this.asyncMessageEventQueue = null;
            this.response = null;

            this.running = true;
            this.request = _request;

            this.decode = _request.getCreateTime();
            this.process = 0;
            this.encode = 0;
            this.complete = 0;

            this.lastReadTime = this.decode;
            this.cmdMeta = null;

            this.closeable = null;
            this.throwables = null;
            this.innerAttach = null;

            this.processThread = null;
        }
        return userAgentTryPipelining;
    }

    public Thread getProcessThread() {
        return processThread;
    }

    public synchronized void registerProcessThread() {
        Thread.interrupted();
        this.processThread = Thread.currentThread();
    }

    public synchronized void unregisterProcessThread() {
        this.processThread = null;
    }

    public long getLastReadTime() {
        return lastReadTime;
    }

    public long getLastWriteTime() {
        return lastWriteTime;
    }

    @Override
    public void operationComplete(ChannelFuture future) throws Exception {
        respOperationCompleteCount++;
        running = false;
        if (cmdMeta != null) {
            cmdMeta.access(this);
        }
        if (closeAfterOperationComplete) {
            future.getChannel().close();
        }
        if (request != null) {
            request.clean();
        }
    }

    public long getChannelOpenTime() {
        return channelOpenTime;
    }

    public StringBuilder getDetailInfo() {
        return getDetailInfo(new StringBuilder(), System.currentTimeMillis());
    }

    public StringBuilder getDetailInfo(StringBuilder tmp, long now) {
        ChannelHandlerContext ctx = channelHandlerContext;
        if (ctx != null) {
            tmp.append(ctx.getChannel()).append(' ');
        }
        tmp.append('\n');

        getTimeInfo(tmp, "o:", channelOpenTime);
        getTimeInfo(tmp, "r:", lastReadTime);
        getTimeInfo(tmp, "w:", lastWriteTime);
        tmp.append("respCount:").append(respOperationCompleteCount);
        tmp.append("\n");

        // 1.各时间花费时长
        tmp.append("TIMESPAN :     ");
        getTimeSpanInfo(tmp, now, true).append(running ? " running" : " idle").append("\n");

        if (running) {
            if (getCmdMeta() != null) {
                long timeout = getCmdMeta().getTimeout();
                tmp.append("cmdTimeout:").append(timeout).append("s\t");
                if (timeout > 0) {
                    long span = System.currentTimeMillis() - getProcess();
                    boolean needInterrupt = span > timeout * 1000;
                    if (needInterrupt) {
                        tmp.append(" < ").append(HumanReadableUtil.timeSpan(span));
                        tmp.append("\tNEED INTERRUPT");
                    } else {
                        tmp.append(" > ").append(HumanReadableUtil.timeSpan(span));
                    }
                }
                tmp.append("\n");
            } else {
                tmp.append("No CmdMeta\n");
            }
        } else {
            // 判断其是否io空闲
            TimeoutInterrupter ti = TimeoutInterrupter.getInstance();
            long read = getLastReadTime();
            long write = getLastWriteTime();
            long all = Math.max(read, write);
            int allTimeout = ti.getAllIdleTimeSeconds();
            if (response == null) {// 现在版本限制得很严格,这里只要发现 response非空,readerIdleTimeSeconds和writerIdleTimeSeconds都不会起作用
                if (ti.getReaderIdleTimeSeconds() > 0) {
                    if (now - read > ti.getReaderIdleTimeSeconds() * 1000) {
                        tmp.append("SHOULD CLOSE: readIdle-" + ti.getReaderIdleTimeSeconds() + "\n");
                    }
                }
                if (ti.getWriterIdleTimeSeconds() > 0) {
                    if (now - write > ti.getWriterIdleTimeSeconds() * 1000) {
                        tmp.append("SHOULD CLOSE: writeIdle-" + ti.getWriterIdleTimeSeconds() + "\n");
                    }
                }
            } else {
                int keepAliveTimeout = response.getKeepAliveTimeout();
                allTimeout = Math.max(ti.getAllIdleTimeSeconds(), keepAliveTimeout); // 保险起见，取大者来清理过期的attach
            }
            if (allTimeout > 0) {// 如果有设置要allIdle timeout
                if (now - all > allTimeout * 1000) {
                    tmp.append("SHOULD CLOSE: allIdle-" + allTimeout + "\n");
                }
            }
        }

        // 2.异步信息
        if (EmptyChecker.isNotEmpty(asyncMessageEventQueue)) {
            tmp.append("ASYNCINFO:\n");
            for (MessageEvent me : asyncMessageEventQueue) {
                tmp.append("\t").append(me).append("\n");
            }
        }

        // 3.正在处理的线程
        if (processThread != null) {
            tmp.append("THREAD   :     ").append(processThread.getName()).append("\n");
        }
        // 4.中途注册的可关闭资源
        if (EmptyChecker.isNotEmpty(closeable)) {
            tmp.append("CLOSEABLE:    ");
            for (Object obj : closeable) {
                tmp.append(obj).append(" ");
            }
            tmp.append("\n");
        }

        // 5.中途捕获的异常
        if (EmptyChecker.isNotEmpty(throwables)) {
            tmp.append("THROWABLE:     ");
            for (Throwable obj : throwables) {
                tmp.append(obj).append(" ");
            }
            tmp.append("\n");
        }

        // 6.请求
        if (request != null) {
            // tmp.append("REQUEST  :     \n");
            tmp.append(request.getDetailInfo());
        }

        // 7.响应
        if (response != null && response.isContentSetted()) {
            tmp.append("RESPONSE :     ").append(response.getStatus()).append("[").append(HumanReadableUtil.byteSize(response.getContentLength())).append("]").append("\n");
            tmp.append(StringHelper.digestString(response.getContentString())).append("\n");
        }
        return tmp;
    }

    @Override
    public int compareTo(ContextAttachment o) {
        int r = (int) (channelOpenTime - (o == null ? 0 : o.channelOpenTime));
        if (r == 0) {
            int hash = o == null ? 0 : o.hashCode();
            r = hashCode() - hash;
        }
        return r;
    }

    public boolean setKeepAliveHeader() {
        int timeout = response.getKeepAliveTimeout();
        boolean close = true;
        try {

            if (userAgentTryPipelining) {
                close = true;// 如果试图pipeline,则只处理第一个请求包，然后关闭连接
            } else if (timeout == -1) {// B.由客户端来决定
                close = HttpHeaders.Values.CLOSE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION)) || request.getProtocolVersion().equals(HttpVersion.HTTP_1_0)
                        && !HttpHeaders.Values.KEEP_ALIVE.equalsIgnoreCase(request.getHeader(HttpHeaders.Names.CONNECTION));
            } else {// C.使用全局配置
                close = timeout <= 0;
            }

            // }
        } finally {
            response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(response.getContentLength()));

            if (close) {
                response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
            } else {
                if (timeout != -1) {
                    response.setHeader(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
                    response.setHeader(HTTP_HEADER_KEEP_ALIVE, "timeout=" + timeout);
                }
            }
        }
        closeAfterOperationComplete = close;
        return close;
    }

    /**
     * 关闭当前请求的keepAlive属性，用于某些特殊情况下不能继续保持连接的情况
     */
    public void disableKeepAliveOnce() {
        userAgentTryPipelining = true;
    }
}
