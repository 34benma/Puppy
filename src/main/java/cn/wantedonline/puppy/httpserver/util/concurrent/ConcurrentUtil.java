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

package cn.wantedonline.puppy.httpserver.util.concurrent;

import cn.wantedonline.puppy.httpserver.util.HumanReadableUtil;

import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy;
import java.util.concurrent.ThreadPoolExecutor.DiscardPolicy;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 业务上尽量都统一使用当前util类的线程池
 */
public class ConcurrentUtil {

    public static final CallerRunsPolicy callerRunsPolicy = new CallerRunsPolicy();
    public static final int CORE_PROCESSOR_NUM = Runtime.getRuntime().availableProcessors();
    private static ScheduledExecutorService daemonExecutor;
    private static ExecutorService defaultExecutor;
    public static final RejectedExecutionHandler discardPolicy = new DiscardPolicy();
    private static final String executorStatFmt = "%-23s %-13s %-12s %-16s %-12s %-12s %-18s %-16s %-16s %-16s %-12s\n";
    private static final String executorStatHeader = String.format(executorStatFmt, "Executor", "activeCount", "poolSize", "largestPoolSize", "queueSize", "taskCount", "completedTaskCount",
            "corePoolSize", "maximumPoolSize", "keepAliveTime", "coreTimeOut");
    private static ExecutorService logExecutor;
    private static final String PREFIX = "";// ConcurrentUtil.class.getSimpleName() +"-";

    private static final ScheduledExecutorService watchdog = new ScheduledThreadPoolExecutor(1, new NamedThreadFactory(PREFIX + "Watchdog(Sche)", Thread.NORM_PRIORITY, true));

