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

package cn.wantedonline.puppy.httpserver.common;

import cn.wantedonline.puppy.httpserver.component.AbstractPageDispatcher;
import cn.wantedonline.puppy.httpserver.component.AccessLogger;
import cn.wantedonline.puppy.httpserver.component.HttpRequestDecoder;
import cn.wantedonline.puppy.httpserver.component.HttpResponseEncoder;
import cn.wantedonline.puppy.httpserver.component.session.SessionManager;
import cn.wantedonline.puppy.httpserver.component.session.SessionManagerBase;
import cn.wantedonline.puppy.httpserver.component.session.StandardSessionManager;
import cn.wantedonline.puppy.httpserver.stat.CountStat;
import cn.wantedonline.puppy.httpserver.stat.StreamStat;
import cn.wantedonline.puppy.httpserver.stat.TimeSpanStat;
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.DefaultSessionIdGenerator;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.SessionIdGenerator;
import cn.wantedonline.puppy.util.concurrent.ConcurrentUtil;
import cn.wantedonline.puppy.util.concurrent.NamedThreadFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 *     Http服务器配置信息，可以在serverconfig.properties修改配置
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/16
 */
@Service
public final class HttpServerConfig {
    private Logger log = Log.getLogger(HttpServerConfig.class);

    public static final int PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();

    private static ContentType respInnerContentType = ContentType.json;

    public static final String SESSIONID_PARAMERTER = "sessionId";

    @Config(resetable = true)
    private int listen_port = 8080;
    @Config(resetable = true)
    private int work_thread_num = 20;
    @Config(resetable = true)
    private int maxInitialLineLength = 4096;
    @Config(resetable = true)
    private int maxHeaderSize = 8192;
    @Config(resetable = true)
    private int maxChunkSize = 8192;
    @Config(resetable = true)
    private int maxContentLength = 1024*1024;
    @Config(resetable = true)
    private int keepAliveTimeout = 2;
    @Config(resetable = true)
    private String cmdSuffix = "Cmd";
    @Config(resetable = true)
    private String cmdDefaultMethod = "process";
    @Config(resetable = true)
    private String respDefaultContentType = "json";
    @Config(resetable = true)
    private boolean openLogHandler;
    //===== add Session on V0.6.3  2017.01.11
    @Config(resetable = true)
    private boolean openSession = false;
    @Config(resetable = true)
    private String sessionStore = null;
    @Config(resetable = true)
    private int sessionMaxActiveTime = 1800;
    @Config(resetable = true)
    private int sessionMaxCount = 10000;
    @Config(resetable = true)
    private int sessionProcessExpiresFrequency = 5;
    @Config(resetable = true)
    private int sessionGCPeriod = 10;
    @Config(resetable = true)
    private int sessionGCInitialDelay = 10;

    public static SessionManagerBase sessionManager = null;

    public static SessionIdGenerator sessionIdGenerator = null;
    //===== end Session

    @Autowired
    private AbstractPageDispatcher dispatcher;

    private NioEventLoopGroup bossEventLoopGroup;
    private NioEventLoopGroup workerEventLoopGroup;
    private ChannelInitializer httpServerHandler;

    {
        bossEventLoopGroup = new NioEventLoopGroup(1, new NamedThreadFactory("Boss thread $", Thread.MAX_PRIORITY));
        int workThreads = work_thread_num > 0 ? work_thread_num : PROCESSOR_NUM*2;
        workerEventLoopGroup = new NioEventLoopGroup(workThreads, new NamedThreadFactory("Worker thread $",Thread.NORM_PRIORITY+4));
        httpServerHandler = new HttpServerHandler();
    }

    //*******************V0.2.0 统计需求 Start**************************//
    @Autowired
    public CountStat countStat;
    @Autowired
    public StreamStat streamStat;
    @Autowired
    public TimeSpanStat timeSpanStat;
    //*******************V0.2.0 统计需求 End ***************************//

    //*******************V0.4.0 日志告警需求 Start *********************//
    @Config(resetable = true)
    private boolean logaccessEnable = true;
    private AccessLogger accessLogger = new AccessLogger();
    //*******************V0.4.0 日志告警需求 End *********************//

    public static ContentType getRespInnerContentType() {
        return respInnerContentType;
    }

    public NioEventLoopGroup getBossEventLoopGroup() {
        return bossEventLoopGroup;
    }

    public NioEventLoopGroup getWorkerEventLoopGroup() {
        return workerEventLoopGroup;
    }

    public int getListenPort() {
        return listen_port;
    }

    public ChannelInitializer getHttpServerHandler() {
        return httpServerHandler;
    }

    public String getCmdSuffix() {
        return cmdSuffix;
    }

    public String getCmdDefaultMethod() {
        return cmdDefaultMethod;
    }

    public boolean isLogaccessEnable() {
        return logaccessEnable;
    }

    public void setLogaccessEnable(boolean logaccessEnable) {
        this.logaccessEnable = logaccessEnable;
        initAccessLogger();
    }

    @AfterConfig
    private void initAccessLogger() {
        accessLogger.setLogEanble(logaccessEnable);
    }

    public AccessLogger getAccessLogger() {
        return accessLogger;
    }

    private class HttpServerHandler extends ChannelInitializer<SocketChannel> {
        @Override
        protected void initChannel(SocketChannel ch) throws Exception {
            ChannelPipeline cp = ch.pipeline();
            if (openLogHandler) {
                cp.addLast("inner_logger_handler",new LoggingHandler(LogLevel.DEBUG));
            }
            cp.addLast("puppy_http_request_decoder",new HttpRequestDecoder(maxInitialLineLength, maxHeaderSize, maxChunkSize))
              .addLast("puppy_http_response_encoder", new HttpResponseEncoder())
//              .addLast("aggregator",new HttpObjectAggregator(maxContentLength))
              .addLast("pageDispatcher", dispatcher);
        }
    }

    public void stopEventLoopGroup() {
        workerEventLoopGroup.shutdownGracefully();
        bossEventLoopGroup.shutdownGracefully();
    }

    public boolean getOpenSession() {
        return openSession;
    }

    public int getWorkerCount() {
        return workerEventLoopGroup.executorCount();
    }

    @AfterConfig
    public void initRespInnerContentType() {
        if ("xml".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.xml;
        } else if ("html".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.html;
        } else if ("plain".equalsIgnoreCase(respDefaultContentType)) {
            respInnerContentType = ContentType.plain;
        } else {
            respInnerContentType = ContentType.json;
        }
        log.info("set response inner contentType is: {}", respInnerContentType);
    }

    @AfterConfig
    public void initSessionManager() {
        if (openSession) {
            if ("jvm".equalsIgnoreCase(sessionStore)) {
                sessionIdGenerator = new DefaultSessionIdGenerator();
                sessionManager = StandardSessionManager.getInstance();
                sessionManager.setSessionIdGenerator(sessionIdGenerator);
                sessionManager.setMaxActive(sessionMaxCount);
                sessionManager.setSessionMaxAliveTime(sessionMaxActiveTime);
                sessionManager.setProcessExpiresFrequency(sessionProcessExpiresFrequency);
                sessionManager.startSessionGC(sessionGCInitialDelay, sessionGCPeriod);
            }
        }
    }



}
