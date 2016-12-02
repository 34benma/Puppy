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

package cn.wantedonline.puppy.util.concurrent;

import java.text.DateFormat;
import java.util.Date;

import cn.wantedonline.puppy.util.DateUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.StringHelper;
import cn.wantedonline.puppy.util.StringTools;
import org.slf4j.Logger;

/**
 * <pre>
 * 此类保存最近一次执行run()的以下信息：
 * 1.开始执行时间
 * 2.执行用时
 * 3.执行出错异常堆栈
 * 
 * 此类适用于
 * 1.定时任务
 * 2.须记录及打印执行时长的业务逻辑
 * </pre>
 * 
 * @author 迅雷 ZengDong
 * @since 2010-9-19 下午04:46:18
 */
public abstract class BaseRunnable implements Runnable {

    /**
     * 最近一次run()的开始执行时间
     */
    protected Date lastBeginTime = null;
    /**
     * 最近一次run()的结束执行时间
     */
    protected Date lastEndTime = null;
    /**
     * 最近一次run()的执行时间
     */
    protected long lastSpan = -1;
    /**
     * 最近一次run()的执行完成后的堆栈信息
     */
    protected Throwable lastThrowable = null;
    protected final Logger log = Log.getLogger(this.getClass().getName());
    /**
     * 日志记录器的名字
     */
    protected String name = log.getName();
    protected boolean logEnable = true;

    public void setLogEnable(boolean logEnable) {
        this.logEnable = logEnable;
    }

    /**
     * 完成处理后记录日志信息
     */
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

    /**
     * 处理前记录日志信息
     */
    public void beforeProcess() {
        if (logEnable) {
            log.debug("BEGIN run {}...", name);
        }
    }

    /**
     * 获得最近一次的开始时间
     * 
     * @return 用字符串表示的最近一次开始时间
     */
    public String getLastBeginTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastBeginTime == null ? "" : df.format(lastBeginTime);
    }

    /**
     * 获得最近一次的结束时间
     * 
     * @return 用字符串表示的最近一次结束时间
     */
    public String getLastEndTimeStr() {
        DateFormat df = DateUtil.DEFAULT_DF_FACOTRY.get();
        return lastEndTime == null ? "" : df.format(lastEndTime);
    }

    /**
     * 获得最近一次执行后的堆栈信息
     * 
     * @return 用字符串表示的堆栈信息
     */
    public String getLastThrowableStr() {
        return lastThrowable == null ? "" : StringHelper.printThrowable(lastThrowable).toString();
    }

    /**
     * 获得扩展信息
     * 
     * @return
     */
    public String getExtendInfo() {
        return null;
    }

    /**
     * 具体run()执行的逻辑，继承BaseRunnable的非抽象类需要重写此方法
     * 
     * @return 执行调试信息
     * @throws Throwable
     */
    public abstract void process() throws Throwable;

    /**
     * run的实现
     */
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

    /**
     * 将BaseRunable的子类对象转化为字符串时调用的方法 返回BaseRunable的属性组成的字符串
     */
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
