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
 * Created by wangcheng on 2016/10/27.
 */
public class Log {
    public static Logger getLogger() {
        return LoggerFactory.getLogger(Thread.currentThread().getStackTrace()[2].getClassName());
    }

    public static Logger getLogger(Object obj) {
        if (obj instanceof Class<?>) {
            return LoggerFactory.getLogger((Class<?>)obj);
        }

        return LoggerFactory.getLogger(obj.getClass().getName());
    }

    public static Logger getLogger(String name) {
        return LoggerFactory.getLogger(name);
    }

    public static Logger getLoggerWith(Object obj, String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + obj.getClass().getName() + "." + suffix);
    }

    public static Logger getLoggerWith(String prefix, String suffix) {
        return LoggerFactory.getLogger(prefix + "." + Thread.currentThread().getStackTrace()[2].getClassName() + "." + suffix);
    }

}
