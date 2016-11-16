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

package cn.wantedonline.puppy;

import cn.wantedonline.puppy.spring.SpringBootstrap;
import cn.wantedonline.puppy.util.HttpServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Puppy启动类
 *
 * Created by wangcheng on 2016/10/27.
 */
@Component
public class Bootstrap {
    private static ApplicationContext context;

    @Autowired
    private HttpServerConfig httpServerConfig;

    /**
     * 初始化一个HttpServerBootstrap,还未绑定端口和启动
     */
    private ServerBootstrap initHttpServerBootstrap() {
        EventLoopGroup bossGroup = httpServerConfig.getBossEventLoopGroup();
        EventLoopGroup workerGroup = httpServerConfig.getWorkerEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                       .channel(NioServerSocketChannel.class)
                       .childHandler(httpServerConfig.getHttpServerHandler());
        return serverBootstrap;
    }

    public static ApplicationContext main(String... springConfigLocations) {
        context = SpringBootstrap.load(springConfigLocations);

        return context;
    }

}
