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

package cn.wantedonline.puppy.httpserver.util;

import cn.wantedonline.puppy.httpserver.component.AccessLogger;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.util.concurrent.ConcurrentUtil;
import cn.wantedonline.puppy.httpserver.util.concurrent.NamedThreadFactory;
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.ExecutorUtil;
import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * Created by wangcheng on 2016/10/30.
 */
@Service
public final class HttpServerConfig {
    public static final Logger ALARMLOG = Log.getLogger("alarm.com.xunlei.netty");
    public static final int CORE_PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();
    private static HttpResponse.ContentType respInnerContentType = HttpResponse.ContentType.json;
    private AccessLogger accessLog = new AccessLogger();

    @Config(resetable = true)
    private int listen_port = 80;
    @Config
    private int https_listen_port = 0;
    @Config
    private int connectTimeoutMillis = 5000;
    @Config
    private int receiveBufferSize = 8192;
    @Config
    private int sendBufferSize = 8192;
    @Config
    private String indexCmdName = "echo";
    @Config
    private String cmdSuffix = "Cmd";
    @Config
    private String cmdDefaultMethod = "process";
    @Config
    public int workerCount = 0;// 0表示默认配置,-1表示则按cpu的个数的1倍来表示,n正数则表示就n个worker
    // 业务处理使用的线程池,带次序及内存监控功能
    // 最多2000线程,60s线程未激活则回收(测试发现新来的任务,不会用原来的线程？！)
    // 该线程池 假定 执行的是 ChannelEventRunnable的话,则最大限制其内存为100M
    // 总共内存限制是 1G
    // @Config
    // private int plMaximumPoolSize = CORE_PROCESSOR_NUM * 50;
    @Config
    private long plMaxChannelMemorySize = 100 * 1024 * 1024;// Caused by: java.lang.IllegalStateException: can't be changed after a task is executed
    @Config
    private long plMaxTotalMemorySize = 1024 * 1024 * 1024;// Caused by: java.lang.IllegalStateException: can't be changed after a task is executed
    @Config(resetable = true)
    private int plCorePoolSize = CORE_PROCESSOR_NUM * 50;
    @Config(resetable = true)
    private long plKeepAliveSecond = 60L;
    @Config(resetable = true)
    private String plAddBefore = "pageDispatcher";// pipelineExecutor线程池放在哪一个 channelHandler前,为null时,表示不加线程池,为空串时,表示放到最前面
    @Config(resetable = true)
    private boolean plAddDefalter = false;// 是否增加压缩逻辑
    @Config(resetable = true)
    private static long plAddDefalterContentLen = 1024; // 响应包 超过多少才进行压缩
    /** 异步处理时，默认创建的连接池大小 */
    @Config
    private static int asyncPoolSize = 1;

    public static long getPlAddDefalterContentLen() {
        return plAddDefalterContentLen;
    }

    /**
     * 异步处理时，默认创建的连接池大小
     */
    public static int getAsyncPoolSize() {
        return asyncPoolSize;
    }

    @Config(resetable = true)
    private String respDefaultContentType = "json";
    @Config(resetable = true)
    private int slowThreshold = 1000;// 统计慢处理的阈值,默认为 1000ms
    @Config(resetable = true)
    private static int keepAliveTimeout = 2;
    @Config(resetable = true)
    private int toleranceTimeout = 10;// 服务器默认认为超过10s钟的业务/编码/解码时间 是 有问题的,可以直接报错或提前关闭连接
    @Config(resetable = true)
    private boolean debugEnable = true;
    @Config(resetable = true)
    private boolean statEnable = true;
    @Config(resetable = true)
    private boolean logaccessEnable = true;
    @Autowired
    private Statistics default_statistics;
    @Autowired
    private Statistics statistics;
    @Resource(name = "httpServerPipelineFactory")
    private HttpServerPipelineFactory httpServerPipelineFactory;
    @Resource(name = "httpsServerPipelineFactory")
    private HttpsServerPipelineFactory httpsServerPipelineFactory;
    @Config(resetable = true)
    private static int asyncProxyPoolChannelCoreNum = 800;
    @Config(resetable = true)
    private static int asyncProxyPoolChannelSwepperDelaySeconds = 60;

