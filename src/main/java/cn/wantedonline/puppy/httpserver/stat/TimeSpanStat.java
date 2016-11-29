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
    private String tableHeader;
    private String htmlTableHeader;
    private String timeStatFmt;
    private String timeStatHtmlFmt;
    private boolean warn;

    public TimeSpanStat(String name, int slowThreshold, boolean warn, Logger log) {
        this.name = name;
        this.slowThreshold = slowThreshold;
        this.log = log;
        this.warn = warn;
        initFormat(35, 0);
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

    public String getTableHeader() {
        return tableHeader;
    }

    /** 暂时只处理 times和avg字段的排序，以后有需要再加其它字段 */
    public String getHtmlTableHeader() {
        return htmlTableHeader;
    }

    public String getTableHeader(boolean useTxt) {
        if (useTxt) {
            return getTableHeader();
        }
        return getHtmlTableHeader();
    }

    public void initFormat(int nameLen, int nameFullWidthCharNum) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < nameFullWidthCharNum; i++) {
            sb.append("　");
        }
        this.timeStatFmt = "%-" + nameLen + "s %-8s %-20s %-8s %-20s %-20s %-20s %-20s\n";
        this.tableHeader = String.format(timeStatFmt, sb.toString(), "times", "avg", "slow", "slow_avg", "max", "slow_span", "all_span");
        // 暂时只处理 times和avg字段的排序，以后有需要再加其它字段
        this.timeStatHtmlFmt = "<tr><td>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td></tr>\n";
        this.htmlTableHeader = String.format(timeStatHtmlFmt, sb.toString(), "<a href=\"?timesOrder=1\">times</a>", "<a href=\"?avgOrder=1\">avg</a>", "slow", "slow_avg", "max", "slow_span",
                "all_span");
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

    @Override
    public String toString() {
        return toString(timeStatFmt, name);
    }

    public String toHtmlString() {
        return toString(timeStatHtmlFmt, name);
    }

    public String toString(String first) {
        return toString(timeStatFmt, first);
    }

    public String toHtmlString(String first) {
        return toString(timeStatHtmlFmt, first);
    }

    public String toString(String timeStatFmt, String first) {
        long all_numTMP = totalCount.get(); // 请求总次数
        long all_spanTMP = totalSpan.get(); // 请求总时长
        long slow_numTMP = slowCount.get(); // 慢的总个数
        long slow_spanTMP = slowSpan.get(); // 慢的总时长
        long allAvg = all_numTMP > 0 ? all_spanTMP / all_numTMP : 0; // 请求平均时长
        long slowAvg = slow_numTMP > 0 ? slow_spanTMP / slow_numTMP : 0; // 慢的平均时长
        return String.format(timeStatFmt, first, all_numTMP > 0 ? all_numTMP : "", HumanReadableUtil.timeSpan(allAvg), slow_numTMP > 0 ? slow_numTMP : "", HumanReadableUtil.timeSpan(slowAvg),
                HumanReadableUtil.timeSpan(maxSpan), HumanReadableUtil.timeSpan(slow_spanTMP), HumanReadableUtil.timeSpan(all_spanTMP));
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