    private static final Collection<Number> atomicCounterList = new HashSet<Number>();
    public static int atomicCounterScanHour = 2;
    public static int atomicCounterResetThreshold = Integer.MAX_VALUE / 2;
    static {
        // 为防止计数器溢出，在后台线程定时n小时重置计数器,默认是2小时就判断一次
        getDaemonExecutor().scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                int integerMax = Integer.MAX_VALUE - atomicCounterResetThreshold;
                long longMax = Long.MAX_VALUE - atomicCounterResetThreshold;
                for (Number n : atomicCounterList) {
                    if (n instanceof AtomicInteger) {
                        AtomicInteger i = (AtomicInteger) n;
                        if (i.get() > integerMax) {
                            i.set(0);
                        }
                    } else if (n instanceof AtomicLong) {
                        AtomicLong i = (AtomicLong) n;
                        if (i.get() > longMax) {
                            i.set(0);
                        }
                    }
                }

            }
        }, atomicCounterScanHour, atomicCounterScanHour, TimeUnit.HOURS);
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE);
        System.out.println(Long.MAX_VALUE);
    }

    public static String getAllExecutorInfo(ExecutorService... executors) {
        return getExecutorInfo(getAllExecutors(executors));
    }

    public static boolean threadSleep(long millis) {
        try {
            Thread.sleep(millis);
            return true;
        } catch (InterruptedException e1) {
            return false;
        }
    }

    public static ExecutorService[] getAllExecutors(ExecutorService... executors) {
        ExecutorService[] arr = new ExecutorService[4 + executors.length];
        int i = 0;
        arr[i++] = watchdog;
        arr[i++] = daemonExecutor;
        arr[i++] = defaultExecutor;
        arr[i++] = logExecutor;
        for (ExecutorService e : executors) {
            arr[i++] = e;
        }
        return arr;
    }

    /**
     * <pre>
     * 后台调度线程池
     * 注意:这里的线程都是后台线程,如果没有主线程,程序会立即退出
     */
    public static ScheduledExecutorService getDaemonExecutor() {
        if (daemonExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (daemonExecutor == null) {
                    daemonExecutor = Executors.newScheduledThreadPool(CORE_PROCESSOR_NUM, new NamedThreadFactory(PREFIX + "Daemon(Sche)", Thread.NORM_PRIORITY, true));
                }
            }
        }
        return daemonExecutor;
    }

    public static ExecutorService getDefaultExecutor() {
        if (defaultExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (defaultExecutor == null) {
                    defaultExecutor = Executors.newCachedThreadPool(new NamedThreadFactory(PREFIX + "Default", Thread.NORM_PRIORITY));

                    // 2011-12-19 以下方法，只会在 队列满的情况下 创建新的线程，不满足需求
                    // defaultExecutor = new ThreadPoolExecutor(0, CORE_PROCESSOR_NUM * 50, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(CORE_PROCESSOR_NUM * 1000), new NamedThreadFactory(
                    // PREFIX + "Default", Thread.NORM_PRIORITY), callerRunsPolicy);
                }
            }
        }
        return defaultExecutor;
    }

    public static String getExecutorInfo(ExecutorService... executors) {
        StringBuilder tmp = new StringBuilder();
        tmp.append(executorStatHeader);
        for (ExecutorService e : executors) {
            if (e != null) {
                String executorName = getExecutorName(e);
                if (e instanceof ThreadPoolExecutor) {
                    ThreadPoolExecutor executor = (ThreadPoolExecutor) e;
                    tmp.append(String.format(executorStatFmt, executorName, executor.getActiveCount(), executor.getPoolSize(), executor.getLargestPoolSize(), executor.getQueue().size(),
                            executor.getTaskCount(), executor.getCompletedTaskCount(), executor.getCorePoolSize(), executor.getMaximumPoolSize(),
                            HumanReadableUtil.timeSpan(executor.getKeepAliveTime(TimeUnit.MILLISECONDS)), executor.allowsCoreThreadTimeOut()));
                } else {
                    tmp.append("!!!").append(executorName);
                }
            }
        }
        return tmp.toString();
    }

    public static String getExecutorName(ExecutorService e) {
        NamedThreadFactory tf = getNamedExecutorThreadFactory(e);
        return tf == null ? e.getClass().getSimpleName() : tf.getNamePrefix();
    }

    public static ExecutorService getLogExecutor() {
        if (logExecutor == null) {
            synchronized (ConcurrentUtil.class) {
                if (logExecutor == null) {
                    // 这是原来 logback1.0.0里面的线程池配置，这里面问题是 LinkedBlockingQueue 没有设置大小，造成实际上不会满，从而都只是新建了一个log线程在跑任务
                    // 0 idle threads, 2 maximum threads, no idle waiting

                    // http://www.blogjava.net/killme2008/archive/2008/09/08/227661.html
                    // 一般情况下，ArrayBlockingQueue性能优于LinkedBlockingQueue,但是LinkedBlockingQueue是无界的。
                    // 这里刚好想要有界
                    // logExecutor = new ThreadPoolExecutor(0, CORE_PROCESSOR_NUM, 60L, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(CORE_PROCESSOR_NUM * 1000), new NamedThreadFactory(PREFIX
                    // + "Log", Thread.NORM_PRIORITY), discardPolicy);
                    // 2011-12-19 发现丢失日志也是不合适
                    int num = (int) Math.round(Math.sqrt(CORE_PROCESSOR_NUM));
                    logExecutor = new ThreadPoolExecutor(num, num, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), new NamedThreadFactory(PREFIX + "Log", Thread.MIN_PRIORITY));
                }
            }
        }
        return logExecutor;
    }

    public static NamedThreadFactory getNamedExecutorThreadFactory(ExecutorService e) {
        if (e instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor executor = (ThreadPoolExecutor) e;
            ThreadFactory tf = executor.getThreadFactory();
            if (tf instanceof NamedThreadFactory) {
                return (NamedThreadFactory) tf;
            }
        }
        return null;
    }

    /**
     * 后台扫描线程，需要跟后台线程隔离开，此线程池只专门用于看门狗式的监控
     */
    public static ScheduledExecutorService getWatchdog() {
        return watchdog;
    }

    public static AtomicInteger newAtomicInteger() {
        AtomicInteger i = new AtomicInteger();
        atomicCounterList.add(i);
        return i;
    }

    public static AtomicLong newAtomicLong() {
        AtomicLong i = new AtomicLong();
        atomicCounterList.add(i);
        return i;
    }

    public static abstract class ParalleledJob implements Runnable {

        private CountDownLatch latch;
        private Throwable throwable;

        public abstract void job() throws Throwable;

        @Override
        public final void run() {
            try {
                job();
            } catch (Throwable t) {
                throwable = t;
            } finally {
                if (null != latch) {
                    latch.countDown();
                }
            }
        }
    }

    /**
     * 一次提交多个任务，并行执行，当所有任务都执行完后再进行下一步，如果有异常则会抛出中断
     */
    public static void parallel(ParalleledJob... pj) {
        // 由于latch.await();会处于线程休眠状态,即使cdl减到0,也不会及时唤醒,提高此线程的优先级有助于尽快往下走.
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

        if (pj.length > 0) {
            CountDownLatch latch = new CountDownLatch(pj.length);
            for (ParalleledJob j : pj) {
                j.latch = latch;
                getDefaultExecutor().execute(j);
            }
            try {
                latch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            for (ParalleledJob j : pj) {
                if (null != j.throwable) {
                    throw new RuntimeException(j.throwable);
                }
            }
        }

        Thread.currentThread().setPriority(Thread.NORM_PRIORITY);
    }

    /**
     * 私有的构造方法
     */
    private ConcurrentUtil() {
    }
}