    // 优先级由高到低: boss - worker - pipeline - biz
    /**
     * <pre>
     * 后台调度线程池
     * 注意:
     * 这里的线程都是后台线程,如果没有主线程,程序会立即退出
     *
     * 请改成调用ConcurrentUtil.getDaemonExecutor();
     */
    @Deprecated
    public static ScheduledExecutorService daemonTaskExecutor = ConcurrentUtil.getDaemonExecutor();
    public static final ExecutorService bossExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("New I/O server boss $", Thread.MAX_PRIORITY));
    public static final ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedThreadFactory("New I/O server worker $", Thread.NORM_PRIORITY + 4));

    /**
     * 有关OrderedMemoryAwareThreadPoolExecutor及MemoryAwareThreadPoolExecutor
     *
     * <pre>
     * http://www.blogjava.net/hankchen/archive/2012/04/08/373572.html
     *
     * 对于ExecutionHandler需要的线程池模型，Netty提供了两种可选：
     *
     * 1） MemoryAwareThreadPoolExecutor 通过对线程池内存的使用控制，可控制Executor中待处理任务的上限（超过上限时，后续进来的任务将被阻塞），并可控制单个Channel待处理任务的上限，防止内存溢出错误；
     *
     * 2） OrderedMemoryAwareThreadPoolExecutor 是 MemoryAwareThreadPoolExecutor 的子类。除了MemoryAwareThreadPoolExecutor 的功能之外，它还可以保证同一Channel中处理的事件流的顺序性，这主要是控制事件在异步处理模式下可能出现的错误的事件顺序，但它并不保证同一Channel中的事件都在一个线程中执行（通常也没必要）。
     *
     * 例如：
     *
     * Thread X: --- Channel A (Event A1) --.   .-- Channel B (Event B2) --- Channel B (Event B3) --->
     *                                       \ /
     *                                        X
     *                                       / \
     * Thread Y: --- Channel B (Event B1) --'   '-- Channel A (Event A2) --- Channel A (Event A3) --->
     * 上图表达的意思有几个：
     *
     * （1）对整个线程池而言，处理同一个Channel的事件，必须是按照顺序来处理的。例如，必须先处理完Channel A (Event A1) ，再处理Channel A (Event A2)、Channel A (Event A3)
     *
     * （2）同一个Channel的多个事件，会分布到线程池的多个线程中去处理。
     *
     * （3）不同Channel的事件可以同时处理（分担到多个线程），互不影响。
     *
     * OrderedMemoryAwareThreadPoolExecutor 的这种事件处理有序性是有意义的，因为通常情况下，请求发送端希望服务器能够按照顺序处理自己的请求，特别是需要多次握手的应用层协议。例如：XMPP协议。
     *
     * 现在回到具体业务上来，我们这里的认证服务也使用了OrderedMemoryAwareThreadPoolExecutor。
     * 认证服务的其中一个环节是使用长连接，不断处理来自另外一个服务器的认证请求。
     * 通信的数据包都很小，一般都是200个字节以内。一般情况下，处理这个过程很快，所以没有什么问题。
     * 但是，由于认证服务需要调用第三方的接口，如果第三方接口出现延迟，将导致这个过程变慢。
     * 一旦一个事件处理不完，由于要保持事件处理的有序性，其他事件就全部堵塞了！
     * 而短连接之所以没有问题，是因为短连接一个Channel就一个请求数据包，处理完Channel就关闭了，根本不存在顺序的问题，
     * 所以在业务层可以迅速收到请求，只是由于同样的原因（第三方接口），处理时间会比较长。
     * 其实，认证过程都是独立的请求数据包（单个帐号），每个请求数据包之间是没有任何关系的，保持这样的顺序没有意义！
     */
    private MemoryAwareThreadPoolExecutor pipelineExecutorUnordered;
    private MemoryAwareThreadPoolExecutor pipelineExecutorOrdered;

    @AfterConfig
    public void initPipelineExecutorOrdered() {
        if (pipelineExecutorOrdered == null) {
            pipelineExecutorOrdered = new OrderedMemoryAwareThreadPoolExecutor(plCorePoolSize, plMaxChannelMemorySize, plMaxTotalMemorySize, plKeepAliveSecond, TimeUnit.SECONDS,
                    new NamedThreadFactory("PIPELINE_ORD#", Thread.NORM_PRIORITY + 2));
        } else {
            pipelineExecutorOrdered.setCorePoolSize(plCorePoolSize);
            pipelineExecutorOrdered.setMaximumPoolSize(plCorePoolSize);
            pipelineExecutorOrdered.setKeepAliveTime(plKeepAliveSecond, TimeUnit.SECONDS);
        }
    }

    // MemoryAwareThreadPoolExecutor内部只使用CorePoolSize,而且core线程是可以回收的(allowCoreThreadTimeOut java1.5实现),然后里面用了一个无限容量的 LinkedTransferQueue<Runnable>()
    // 所以里面Plicy：new NewThreadRunsPolicy()按理是永远不会进的,所以这时候就要有内存计算来判断应该怎么处理
    @AfterConfig
    public void initPipelineExecutorUnordered() {
        if (pipelineExecutorUnordered == null) {
            pipelineExecutorUnordered = new MemoryAwareThreadPoolExecutor(plCorePoolSize, plMaxChannelMemorySize, plMaxTotalMemorySize, plKeepAliveSecond, TimeUnit.SECONDS, new NamedThreadFactory(
                    "PIPELINE#", Thread.NORM_PRIORITY + 2));
            // pipelineExecutor.allowCoreThreadTimeOut(false);
        } else {
            pipelineExecutorUnordered.setCorePoolSize(plCorePoolSize);
            pipelineExecutorUnordered.setMaximumPoolSize(plCorePoolSize);
            pipelineExecutorUnordered.setKeepAliveTime(plKeepAliveSecond, TimeUnit.SECONDS);
        }
    }

    @AfterConfig
    public void initRespInnerContentType() {
        if ("xml".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = HttpResponse.ContentType.xml;
        } else if ("html".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = HttpResponse.ContentType.html;
        } else if ("plain".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = HttpResponse.ContentType.plain;
        } else {
            respInnerContentType = HttpResponse.ContentType.json;
        }
    }

    @AfterConfig
    private void initStat() {
        statistics = statEnable ? default_statistics : NOPStatistics.INSTANCE;
    }

    public OrderedMemoryAwareThreadPoolExecutor getPipelineExecutor() {
        return (OrderedMemoryAwareThreadPoolExecutor) getPipelineExecutorOrdered();
    }

    public MemoryAwareThreadPoolExecutor getPipelineExecutorUnordered() {
        if (null == pipelineExecutorUnordered) {// 注意此处的调用需要考虑AfterConfig方法调用顺序
            initPipelineExecutorUnordered();
        }
        return pipelineExecutorUnordered;
    }

    public MemoryAwareThreadPoolExecutor getPipelineExecutorOrdered() {
        if (null == pipelineExecutorOrdered) {// 注意此处的调用需要考虑AfterConfig方法调用顺序
            initPipelineExecutorOrdered();
        }
        return pipelineExecutorOrdered;
    }

    // public static final ThreadPoolExecutor bizExecutor = new ThreadPoolExecutor(0, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new
    // SynchronousQueue<Runnable>(), new NamedThreadFactory("BIZ#"));

    // Executors.newCachedThreadPool(new NamedThreadFactory("   BizProcessor #"));
    // new ThreadPoolExecutor(10, 100, 65L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    public void setPlCorePoolSize(int plCorePoolSize) {
        this.plCorePoolSize = plCorePoolSize;
        initPipelineExecutorOrdered();
        initPipelineExecutorUnordered();
    }

    public void setPlKeepAliveSecond(long plKeepAliveSecond) {
        this.plKeepAliveSecond = plKeepAliveSecond;
        initPipelineExecutorOrdered();
        initPipelineExecutorUnordered();
    }

    public int getRealWorkerCount() {
        if (workerCount < 0) {
            return CORE_PROCESSOR_NUM * workerCount * -1;
        } else if (workerCount == 0) {
            return CORE_PROCESSOR_NUM * 2;// 默认配置,也就等同于 -2
        } else {
            return workerCount;
        }
    }

    public int getListen_port() {
        return listen_port;
    }

    public int getHttps_listen_port() {
        return https_listen_port;
    }

    public int getConnectTimeoutMillis() {
        return connectTimeoutMillis;
    }

    public int getReceiveBufferSize() {
        return receiveBufferSize;
    }

    public int getSendBufferSize() {
        return sendBufferSize;
    }

    public int getSlowThreshold() {
        return slowThreshold;
    }

    public static int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public int getToleranceTimeout() {
        return toleranceTimeout;
    }

    public boolean isDebugEnable() {
        return debugEnable;
    }

    public boolean isStatEnable() {
        return statEnable;
    }

    public String getIndexCmdName() {
        return indexCmdName;
    }

    public String getCmdSuffix() {
        return cmdSuffix;
    }

    public String getCmdDefaultMethod() {
        return cmdDefaultMethod;
    }

    public String getPlAddBefore() {
        return plAddBefore;
    }

    public boolean isPlAddDefalter() {
        return plAddDefalter;
    }

    public static HttpResponse.ContentType getRespInnerContentType() {
        return respInnerContentType;
    }

    public void setRespDefaultContentType(String respDefaultContentType) {
        this.respDefaultContentType = respDefaultContentType;
        initRespInnerContentType();
    }

    public void setStatEnable(boolean statEnable) {
        this.statEnable = statEnable;
        initStat();
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public void setLogaccessEnable(boolean logaccessEnable) {
        this.logaccessEnable = logaccessEnable;
        initAccessLogger();
    }

    @AfterConfig
    private void initAccessLogger() {
        accessLog.setLogEanble(logaccessEnable);
    }

    public AccessLogger getAccessLog() {
        return accessLog;
    }

    public void setPlAddBefore(String plAddBefore) {
        this.plAddBefore = plAddBefore;
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    public void setPlAddDefalter(boolean plAddDefalter) {
        this.plAddDefalter = plAddDefalter;
        httpServerPipelineFactory.rebuildPipeline();
        httpsServerPipelineFactory.rebuildPipeline();
    }

    /**
     * <pre>
     * 关闭httpServer内部netty的boss线程跟worker线程
     * 抄自： org.jboss.netty.channel.socket.nio.releaseExternalResources
     *
     * 注意： windows环境测试发现, 空跑情况下,调用ExecutorUtil.terminate方法,都会有cpu占用100%,并等待很久的情况,经常是花了50多秒
     *
     * 问题1：测试发现es.shutdownNow()方法好像是造成cpu100%的原因;
     *
     * 问题2：测试发现如果使用es.shutdown()方法(也就是 如果没有调用shutdownNow方法成功的情况下,es.awaitTermination(100, TimeUnit.MILLISECONDS)永远会返回false
     * 因此如果运行了shutdown()方法,内部for(;;)无法退出,死循环
     *
     * [详细关闭过程请查看ExecutorUtil.terminate源码]
     *
     * netty官方论坛也有类似问题的讨论?!：
     * http://www.jboss.org/netty/community.html#nabble-td5492010
     * http://www.jboss.org/netty/community.html#nabble-td5976446
     *
     * https://issues.jboss.org/browse/NETTY-366
     * https://issues.jboss.org/browse/NETTY-380
     */
    public static void releaseExternalResources() {
        ExecutorUtil.terminate(new Executor[] {
                bossExecutor,
                workerExecutor
        });
    }

    public static int getAsyncProxyPoolChannelCoreNum() {
        return asyncProxyPoolChannelCoreNum;
    }

    public static int getAsyncProxyPoolChannelSwepperDelaySeconds() {
        return asyncProxyPoolChannelSwepperDelaySeconds;
    }

}
