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
import cn.wantedonline.puppy.httpserver.component.HttpRequestDecoder;
import cn.wantedonline.puppy.httpserver.component.HttpResponseEncoder;
import cn.wantedonline.puppy.httpserver.stat.CountStat;
import cn.wantedonline.puppy.httpserver.stat.StreamStat;
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.NamedThreadFactory;
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

    @Autowired
    private AbstractPageDispatcher dispatcher;

    private NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1, new NamedThreadFactory("Boss thread $", Thread.MAX_PRIORITY));
    private NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(work_thread_num > 0 ? work_thread_num : PROCESSOR_NUM*2, new NamedThreadFactory("Worker thread $",Thread.NORM_PRIORITY+4));
    private ChannelInitializer httpServerHandler = new HttpServerHandler();

    //*******************V0.2.0 统计需求 Start**************************//
    @Autowired
    public CountStat countStat;
    @Autowired
    public StreamStat streamStat;
    //*******************V0.2.0 统计需求 End ***************************//

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
}
