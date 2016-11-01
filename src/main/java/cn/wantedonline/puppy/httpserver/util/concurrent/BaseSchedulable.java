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

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * 1. 可以重改定时时间 2. scheduler 定时任务，跑runnable时，如果里面 抛错时，原来的定时任务是直接停止的，现在改成内部捕获所有throwable
 *
 */
public abstract class BaseSchedulable extends BaseRunnable {

    private ScheduledFuture<?> lastScheduledFuture;
    private ScheduledExecutorService executor;

    public BaseSchedulable() {
        this(null);
    }

    public BaseSchedulable(ScheduledExecutorService scheduledExecutorService) {// 注意默认是 后台线程，在主程序退出时，也会退出
        this.executor = scheduledExecutorService == null ? ConcurrentUtil.getDaemonExecutor() : scheduledExecutorService;
    }

    public boolean cancel() {
        return cancel(true);
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        ScheduledFuture<?> last = lastScheduledFuture;
        boolean r = true;// 如果还没有schedule也认为是 cacnel返回true
        if (lastScheduledFuture != null) {
            r = lastScheduledFuture.cancel(true);
        }
        log.error("cancel lastFuture:{}", last != null ? "isCancelled:" + last.isCancelled() : "N/A");
        return r;
    }

    public void schedule(long initialDelay, long period, TimeUnit unit, boolean atFixedRate) {
        ScheduledFuture<?> last = lastScheduledFuture;
        if (lastScheduledFuture != null) {
            lastScheduledFuture.cancel(true);
        }
        if (period <= 0) {
            log.info("schedule initialDelay:{},period:{},unit:{},atFixRate:{},lastFuture:{}", new Object[] {
                initialDelay,
                period,
                unit,
                atFixedRate,
                last != null ? "isCancelled:" + last.isCancelled() : "N/A"
            });
            return;
        }
        if (atFixedRate) {
            lastScheduledFuture = executor.scheduleAtFixedRate(this, initialDelay, period, unit);
        } else {
            lastScheduledFuture = executor.scheduleWithFixedDelay(this, initialDelay, period, unit);
        }
        log.info("schedule initialDelay:{},period:{},unit:{},atFixRate:{},lastFuture:{}", new Object[] {
            initialDelay,
            period,
            unit,
            atFixedRate,
            last != null ? "isCancelled:" + last.isCancelled() : "N/A"
        });
    }

    public void scheduleWithFixedDelaySec(long sec) {
        this.schedule(sec, sec, TimeUnit.SECONDS, false);
    }

    public void scheduleWithFixedDelayMs(long millsecond) {
        this.schedule(millsecond, millsecond, TimeUnit.MILLISECONDS, false);
    }

    public void scheduleAtFixedRateSec(long sec) {
        this.schedule(sec, sec, TimeUnit.SECONDS, true);
    }

    public void scheduleAtFixedRateMs(long millsecond) {
        this.schedule(millsecond, millsecond, TimeUnit.MILLISECONDS, true);
    }

    public static void main(String[] args) throws InterruptedException {
        BaseSchedulable bs = new BaseSchedulable(Executors.newScheduledThreadPool(1)) {

            @Override
            public void process() throws Throwable {
                System.out.println("gogogo");
            }
        };
        bs.scheduleAtFixedRateSec(1);
        Thread.sleep(10000);
        bs.scheduleAtFixedRateMs(100);
        Thread.sleep(2000);
        bs.scheduleWithFixedDelayMs(500);
    }
}
