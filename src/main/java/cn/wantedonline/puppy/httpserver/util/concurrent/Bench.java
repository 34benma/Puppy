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

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * <pre>
 * simple micro-benchmarking tool
 * 基准性能测试工具
 * 
 * 注意:-server一般能提高性能测试结果
 * </pre>
 *
 */
public class Bench {

    private class TimeMeasureProxy implements Runnable {

        private CountDownLatch measureLatch;
        private Runnable runnable;

        public TimeMeasureProxy(Runnable runnable, int measurements) {
            this.runnable = runnable;
            this.measureLatch = new CountDownLatch(measurements);
        }

        /**
         * 等待所有执行完成
         */
        public void await() {
            try {
                measureLatch.await();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public void run() {
            this.runnable.run();
            this.measureLatch.countDown();
        }
    }

    public static int DEFAULT_WARMUPS = 1000;

    /**
     * 计算内存的使用量
     * 
     * @return
     */
    public static long memoryUsed() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    /**
     * 重新装载JVM
     */
    public static void restoreJvm0() {
        final CountDownLatch drained = new CountDownLatch(1);
        try {
            System.gc(); // enqueue finalizable objects
            new Object() {

                @Override
                protected void finalize() {
                    drained.countDown();
                }
            };
            System.gc(); // enqueue detector
            drained.await(); // wait for finalizer queue to drain
            System.gc(); // cleanup finalized objects
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }

    /**
     * 重新装载JVM
     */
    public static void restoreJvm() {
        int maxRestoreJvmLoops = 10;
        long memUsedPrev = memoryUsed();
        for (int i = 0; i < maxRestoreJvmLoops; i++) {
            System.runFinalization();
            System.gc();

            long memUsedNow = memoryUsed();
            // break early if have no more finalization and get constant mem
            // used
            if ((ManagementFactory.getMemoryMXBean().getObjectPendingFinalizationCount() == 0) && (memUsedNow >= memUsedPrev)) {
                break;
            } else {
                memUsedPrev = memUsedNow;
            }
        }
    }

    /**
     * 是否为debug模式
     */
    private boolean debug = true;
    /**
     * 整型的格式
     */
    private DecimalFormat integerFormat = new DecimalFormat("#,##0.000");
    private ExecutorService mainExecutor;
    private int measurements;
    private int threads;
    private ExecutorService warmUpExecutor;
    private int warmupMeasurements = DEFAULT_WARMUPS;
    private int timesPerMeasurements;

    public Bench(int threads, int measurements, int warmupMeasurements, int timesPerMeasurements) {
        this.threads = threads <= 0 ? 1 : threads;
        this.measurements = measurements <= 0 ? 1 : measurements;
        this.warmupMeasurements = warmupMeasurements <= 0 ? DEFAULT_WARMUPS : warmupMeasurements;
        this.timesPerMeasurements = timesPerMeasurements <= 0 ? 1 : timesPerMeasurements;
        warmUpExecutor = Executors.newSingleThreadExecutor();
        mainExecutor = Executors.newFixedThreadPool(this.threads);
        // doWarmUpExecutor();
    }

    /**
     * 将线程 task 执行times次
     * 
     * @param task 线程
     * @param executor 线程池
     * @param times 执行的次数
     */
    private void _run(Runnable task, ExecutorService executor, int times) {
        if (executor == null || task == null) {
            return;
        }
        TimeMeasureProxy timeMeasureProxy = new TimeMeasureProxy(task, times);
        for (int i = 0; i < times; i++) {
            executor.execute(timeMeasureProxy);
        }
        timeMeasureProxy.await();
    }

    /**
     * 将某线程执行指定的次数，并输出所花费的时间
     * 
     * @param label 线程的表示
     * @param task 线程
     */
    private void doMeasure(String label, Runnable task) {
        restoreJvm();
        long startTime = System.nanoTime();
        _run(task, mainExecutor, measurements);
        printResult(label, startTime);
    }

    /**
     * <pre>
     * 可以使用 -XX:+PrintCompilation来判断是否真正warmup
     * 而-verbose:gc可以显示执行的gc详细信息
     * </pre>
     * 
     * @param task 线程
     */
    private void doWarmup(Runnable task) {
        restoreJvm();
        long startTime = System.nanoTime();
        _run(task, warmUpExecutor, warmupMeasurements);
    }

    // /**
    // * TODO:是否有必要增加
    // */
    // private void doWarmUpExecutor() {
    // long startTime = System.nanoTime();
    // if (debug)
    // System.out.println("\tdoWarmUpExecutor");
    // ExecutorService es = Executors.newFixedThreadPool(2);
    // _run(new Runnable() {
    // public void run() {
    // try {
    // LinkedBlockingQueue<Runnable> queue = new
    // LinkedBlockingQueue<Runnable>();
    // queue.offer(this);
    // queue.peek();
    // queue.add(this);
    // queue.poll();
    // _run(null, null, 0);
    // } catch (Exception e) {
    // }
    // }
    // }, es, 1000);
    // es.shutdownNow();
    // restoreJvm();
    // if (debug)
    // System.out.println("\tdoWarmUpExecutor Using" + (System.nanoTime() -
    // startTime) / 1000000 + " MS");
    // }
    /**
     * 开始衡量
     */
    public void measure(String label, Runnable task) {
        if (debug) {
            System.out.println("\tSTARTUP WARMUP  " + label);
        }
        doWarmup(task);
        if (debug) {
            System.out.println("\tSTARTUP MEASURE " + label);
        }
        doMeasure(label, task);
    }

    /**
     * 输出线程运行的时间结果
     * 
     * @param label 线程标识
     * @param startTime 线程开始时间
     */
    private void printResult(String label, long startTime) {
        long span = System.nanoTime() - startTime;
        double avg = span / measurements / timesPerMeasurements / 1000000.0;
        String avgStr = integerFormat.format(avg) + "ms";

        double total = span / 1000000.0;
        String totalStr = integerFormat.format(total) + "ms";

        double tps = measurements * timesPerMeasurements / (span / 1000000000.0);
        String tpsStr = integerFormat.format(tps);

        System.out.println(String.format("%-30s[avg:%-20s total:%-20s tps:%-20s mem:%-10s]", label, avgStr, totalStr, tpsStr, HumanReadableUtil.byteSize(memoryUsed())));
    }

    /**
     * 关闭线程池
     */
    public void shutdown() {
        warmUpExecutor.shutdown();
        mainExecutor.shutdown();
    }

    /**
     * 输出线程池信息
     */
    @Override
    public String toString() {
        return "Bench [threads=" + threads + ", measurements=" + measurements + ", warmupMeasurements=" + warmupMeasurements + ", timesPerMeasurements=" + timesPerMeasurements + "]";
    }

    /**
     * 设置是否为调试模式
     * 
     * @param debug
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }
}
