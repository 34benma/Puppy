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

import cn.wantedonline.puppy.httpserver.util.StringHelper;
import cn.wantedonline.puppy.util.DateUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.StringTools;
import org.slf4j.Logger;

import java.text.DateFormat;
import java.util.Date;

/**
 * Created by wangcheng on 2016/11/1.
 */
public abstract class BaseRunnable implements Runnable {
    protected Date lastBeginTime = null;
    protected Date lastEndTime = null;
    protected long lastSpan = -1;
    protected Throwable lastThrowable = null;
    protected final Logger log = Log.getLogger(this.getClass().getName());

    protected String name = log.getName();
    protected boolean logEnable = true;

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    public void afterProcess() {
        if (!logEnable) {
            return;
        }
        if (lastThrowable == null) {
            log.info("END   run {},USING {} MS...", new Object[] {
                    name,
                    lastSpan
            });
        } else {
            log.error("END   run {},USING {} MS...", new Object[] {
                    name,
                    lastSpan,
                    lastThrowable
            });
        }
    }

    public void beforeProcess() {
        if (logEnable) {
            log.debug("BEGIN run {}...", name);
        }
    }

    public String getLastBeginTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastBeginTime == null ? "" : df.format(lastBeginTime);
    }

    public String getLastEndTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastEndTime == null ? "" : df.format(lastEndTime);
    }

    public String getLastThrowableStr() {
        return lastThrowable == null ? "" : StringHelper.printThrowable(lastThrowable).toString();;
    }

    public String getExtendInfo() {
        return null;
    }

    public abstract void process() throws Throwable;

    @Override
    public synchronized void run() {
        Date begin = new Date();
        Throwable ex = null;
        try {
            beforeProcess();
            process();
        } catch (Throwable e) {
            ex = e;
        }
        Date end = new Date();

        lastBeginTime = begin;
        lastEndTime = end;
        lastSpan = end.getTime() - begin.getTime();
        lastThrowable = ex;
        try {
            afterProcess();
        } catch (Throwable e) {
            log.error("", e);
        }
    }

    @Override
    public String toString() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(name);
        tmp.append(":lastBeginTime=");
        tmp.append(getLastBeginTimeStr());
        tmp.append(",lastSpan=");
        tmp.append(lastSpan);
        String ext = getExtendInfo();
        if (StringTools.isNotEmpty(ext)) {
            tmp.append(",");
            tmp.append(ext);
        }
        if (lastThrowable != null) {
            tmp.append("\n");
            StringHelper.printThrowable(tmp, lastThrowable);
        }
        return tmp.toString();
    }
}
