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

package cn.wantedonline.puppy.httpserver.util.ch.qos.logback.core.rolling;

import ch.qos.logback.core.rolling.RollingFileAppender;

/**
 * 在httpserver进行高并发访问情况下，为了不上日志写文件时因为同步问题而让pipiline线程池堵塞
 */
public class AsyncRollingFileAppender<E> extends RollingFileAppender<E> {

    @Override
    protected void subAppend(final E event) {
        // perform actual sending asynchronously
        // 同SMTPAppenderBase一样，都走后台异步
        // 增加防止爆内存的检查，日志过多会导致IO过高和内存溢出，如果过多就临时关闭日志功能以保护系统
        if (SystemChecker.isLogEnabled()) {
            this.context.getExecutorService().execute(new Runnable() {

                @Override
                public void run() {
                    AsyncRollingFileAppender.super.subAppend(event);
                }
            });
        }
    }
}
