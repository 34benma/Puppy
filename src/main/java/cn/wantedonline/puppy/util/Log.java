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

package cn.wantedonline.puppy.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <pre>
 *     日志工具类
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/27.
 */
public class Log {
    private Log() {}

    public static Logger getLogger() {
        return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
    }

    /**
     * 获得代表obj的类的Logger,使用getLogger(this)来获得对应子类的logger
     *
     * @param obj 指定的要获得其类的对象
     * @return
     */
    public static Logger getLogger(Object obj) {
        if (obj instanceof Class<?>) {
            return LoggerFactory.getLogger((Class<?>) obj);
        }
        return LoggerFactory.getLogger(obj.getClass().getName());
    }

    /**
     * 直接指定logger名称
     *
     * @param str
     * @return
     */
    public static Logger getLogger(String str) {
        return LoggerFactory.getLogger(str);
    }

    /**
     * 通过指定对象、前缀和后缀来获得Logger
     *
     * @param obj 对象
     * @param prefix 前缀
     * @param suffix 后缀
     * @return
     */
    public static Logger getLoggerWith(Object obj, String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + obj.getClass().getName() + "." + suffix);
    }

    public static Logger getLoggerWith(String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + Thread.currentThread().getStackTrace()[2].getClassName() + "." + suffix);
    }

    /**
     * 通过对象和前缀来获得Logger
     *
     * @param obj 对象
     * @param prefix 前缀
     * @return
     */
    public static Logger getLoggerWithPrefix(Object obj, String prefix) {
        return LoggerFactory.getLogger(prefix + "." + obj.getClass().getName());
    }

    public static Logger getLoggerWithPrefix(String prefix) {
        return LoggerFactory.getLogger(prefix + "." + Thread.currentThread().getStackTrace()[2].getClassName());
    }

    /**
     * 通过对象和后缀来获得Logger
     *
     * @param obj 对象
     * @param suffix 后缀
     * @return
     */
    public static Logger getLoggerWithSuffix(Object obj, String suffix) {
        return LoggerFactory.getLogger(obj.getClass().getName() + "." + suffix);
    }

    public static Logger getLoggerWithSuffix(String suffix) {
        return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName() + "." + suffix);
    }
}
