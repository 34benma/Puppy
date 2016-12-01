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

package cn.wantedonline.puppy.httpserver.stat;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.util.HumanReadableUtil;
import org.slf4j.Logger;

/**
 * <pre>
 *     请求响应，编解码时间统计
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 2016/11/29
 */
public class TimeSpanStat {

    private AtomicLong totalCount = new AtomicLong(); // 处理总次数
    private AtomicLong totalSpan = new AtomicLong(); // 处理总时长
    private Logger log;
    private volatile long maxSpan;
    private String name = "";
    private AtomicLong slowCount = new AtomicLong(); // 慢的总个数
    private AtomicLong slowSpan = new AtomicLong(); // 慢的总时长
    private int slowThreshold;
    private boolean warn;

    public TimeSpanStat(String name, int slowThreshold, boolean warn, Logger log) {
        this.name = name;
        this.slowThreshold = slowThreshold;
        this.log = log;
        this.warn = warn;
    }

    public TimeSpanStat(String name, Logger log) {
        this(name, 1000, true, log);
    }

    public long getTotalCount() {

        return totalCount.get();
    }

    public void setTotalCount(long totalCount) {
        this.totalCount.set(totalCount);
    }

    public long getTotalSpan() {
        return totalSpan.get();
    }


    public void setTotalSpan(long totalSpan) {
        this.totalSpan.set(totalSpan);
    }

    public long getSlowCount() {
        return slowCount.get();
    }


    public void setSlowCount(long slowCount) {
        this.slowCount.set(slowCount);
    }

    public long getSlowSpan() {
        return slowSpan.get();
    }


    public void setSlowSpan(long slowSpan) {
        this.slowSpan.set(slowSpan);
    }

    /**
     * 提取最大时长
     */
    public long getMaxSpan() {
        return maxSpan;
    }

    /**
     * 设置最大时长
     */
    public void setMaxSpan(long maxSpan) {
        this.maxSpan = maxSpan;
    }

    public void record(long end, long begin, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        totalCount.incrementAndGet();
        long span = end - begin;
        totalSpan.addAndGet(span);
        if (span >= slowThreshold) {
            slowCount.incrementAndGet();
            slowSpan.addAndGet(span);
            if (warn) {
                warn(end, begin, arg);
            }
        }
        if (span > maxSpan) {
            maxSpan = span;
        }
    }

    public void record(long end, long begin, int count, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        totalCount.addAndGet(count);
        long span = end - begin;
        totalSpan.addAndGet(span);
        if (span / count >= slowThreshold) {
            slowCount.addAndGet(count);
            slowSpan.addAndGet(span);
            if (warn) {
                warn(end, begin, arg);
            }
        }
        if (span > maxSpan) {
            maxSpan = span;
        }
    }


    protected void warn(long end, long begin, Object arg) {
        log.error("SLOW_PROCESS:{}:{} [{}ms]\n", new Object[] {
            name,
            arg,
            end - begin
        });
    }

    public boolean isNeedReset() {
        return totalCount.get() < 0 || totalSpan.get() < 0;
    }

    /** 默认按次数倒序排序 */
    public static <T extends TimeSpanStat> void sort(List<T> list, HttpRequest request, HttpResponse response) {
        boolean avgOrder = request.getParameterBoolean("avgOrder", false);
        // 默认按次数倒序排序
        Collections.sort(list, new Comparator<T>() {

            @Override
            public int compare(T o1, T o2) {
                long a1 = o1 == null ? 0 : o1.getTotalCount();
                long a2 = o2 == null ? 0 : o2.getTotalCount();
                return (int) (a2 - a1);
            }
        });
        if (avgOrder) {
            Collections.sort(list, new Comparator<T>() {

                @Override
                public int compare(T o1, T o2) {
                    long o1AllNum = o1 == null ? 0 : o1.getTotalCount();
                    long o1AllSpan = o1 == null ? 0 : o1.getTotalSpan();
                    long o2AllNum = o2 == null ? 0 : o2.getTotalCount();
                    long o2AllSpan = o2 == null ? 0 : o2.getTotalSpan();
                    return (int) ((o2AllNum == 0 ? 0 : o2AllSpan / o2AllNum) - (o1AllNum == 0 ? 0 : o1AllSpan / o1AllNum));
                }
            });
        }
    }
}
