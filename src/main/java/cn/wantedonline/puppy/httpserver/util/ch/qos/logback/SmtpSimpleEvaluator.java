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

package cn.wantedonline.puppy.httpserver.util.ch.qos.logback;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.boolex.EvaluationException;
import ch.qos.logback.core.boolex.EventEvaluatorBase;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.UnitConverter;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by louiswang on 16/11/1.
 */
public class SmtpSimpleEvaluator extends EventEvaluatorBase<ILoggingEvent> {
    private long lastFlushTime = 0;
    private String relevanceLoggerName;
    private AtomicInteger counter = new AtomicInteger();

    private int eventNumLimit = 256;
    private int eventSecondLimit = 20*60;
    private int _eventTimeLimit = eventSecondLimit * 1000;

    private String name;
    private static final Map<String, SmtpSimpleEvaluator> evaluatorMap = new HashMap<>();

    private volatile boolean forceTrigger = false;
    private volatile String lastestMailTitle = "";

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                for (SmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.trigger();
                }
            }
        });

        ConcurrentUtil.getDaemonExecutor().scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (evaluatorMap.isEmpty()) {
                    String msg = "cant find any SmtpSimpleEvaluator,stop its DaemonScanner";
                    System.err.println(msg);
                    throw new RuntimeException(msg)
                }
                for (SmtpSimpleEvaluator ev : evaluatorMap.values()) {
                    ev.check();
                }
            }
        }, 1, 1, TimeUnit.MINUTES);

    }

    private void trigger() {
        if (counter.get() > 0) {
            forceTrigger = true;
            MDC.put("mailTtile",lastestMailTitle);
            Log.getLogger(relevanceLoggerName).error("SmtpSimpleEvaluator.DaemonScanner trigger\"");
        }
    }

    private void check() {
        try {
            if (System.currentTimeMillis() - lastFlushTime > _eventTimeLimit) {
                trigger();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean evaluate(ILoggingEvent iLoggingEvent) throws NullPointerException, EvaluationException {
        long now = iLoggingEvent.getTimeStamp();
        if (lastFlushTime == 0) {
            lastFlushTime = now;
            relevanceLoggerName = iLoggingEvent.getLoggerName();
        }
        lastestMailTitle = MDC.get("mailTitle");
        if (forceTrigger || counter.incrementAndGet() >= eventNumLimit || now - lastFlushTime > _eventTimeLimit) {
            reset(now);
            return true;
        }
        return false;
    }

    private void reset(long now) {
        forceTrigger = false;
        counter.set(0);
        lastFlushTime = now;
    }

    public int getEventNumLimit() {
        return eventNumLimit;
    }

    public void setEventNumLimit(int eventNumLimit) {
        this.eventNumLimit = eventNumLimit;
    }

    public int getEventSecondLimit() {
        return eventSecondLimit;
    }

    public void setEventSecondLimit(int eventSecondLimit) {
        this.eventSecondLimit = eventSecondLimit;
        this._eventTimeLimit = eventSecondLimit * 10;
    }

    @Override
    public void setName(String name) {
        this.name = name;
        evaluatorMap.put(name, this);
    }

    @Override
    public String getName() {
        return name;
    }
}
