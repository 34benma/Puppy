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

import cn.wantedonline.puppy.httpserver.util.HttpServerConfig;
import cn.wantedonline.puppy.httpserver.util.SystemInfo;
import cn.wantedonline.puppy.httpserver.util.SystemMonitor;
import cn.wantedonline.puppy.httpserver.util.concurrent.ConcurrentUtil;
import cn.wantedonline.puppy.spring.annotation.AfterBootstrap;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static jdk.nashorn.internal.runtime.regexp.joni.Config.log;

/**
 * Created by louiswang on 16/11/2.
 */
@Service
public class SystemChecker {
    private static final Logger logger = Log.getLogger(SystemMonitor.class);
    private static SystemChecker INSTNACE;

    private SystemChecker() {
        INSTNACE = this;
    }

    public static SystemChecker getInstance() {
        return INSTNACE;
    }

    @Autowired
    protected HttpServerConfig config;
    private static volatile boolean denialOfService = false;
    /** 快要接近denialOfService了,进一步退化,参数是: threadPoolQueueCountLimit - 500 */
    private static volatile boolean denialOfServiceAlmost = false;
    /** 系统最重要的线程池的监控，要很实时 */
    @Config
    private int dosMonitorCheckSec = 1;
    /** 垃圾回收监控，建议5分钟监控一次（因为是总回收时间的对比） */
    @Config
    private int gcMonitorSec = 5 * 60;
    private static volatile boolean lastTestDenialOfService = false;
    @Config(resetable = true)
    private double loadMonitorErrorThreshold = ConcurrentUtil.CORE_PROCESSOR_NUM;
    /** 系统负载监控，建议半分钟监控一次（因为是平均负载） */
    @Config
    private int loadMonitorSec = 1 * 30;
    /** 数值的界定，请参考http://blog.csdn.net/marising/article/details/5182771 */
    @Config(resetable = true)
    private double loadMonitorWarnThreshold = ConcurrentUtil.CORE_PROCESSOR_NUM * 0.7;
    /** 线程cpu时间监控,建议每半分钟监控一次 */
    @Config
    private int threadCpuTimeMonitorSec = 1 * 30;
    @Config(resetable = true)
    private int threadMonitorQueueThreshold = 1000;
    /** 线程池监控，建议每5秒钟监控一次（因为每时每刻的queue的数量都可能在大幅度变化） */
    @Config
    private int threadMonitorSec = 5;
    /** log线程池监控，要严防队列过大导致IO过高和内存溢出 */
    @Config
    private int logMonitorCheckSec = 10;
    @Config(resetable = true)
    private int logMonitorLimit = 20000;
    private static volatile boolean logEnabled = true;
    private static volatile boolean logAlreadyExceeded = false;
    /** 为了防止启动时候突然飙高而报，启动时候第一次不报，可以防止服务器启动时突然的堆积日志把服务器搞崩溃 */
    private static volatile boolean logExceededFirstTime = true;

    /** 线程池允许等待的线程数 */
    @Config(resetable = true)
    private int threadPoolQueueCountLimit = 30000;

    @AfterBootstrap
    protected void init() {
        SystemMonitor.initGarbageCollectMonitor(gcMonitorSec);
        SystemMonitor.initLoadAverageMonitor(loadMonitorSec);
        SystemMonitor.initThreadCpuTimeMonitor(threadCpuTimeMonitorSec);
        SystemMonitor.initThreadPoolMonitor(threadMonitorSec, threadMonitorQueueThreshold);
        initDenialOfServiceMonitor();
        initLogMonitor();
    }

    public void initDenialOfServiceMonitor() {
        if (dosMonitorCheckSec > 0) {
            logger.info("DenialOfServiceMonitor ON, interval:{}sec, pipelineSize:{}", dosMonitorCheckSec, config.getPipelineExecutorOrdered().getCorePoolSize());
            ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                /**
                 * check denialOfServiceAlmost
                 */
                private boolean check2(ThreadPoolExecutor executor) {
                    int _threadPoolQueueCountLimit = threadPoolQueueCountLimit - 500;// 几乎要拒绝服务的等待队列阀值

                    int activeCount = executor.getActiveCount();
                    int queueCount = executor.getQueue().size(); // 等待队列的大小
                    int largestPoolSize = executor.getLargestPoolSize();
                    int atLeastReamin = largestPoolSize <= HttpServerConfig.CORE_PROCESSOR_NUM ? 1 : HttpServerConfig.CORE_PROCESSOR_NUM; // 最少要剩余的空闲线程数
                    if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize) {
                        ConcurrentUtil.threadSleep(2000); // 小小暂停一下，防止是那种突然一下子冲过来的情况而报DOS
                        activeCount = executor.getActiveCount();
                        int currQueueCount = executor.getQueue().size(); // 等待队列的大小，如果持续增大，就说明肯定有问题，需要DOS
                        if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize && currQueueCount > queueCount && currQueueCount > _threadPoolQueueCountLimit) {
                            denialOfServiceAlmost = true;
                            return true;
                        }
                    }
                    return false;
                }

