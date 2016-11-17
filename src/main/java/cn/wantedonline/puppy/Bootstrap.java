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

import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.spring.SpringBootstrap;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.HttpServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;

/**
 * Puppy启动类
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/10/27.
 */
@Component
public class Bootstrap {
    private static ApplicationContext context;

    @Autowired
    private HttpServerConfig httpServerConfig;

    private Runnable shutdownRunnable;

    private ChannelFuture serverChannelFuture;

    private String serverStartTime = "";

    private volatile boolean stopping = false;

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

    private void startHttpServer(Runnable initRunnable) {
        initEnv();
        initOutter(initRunnable);
        start();
    }

    private void initEnv() {
        long begin = System.currentTimeMillis();
        //TODO:日志组件等环境初始化工作
        System.out.println("------------------------------> 系统组件准备完毕，耗时：" + (System.currentTimeMillis() - begin) + "MS");
    }

    private void start() {
        long begin = System.currentTimeMillis();
        ServerBootstrap b = initHttpServerBootstrap();
        int port = httpServerConfig.getListenPort(); //绑定端口前要先检查，端口有可能被占用
        //TODO:要检查端口是否被占用
        try {
            serverChannelFuture = b.bind(new InetSocketAddress(port)).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            System.exit(1); //netty绑定失败，直接退出
        }
        System.out.println("------------------------------> Netty 端口绑定成功[port:" + port + "]耗时： " + (System.currentTimeMillis() - begin) + "MS");
    }

    private void initOutter(Runnable runnable) {
        if (AssertUtil.isNotNull(runnable)) {
            long begin = System.currentTimeMillis();
            try {
                runnable.run(); //事实上，这个线程和主线程是同一个线程
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(1); //初始化线程启动异常，直接退出
            }
            System.out.println("------------------------------> initRunable执行完毕，耗时: " + (System.currentTimeMillis() - begin) + "MS");
        }
    }

    /**
     * Puppy启动入口
     * @param args 启动参数
     * @param initRunnable 在Puppy完全启动前希望执行的任务
     * @param shutdownRunnable 在Puppy关闭时希望执行的任务
     * @param springConfigLocations
     * @return
     */
    public static ApplicationContext main(String[] args, Runnable initRunnable, Runnable shutdownRunnable, String... springConfigLocations) {
        long begin = System.currentTimeMillis();
        try {
            context = SpringBootstrap.load(springConfigLocations);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            System.exit(1); //Spring起不来就无法玩下去了
        }

        System.out.println("------------------------------> Spring启动完毕,耗时: " + (System.currentTimeMillis() - begin) + "MS");

        Bootstrap bootstrap = BeanUtil.getTypedBean("bootstrap");
        bootstrap.shutdownRunnable = shutdownRunnable;
        bootstrap.startHttpServer(initRunnable);
        // http://www.kammerl.de/ascii/AsciiSignature.php
        // choose font: doom
        String ascii_doom = "______                            \n" +
                "| ___ \\                           \n" +
                "| |_/ /_   _  _ __   _ __   _   _ \n" +
                "|  __/| | | || '_ \\ | '_ \\ | | | |\n" +
                "| |   | |_| || |_) || |_) || |_| |\n" +
                "\\_|    \\__,_|| .__/ | .__/  \\__, |\n" +
                "             | |    | |      __/ |\n" +
                "             |_|    |_|     |___/ ";

        bootstrap.serverStartTime = DateStringUtil.DEFAULT.now();
        System.out.println(ascii_doom + " ["+ bootstrap.serverStartTime +"]启动完成...");
        return context;
    }


    public void stopServer() {
        if (AssertUtil.isNull(serverChannelFuture) || !serverChannelFuture.isSuccess()) {
            System.out.println("------------------------------> 服务器未启动或启动失败，停止服务器无效...");
        }
        long begin = System.currentTimeMillis();
        stopping = true;
        if (AssertUtil.isNotNull(shutdownRunnable)) {
            shutdownRunnable.run();
            System.out.println("------------------------------> 服务器准备关闭，资源清理完毕，耗时: " + (System.currentTimeMillis() - begin) + "MS");
        }
        try {
            serverChannelFuture.channel().closeFuture().sync();
            httpServerConfig.stopEventLoopGroup();
        } catch (Throwable e) {
            e.printStackTrace();
        }
        String ascii_doom = "______              ______              \n" +
                "| ___ \\             | ___ \\             \n" +
                "| |_/ / _   _   ___ | |_/ / _   _   ___ \n" +
                "| ___ \\| | | | / _ \\| ___ \\| | | | / _ \\\n" +
                "| |_/ /| |_| ||  __/| |_/ /| |_| ||  __/\n" +
                "\\____/  \\__, | \\___|\\____/  \\__, | \\___|\n" +
                "         __/ |               __/ |      \n" +
                "        |___/               |___/       ";
        System.out.println(ascii_doom);
    }

    public boolean isStopping() {
        return stopping;
    }

    public String getServerStartTime() {
        return serverStartTime;
    }
}
