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

package cn.wantedonline.puppy.httpserver.util;

import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by wangcheng on 2016/10/30.
 * 监控类
 */
public class TimeSpanStat {
    protected AtomicLong all_num = new AtomicLong(); //记录处理总数
    protected AtomicLong all_span = new AtomicLong(); //记录处理总时长
    protected Logger logger;
    protected volatile long max_span;
    protected String name;
    protected AtomicLong slow_num = new AtomicLong();
    protected AtomicLong slow_span = new AtomicLong();
    protected int slowThreshold;
    protected String tableHeader;
    protected String htmlTableHeader;
    protected String timeStatFmt;
    protected String timeStatHtmlFmt;
    protected boolean warn;

    public TimeSpanStat(String name, int slowThreshold, boolean warn, Logger logger) {
        this.name = name;
        this.slowThreshold = slowThreshold;
        this.logger = logger;
        this.warn = warn;
        initFormat(35, 0);
    }

    public TimeSpanStat(String name, Logger logger) {
        this(name, 1000, true, logger);
    }

    public long getAllNum() {
        return all_num.get();
    }

    public void setAllNum(long all_num) {
        this.all_num.set(all_num);
    }

    public long getAllSpan() {
        return all_span.get();
    }

    public void setAllSpan(long all_span) {
        this.all_span.set(all_span);
    }

    public long getSlowNum() {
        return slow_num.get();
    }

    public void setSlowNum(long slow_num) {
        this.slow_num.set(slow_num);
    }

    public long getSlowSpan() {
        return slow_span.get();
    }

    public void setSlowSpan(long slow_span) {
        this.slow_span.set(slow_span);
    }

    public long getMaxSpan() {
        return max_span;
    }

    public void setMaxSpan(long max_span) {
        this.max_span = max_span;
    }

    public String getTableHeader() {
        return tableHeader;
    }

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
            sb.append(" ");
        }
        this.timeStatFmt = "%-" + nameLen + "s %-8s %-20s %-8s %-20s %-20s %-20s %-20s\n";
        this.tableHeader = String.format(timeStatFmt, sb.toString(), "times", "avg", "slow", "slow_avg", "max", "slow_span", "all_span");
        this.timeStatHtmlFmt = "<tr><td>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td><td nowrap>%s</td></tr>\\n";;
        this.htmlTableHeader = String.format(timeStatHtmlFmt, sb.toString(), "<a href=\"?timesOrder=1\">times</a>", "<a href=\"?avgOrder=1\">avg</a>", "slow", "slow_avg", "max", "slow_span", "all_span");
    }

    public void record(long end, long begin, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        all_num.incrementAndGet();
        long span = end -begin;
        all_span.addAndGet(span);
        if (span >= slowThreshold) {
            slow_num.incrementAndGet();
            slow_span.addAndGet(span);
            if (warn) {
                warn(end, begin, arg)
            }
        }
        if (span > max_span) {
            max_span = span;
        }
    }

    public void record(long end, long begin, int count, Object arg) {
        if (begin <= 0 || end <= 0) {
            return;
        }
        all_num.addAndGet(count);
        long span = end - begin;
        all_span.addAndGet(span);
        if (span >= slowThreshold) {
            slow_num.incrementAndGet();
            slow_span.addAndGet(span);
            if (warn) {
                warn(end, begin, arg)
            }
        }
        if (span > max_span) {
            max_span = span;
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
        long all_numTMP = all_num.get();
        long all_spanTMP = all_span.get();
        long slow_numTMP = slow_num.get();
        long slow_spanTMP = slow_span.get();
        long allAvg = all_numTMP > 0 ? all_spanTMP / all_numTMP : 0;
        long slowAvg = slow_numTMP > 0 ? slow_spanTMP / slow_numTMP : 0;
        return String.format(timeStatFmt, first, all_numTMP > 0 ? all_numTMP : "", HumanReadableUtil.timeSpan(allAvg), slow_numTMP > 0 ? slow_numTMP : "", HumanReadableUtil.timeSpan(slowAvg),
                HumanReadableUtil.timeSpan(max_span), HumanReadableUtil.timeSpan(slow_spanTMP), HumanReadableUtil.timeSpan(all_spanTMP));
    }

    protected void warn(long end, long begin, Object arg) {
        logger.error("SLOW_PROCESS:{}:{} [{}ms]\n", new Object[] {
                name,
                arg,
                end - begin
        });
    }

    public boolean isNeedReset() {
        return all_num.get() < 0 || all_span.get() < 0;
    }

    public static <T extends TimeSpanStat> void sort(List<T> list, HttpRequest request, HttpResponse response) {
        boolean avgOrder = request.getP
    }
}
