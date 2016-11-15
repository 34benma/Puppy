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
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.EmptyChecker;
import cn.wantedonline.puppy.util.HttpUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.ValueUtil;
import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.management.ManagementFactory;

/**
 * Created by wangcheng on 2016/10/27.
 */
@Component
public class Bootstrap {

    private static final Logger logger = Log.getLogger();
    private volatile boolean stopping = false;
    private static ApplicationContext context;
    private String startServerTime = "";
    private String serverStatus = "";

    /**
     * 获得本地服务器启动时间
     * @return
     */
    public String getStartServerTime() {
        String hostName = "UNKNOWN";
        int pid = -1;
        String ip = HttpUtil.getLocalSampleIP();
        String pidAtHostName = ManagementFactory.getRuntimeMXBean().getName();
        int idx = pidAtHostName.indexOf('@');
        if (idx > 0) {
            pid = ValueUtil.getInteger(pidAtHostName.substring(0, idx), pid);
            hostName = pidAtHostName.substring(idx+1);
        }
        return hostName + "----" + ip + "----" + serverStatus + "----" + startServerTime;
    }

    private static boolean isArgWhat(String[] args, String... what) {
        if (EmptyChecker.isEmpty(args)) {
            return false;
        }
        String arg = args[0].toLowerCase();
        for (String w : what) {
            if (arg.indexOf(w) > 0) {
                return true;
            }
        }
        return false;
    }

    public static ApplicationContext main(String[] args, Runnable initRunnable, Runnable shutdownRunnable, String... springConfigLocations) throws IOException {
        long before = System.currentTimeMillis();
        try {
            context = SpringBootstrap.load(springConfigLocations);
        } catch (Throwable t) {
            t.printStackTrace();
            System.exit(1);
        }
        System.out.println("---->loadContext [" + (System.currentTimeMillis() - before) + "] MS");
        logger.info("---->loadContext [" + (System.currentTimeMillis() - before) + "] MS");
        Bootstrap bootstrap = BeanUtil.getTypedBean("bootstrap");
        bootstrap.shutdownRunnable = shutdownRunnable;
        if (isArgWhat(args, "stop", "shutdown")) {
            bootstrap.sendShutdownCmd();
            System.exit(0);
        } else {
            if (!isArgWhat(args, "compelled", "force")) {
                bootstrap.bindRetryTimeout = 0;
            }
            bootstrap.start(initRunnable);
        }
        return context;
    }

    public static ApplicationContext main(String[] args, Runnable initRunnable, String... springConfigLocations) throws IOException {
        return main(args, initRunnable, null, springConfigLocations);
    }

    public static ApplicationContext main(String[] args, String... springConfigLocations) throws IOException {
        return main(args, null, null, springConfigLocations);
    }

    private Runnable shutdownRunnable = null;
    @Config
    private long bindRetryTimeout = 6000;

    public long sendShutdownCmd() {
        return 0;
    }

    public void start(Runnable initialRunnable) throws  IOException {

    }
}
