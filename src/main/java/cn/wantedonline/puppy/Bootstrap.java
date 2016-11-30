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

import cn.wantedonline.puppy.httpserver.component.CmdPageDispatcher;
import cn.wantedonline.puppy.httpserver.stat.NioWorkerStat;
import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.spring.SpringBootstrap;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.*;
import cn.wantedonline.puppy.httpserver.common.HttpServerConfig;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.internal.logging.InternalLoggerFactory;
import io.netty.util.internal.logging.Slf4JLoggerFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.management.ManagementFactory;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Puppy启动类
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/10/27.
 */
@Component
public class Bootstrap {
    private Logger log = Log.getLogger(Bootstrap.class);

    private static ApplicationContext context;

    @Autowired
    private HttpServerConfig httpServerConfig;
    @Autowired
    private CmdPageDispatcher dispatcher;

    private Runnable shutdownRunnable;

    private ChannelFuture serverChannelFuture;

    private String serverStartTime = "";
    private String serverStatus = "";

    private volatile boolean stopping = false;

    @Config
    private long bindRetryTimeout = 60000; //端口绑定超时时间，默认60s

    public static boolean isArgWhat(String[] args, String... what) {
        if (AssertUtil.isEmptyArray(args)) {
            return false;
        }
        String arg = args[0].toLowerCase();
        for (String w : what) {
            if (arg.indexOf(w) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化一个HttpServerBootstrap,还未绑定端口和启动
     */
    private ServerBootstrap initHttpServerBootstrap() {
        EventLoopGroup bossGroup = httpServerConfig.getBossEventLoopGroup();
        NioEventLoopGroup workerGroup = httpServerConfig.getWorkerEventLoopGroup();
        NioWorkerStat.registerWorkers(workerGroup);
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                       .channel(NioServerSocketChannel.class)
                       .childHandler(httpServerConfig.getHttpServerHandler());
        return serverBootstrap;
    }

    private void startHttpServer(Runnable initRunnable) {
        initEnv();
        dispatcher.init();
        initOutter(initRunnable);
        start();
    }

    private void initEnv() {
        long begin = System.currentTimeMillis();
        //替换掉Netty默认日志组件
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        System.out.println("------------------------------> 系统组件准备完毕，耗时：" + (System.currentTimeMillis() - begin) + "MS");
    }

    private void start() {
        ServerBootstrap b = initHttpServerBootstrap();
        int port = httpServerConfig.getListenPort(); //绑定端口前要先检查，端口有可能被占用
        Exception ex = null;
        try {
            final AtomicLong retryBind = new AtomicLong(0);
            final AtomicBoolean sended = new AtomicBoolean(false);
            boolean binded = false;
            long beginBind = System.currentTimeMillis();
            long lastPrintTime = 0;
            while (!binded) {
                try {
                    long thisPrintTime = System.currentTimeMillis();
                    if (thisPrintTime - lastPrintTime > 1000) {
                        System.out.println("Ready bind Http port [" + port + "]");
                        lastPrintTime = thisPrintTime;
                    }
                    serverChannelFuture = b.bind(new InetSocketAddress(port)).sync();
                    long endBind = System.currentTimeMillis();
                    long rb = retryBind.get();
                    if (rb > 0) {
                        System.out.println("------------------------------> 重试绑定成功，最后一次重试耗时："+ (endBind-rb) + "MS");
                    }
                    System.out.println("------------------------------> 端口绑定成功[port:" + port + "]耗时： " + (endBind - beginBind) + "MS");
                    binded = true;
                } catch (final Exception e) {
                    if (System.currentTimeMillis() - beginBind > bindRetryTimeout) {
                        throw e;
                    }
                    if (!sended.get()) {
                        ConcurrentUtil.getDaemonExecutor().execute(new Runnable() {

                            @Override
                            public void run() {
                                System.out.println("------------------------------> sendShutdownCmd  [" + httpServerConfig.getListenPort() + "]");
                                log.error("{}", e.toString());
                                long oriSvrBeginExitTime = sendShutdownCmd();
                                boolean result = oriSvrBeginExitTime > 0;
                                if (result) {
                                    if (retryBind.get() == 0) {
                                        retryBind.set(oriSvrBeginExitTime);
                                    }
                                }
                                sended.set(result);
                            }
                        });
                        sended.set(true);
                    }
                    ConcurrentUtil.threadSleep(10);
                    }
                }
            } catch (Exception e) {
                ex = e;
        }

        long span = System.currentTimeMillis() - ManagementFactory.getRuntimeMXBean().getStartTime();
        String msg = ex == null ? "OK" : "ERROR";
        String chnmsg = ex == null ? "服务器启动完毕.(port[" + port + "])" : "服务器启动失败.(port[" + port + "])";
        String spanStr = "[" + span + "MS]";
        String errStr = ex == null ? "" : ex.getMessage();
        log.error(
                "HTTPServer(port[{}],workerCount[{}]) Start {}.{}", new Object[]{
                        port,
                        httpServerConfig.getWorkerCount(),
                        msg,
                        spanStr,
                        ex
                });

        serverStatus = chnmsg;
        serverStartTime = DateStringUtil.DEFAULT.now();
        if (ex != null) {
            System.exit(1);
        }


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

    public static ApplicationContext main(String[] args, String... springConfigLocations) {
        return main(args, null, null, springConfigLocations);
    }

    public static ApplicationContext main(String[] args, Runnable initRunnable, String... springConfigLocations) {
        return main(args, initRunnable, null, springConfigLocations);
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

        if (isArgWhat(args, "stop", "shutdown")) {
            bootstrap.sendShutdownCmd();
            System.exit(0);
        } else {
            if (!isArgWhat(args, "compelled", "force")) {
                bootstrap.bindRetryTimeout = 0;
            }
            bootstrap.startHttpServer(initRunnable);
        }

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

        System.out.println(ascii_doom + " ["+ bootstrap.serverStartTime +"]启动完成...");
        return context;
    }

    public long sendShutdownCmd() {
        HttpURLConnection urlConnection = null;
        LineNumberReader lineReader = null;
        try {
            URL url = new URL("http://localhost:" + httpServerConfig.getListenPort() + "/shutdown");
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            lineReader = new LineNumberReader(new InputStreamReader(urlConnection.getInputStream()));
            StringBuilder sb = new StringBuilder();
            while (true) {
                String tmp = lineReader.readLine();
                if (tmp == null) {
                    break;
                }
                sb.append(tmp);
            }
            String returnStr = sb.toString();
            log.error("shutdown last result:{}", returnStr);
            return Long.valueOf(returnStr);
        } catch (Exception e) {
            log.error("", e);
        } finally {
            if (AssertUtil.isNotNull(urlConnection)) {
                urlConnection.disconnect();
                urlConnection = null;
            }
            CloseableHelper.closeSilently(lineReader);
        }
        return 0;
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
            serverChannelFuture.channel().closeFuture();
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
