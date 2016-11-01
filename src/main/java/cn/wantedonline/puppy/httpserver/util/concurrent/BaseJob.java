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
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.FixNameMemAppender;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.FixSizeMemAppender;
import cn.wantedonline.puppy.util.DateStringUtil;
import org.slf4j.Logger;

import java.util.Date;

/**
 * Created by wangcheng on 2016/11/1.
 */
public abstract class BaseJob implements Runnable, FixNameMemAppender.FixNameLog {
    public static int DEFAULT_LOG_SIZE = 0;

    protected String jobName;
    protected String description;
    private FixSizeMemAppender.FixSizeLog lastRunningInfo;
    private Date lastStartTime = null;
    protected Logger logger;
    private boolean logAppenderInited = false;
    protected int logSize;
    private volatile boolean running = false;
    private FixSizeMemAppender.FixSizeLog runningInfo;
    private Thread runningThread;
    protected int runTimes;
    private boolean scheduleNext;
    private volatile boolean stop = true;

    public BaseJob(Logger log) {
        this(log, log.getName(), log.getName(), DEFAULT_LOG_SIZE);
    }

    public BaseJob(Logger log, int logSize) {
        this(log, log.getName(), log.getName(), logSize);
    }

    public BaseJob(Logger log, String jobName, int logSize) {
        this(log, jobName, jobName, logSize);
    }

    public BaseJob(Logger log, String jobName, String description, int logSize) {
        this.logger = log;
        this.jobName = jobName;
        this.description = description;
        this.logSize = logSize;
        initFixNamememAppender();
    }

    private void initFixNamememAppender() {
        if (logSize > 0 && !logAppenderInited) {
            FixNameMemAppender.register(this);
            logAppenderInited = true;
        }
    }

    private void resetRunningInfo() {
        this.lastRunningInfo = runningInfo;
        this.runningInfo = logSize > 0 ? new FixSizeMemAppender.FixSizeLog(jobName, logSize) : null;
    }

    public void begin() {
        runningThread = Thread.currentThread();
        boolean urge = scheduleNext;
        this.scheduleNext = false;
        resetRunningInfo();
        running = true;
        stop = false;
        runTimes++;
        Date now = new Date();
        logger.info(
                "START JOB [{}({})],RUN TIMES:{}{}", new Object[] {
                        jobName,
                        description,
                        runTimes,
                        urge ? "[URGE]" : ""
                }
        );
        lastStartTime = now;
    }

    public void cancel() {
        stop = true;
        Thread t = getRunningThread();
        if (null != t) {
            t.interrupt();
        }
    }

    public void end() {
        runningThread = null;
        long span = System.currentTimeMillis();
        logger.info(
                "END   JOB [{}({})] USING {}", new Object[] {
                        jobName,
                        description,
                        HumanReadableUtil.timeSpan(span)
                }
        );
    }

    protected String getRunningInfo(int begin, int end, int tail, boolean last) {
        FixSizeMemAppender.FixSizeLog fsl = last ? lastRunningInfo : runningInfo;
        if (null == fsl) {
            return "";
        }
        if (tail > 0) {
            return fsl.tail(tail);
        }
        return fsl.sub(begin, end);
    }

    public String getLastStartTimeString() {
        if (null == lastStartTime) {
            return "";
        }
        return DateStringUtil.DEFAULT.format(lastStartTime);
    }

    @Override
    public String getLoggerName() {
        return logger.getName();
    }

    public Thread getRunningThread() {
        return runningThread;
    }

    public int getRunTimes() {
        return runTimes;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isStop() {
        return stop;
    }

    @Override
    public void log(String msg) {
        if (null != runningInfo) {
            runningInfo.log(msg);
        }
    }

    public abstract void process() throws Throwable;

    @Override
    public void run() {
        do {
            begin();
            try {
                process();
            } catch (InterruptedException e) {
                logger.error("INTERRUPT JOB [{}({})]", new Object[] {
                        jobName,
                        description,
                        e
                });
            } catch (Throwable e1) {
                logger.error("",e1);
            }
            end();
        } while(scheduleNext);
    }

    public String getJobName() {
        return jobName;
    }

    public void urge() {
        if (running) {
            scheduleNext = true;
        }
    }

    @Override
    public String toString() {
        return String.format("[jobName=%s, description=%s, log=%s, runTimes=%s, lastStartTime=%s, scheduleNext=%s]", jobName, description, logger, runTimes, getLastStartTimeString(), scheduleNext);
    }
}
