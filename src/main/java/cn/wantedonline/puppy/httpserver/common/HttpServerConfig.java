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
import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.NamedThreadFactory;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
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
    public static final int PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();
    private static ContentType respInnerContentType = ContentType.json;

    @Config(resetable = true)
    private int listen_port = 8080;
    @Config(resetable = true)
    private int work_thread_num = 0;
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

    @Autowired
    private AbstractPageDispatcher dispatcher;

    private NioEventLoopGroup bossEventLoopGroup = new NioEventLoopGroup(1, new NamedThreadFactory("PuppyServer:NIO boss thread $", Thread.MAX_PRIORITY));
    private NioEventLoopGroup workerEventLoopGroup = new NioEventLoopGroup(work_thread_num <= 0 ? PROCESSOR_NUM*2 : work_thread_num, new NamedThreadFactory("PuppyServer:NIO worker thread $",Thread.NORM_PRIORITY+4));
    private ChannelInitializer httpServerHandler = new HttpServerHandler();

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
    }
}