                /**
                 * check denialOfService
                 */
                private boolean check(ThreadPoolExecutor executor) {
                    int activeCount = executor.getActiveCount();
                    int queueCount = executor.getQueue().size(); // 等待队列的大小
                    int largestPoolSize = executor.getLargestPoolSize();
                    int atLeastReamin = largestPoolSize <= HttpServerConfig.CORE_PROCESSOR_NUM ? 1 : HttpServerConfig.CORE_PROCESSOR_NUM; // 最少要剩余的空闲线程数
                    if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize) {
                        String pipelineInfo = SystemInfo.getThreadsDetailInfo("PIPELINE", true, 20); // 先打印好
                        ConcurrentUtil.threadSleep(2000); // 小小暂停一下，防止是那种突然一下子冲过来的情况而报DOS
                        activeCount = executor.getActiveCount();
                        int currQueueCount = executor.getQueue().size(); // 等待队列的大小，如果持续增大，就说明肯定有问题，需要DOS
                        if (activeCount > 0 && activeCount + atLeastReamin >= largestPoolSize && currQueueCount > queueCount && currQueueCount > threadPoolQueueCountLimit) { // 打印好了，线程池仍然大量占用，就发出报警邮件
                            if (!lastTestDenialOfService) {
                                MDC.put("mailTitle", "DenialOfService");
                                logger.error("DENIAL OF SERVICE, ACTIVE_SIZE:{}, WAITTING_QUEUE_SIZE:{}, RUNNING PIPELINE:\n{}\n\n", activeCount, currQueueCount, pipelineInfo);
                            }
                            denialOfService = true;
                            lastTestDenialOfService = true;
                            denialOfServiceAlmost = true;
                            return true;
                        }
                    }
                    return false;
                }

                @Override
                public void run() {
                    boolean r = check(config.getPipelineExecutorOrdered());
                    boolean r1 = check(config.getPipelineExecutorUnordered());
                    if (!r && !r1) {
                        denialOfService = false;
                        lastTestDenialOfService = false;
                    }

                    boolean r2 = check2(config.getPipelineExecutorOrdered());
                    boolean r21 = check2(config.getPipelineExecutorUnordered());
                    if (!r2 && !r21) {
                        denialOfServiceAlmost = false;
                    }
                }
            }, dosMonitorCheckSec * 5, dosMonitorCheckSec, TimeUnit.SECONDS);// httpServer刚启动时，很有可能很多请求冲进来，先不拒绝服务，所以暂定5s后再开始定时
        }
    }

    public void initLogMonitor() {
        if (logMonitorCheckSec > 0) {
            final ThreadPoolExecutor logExecutor = (ThreadPoolExecutor) ConcurrentUtil.getLogExecutor();
            logger.info("LogMonitor ON, interval:{}sec, logExecutorSize:{}", logMonitorCheckSec, logExecutor.getQueue().size());
            ConcurrentUtil.getWatchdog().scheduleWithFixedDelay(new Runnable() {

                @Override
                public void run() {
                    boolean r = isLogExceeded(logExecutor);
                    if (!r) {
                        logEnabled = true;
                        logAlreadyExceeded = false;
                    }
                }

                /**
                 * 检查日志线程池队列任务数
                 *
                 * @return 如果超过限值返回true，没超过返回false
                 */
                private boolean isLogExceeded(ThreadPoolExecutor executor) {
                    int queueCount = executor.getQueue().size(); // 如果队列的log超过限制，就临时把日志功能关闭
                    if (queueCount > logMonitorLimit) {
                        if (!logAlreadyExceeded) { // 保证超过限定值之后只报出一次，如果回退到限定值内，再超过才再报
                            if (logExceededFirstTime) {
                                logExceededFirstTime = false;
                            } else {
                                MDC.put("mailTitle", "TOO MANY LOGS");
                                logger.error("TOO MANY LOGS,WAITTING_QUEUE_SIZE:{}\n", queueCount);
                            }
                        }
                        logEnabled = false;
                        logAlreadyExceeded = true;
                        return true;
                    }
                    return false;
                }

            }, logMonitorCheckSec, logMonitorCheckSec, TimeUnit.SECONDS);
        }
    }

    /**
     * 是否达到拒绝服务的阀值
     */
    public static boolean isDenialOfService() {
        return denialOfService;
    }

    /**
     * 快要接近denialOfService了,进一步退化,参数是: threadPoolQueueCountLimit - 500
     */
    public static boolean isDenialOfServiceAlmost() {
        return denialOfServiceAlmost;
    }

    /**
     * 如果日志功能打开了，才记录日志
     */
    public static boolean isLogEnabled() {
        return logEnabled;
    }

    public double getLoadMonitorErrorThreshold() {
        return loadMonitorErrorThreshold;
    }

    public double getLoadMonitorWarnThreshold() {
        return loadMonitorWarnThreshold;
    }

}
