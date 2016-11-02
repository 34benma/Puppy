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

package cn.wantedonline.puppy.util;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import cn.wantedonline.puppy.exception.AntiDosError;
import org.slf4j.Logger;

/**
 * 组件：防止客户端Denial of Service攻击 antiDosSpan秒内,防止一个指定类型请求最多响应antiDosCount次
 *
 */
public class AntiDos {

    private class VisitInfo {

        private volatile AtomicInteger counter;
        private long createTime;

        public void init() {
            AtomicInteger i = new AtomicInteger(1);
            counter = i;
            createTime = System.currentTimeMillis();
        }

        /**
         * 首次也有可能会超标，需要对此进行处理 
         */
        public boolean init(int count) {
            counter = new AtomicInteger(count);
            createTime = System.currentTimeMillis();
            if (count >= antiDosCount) {
                return false;
            }
            return true;
        }

        public boolean isStale() {
            return System.currentTimeMillis() - createTime >= antiDosSpanMs;
        }

        public boolean isStale(long now) {
            return now - createTime >= antiDosSpanMs;
        }

        public boolean visit(Object checkid) {
            if (null == counter) {
                return true;
            }
            int count = counter.incrementAndGet();
            if (isStale()) {
                removeVisitInfo(checkid, count);
            }
            if (count >= antiDosCount) {
                return false;
            }
            return true;
        }

        public boolean visit(Object checkid, int count) {
            int c = counter.addAndGet(count);
            if (isStale()) {
                removeVisitInfo(checkid, c);
            }
            if (c >= antiDosCount) {
                return false;
            }
            return true;
        }

    }

    private static final Logger log = Log.getLogger();

    public static void main(String[] args) throws InterruptedException {
        AntiDos dos = new AntiDos().initSweeper();
        while (true) {
            if (dos.visit(1l)) {
                System.out.println(System.currentTimeMillis() + " - true");
            } else {
                System.err.println(System.currentTimeMillis() + " - false");
            }
            Thread.sleep(300);
        }
    }

    private int antiDosCount;
    private int antiDosSpanMs;
    private int antiDosWarnCount;
    private ScheduledFuture<?> lastScheduledFuture;
    private ConcurrentHashMap<Object, VisitInfo> visitInfos = new ConcurrentHashMap<Object, VisitInfo>();

    public AntiDos() {
        this(10, 10);
    }

    public AntiDos(int antiDosCount, int antiDosSpanSec) {
        init(antiDosCount, antiDosSpanSec);
    }

    public AntiDos(String conf) {
        List<String> list = StringTools.splitAndTrim(conf, ",");
        if (list.size() == 2) {
            init(ValueUtil.getInteger(list.get(0), -1), ValueUtil.getInteger(list.get(1), -1));
        } else {
            throw new IllegalArgumentException("conf's pattern should be antiDosCount,antiDosSpanSec");
        }
    }

    public int getAntiDosCount() {
        return antiDosCount;
    }

    public int getAntiDosSpan() {
        return antiDosSpanMs;
    }

    public int getAntiDosWarnCount() {
        return antiDosWarnCount;
    }

    public ConcurrentHashMap<Object, VisitInfo> getVisitInfos() {
        return visitInfos;
    }

    private void init(int antiDosCount_l, int antiDosSpanSec) {
        if (antiDosCount_l < 1) {
            throw new IllegalArgumentException("antiDosCount must greater than 1");
        }
        if (antiDosSpanSec < 1) {
            throw new IllegalArgumentException("antiDosSpan must greater than 1");
        }

        this.antiDosCount = antiDosCount_l;
        this.antiDosWarnCount = antiDosCount_l * 5;
        this.antiDosSpanMs = antiDosSpanSec * 1000;
    }

    public AntiDos initSweeper() {
        return initSweeper(0, null);
    }

    public AntiDos initSweeper(int sweepSecSpan, ScheduledExecutorService sweepScheduler) {
        initSweeper(sweepSecSpan, sweepSecSpan, sweepScheduler);
        return this;
    }

    public AntiDos initSweeper(int initialDelay, int sweepSecSpan, ScheduledExecutorService sweepScheduler) {
        int sweepSecSpan_l = sweepSecSpan < 1 ? 600 : sweepSecSpan;// 默认10分钟清除一次
        int initialDelay_l = initialDelay < 1 ? 600 : initialDelay;// 默认延迟10分钟
        ScheduledExecutorService sweepScheduler_l = sweepScheduler == null ? Executors.newScheduledThreadPool(1) : sweepScheduler;

        if (lastScheduledFuture != null) {
            lastScheduledFuture.cancel(true);
        }

        lastScheduledFuture = sweepScheduler_l.scheduleAtFixedRate(new Runnable() {

            @Override
            public void run() {
                try {
                    long now = System.currentTimeMillis();
                    for (Map.Entry<Object, VisitInfo> e : visitInfos.entrySet()) {
                        VisitInfo info = e.getValue();
                        if (info.isStale(now)) {
                            removeVisitInfo(e.getKey(), info.counter.get());
                        }
                    }
                } catch (Throwable e) {
                    log.error("", e);
                }
            }
        }, initialDelay_l, sweepSecSpan_l, TimeUnit.SECONDS);
        return this;
    }

    private void removeVisitInfo(Object checkid, int count) {
        if (count >= antiDosWarnCount) {
            log.warn("checkid:{} last {} SEC visitCount is {}", new Object[] {
                checkid,
                antiDosSpanMs,
                count
            });
        }
        visitInfos.remove(checkid);
    }

    public void setAntiDosCount(int antiDosCount) {
        this.antiDosCount = antiDosCount;
    }

    public void setAntiDosSpan(int antiDosSpanSec) {
        this.antiDosSpanMs = antiDosSpanSec * 1000;
    }

    public void setAntiDosWarnCount(int antiDosWarnCount) {
        this.antiDosWarnCount = antiDosWarnCount;
    }

    public boolean visit(Object checkid) {
        VisitInfo newInfo = new VisitInfo();
        VisitInfo ori = visitInfos.putIfAbsent(checkid, newInfo);
        if (ori != null) {
            newInfo = null;
            return ori.visit(checkid);
        }
        newInfo.init();
        return true;
    }

    public boolean visit(Object checkid, int count) {
        VisitInfo newInfo = new VisitInfo();
        VisitInfo ori = visitInfos.putIfAbsent(checkid, newInfo);
        if (ori != null) {
            newInfo = null;
            return ori.visit(checkid, count);
        }
        return newInfo.init(count);
    }

    public void visitAndCheck(Object checkid) {
        if (!visit(checkid)) {
            throw AntiDosError.INSTANCE;
        }
    }

    public void visitAndCheck(Object checkid, int count) {
        if (!visit(checkid, count)) {
            throw AntiDosError.INSTANCE;
        }
    }
}
