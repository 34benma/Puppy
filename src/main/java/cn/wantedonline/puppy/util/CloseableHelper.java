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

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/10/30
 */
public class CloseableHelper {

    private static final Map<Class<?>, Method> closeMethodCache = new HashMap<Class<?>, Method>(2);
    private static final Logger log = Log.getLogger();

    /**
     * 关闭对象，关闭前会判断是否为null，可能抛出异常
     * 
     * @param c
     * @throws IOException
     */
    public static void close(Closeable c) throws IOException {
        if (c != null) {
            c.close();
        }
    }

    /**
     * 关闭对象，关闭前会判断是否为null，可能抛出异常
     * 
     * @param c
     * @throws Exception
     */
    public static void close(Object c) throws Exception {
        if (c != null) {
            Class<?> clazz = c.getClass();
            Method cachedMehtod = closeMethodCache.get(clazz);
            if (cachedMehtod == null) {
                Method m = clazz.getMethod("close");// close方法必须是 public的,因此这里使用getMethod,而非getDeclaredMethod
                m.invoke(c);
                closeMethodCache.put(clazz, m);
            } else {
                cachedMehtod.invoke(c);
            }
        }
    }

    /**
     * 关闭对象，关闭前会判断是否为null，可能抛出异常
     * 
     * <pre>
     * 注意：
     * Closing this socket will also close the socket's {@link java.io.InputStream InputStream} and {@link java.io.OutputStream OutputStream}.
     * If this socket has an associated channel then the channel is closed as well.
     * @param c
     * @throws IOException
     */
    public static void close(Socket c) throws IOException {
        if (c != null) {
            c.close();
        }
    }

    /**
     * 关闭对象，方法内是使用了catch，不会抛出异常
     * 
     * @param c
     */
    public static void closeSilently(Closeable c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    /**
     * 关闭对象，方法内是使用了catch，不会抛出异常
     * 
     * @param c
     */
    public static void closeSilently(Object c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    /**
     * 关闭对象，方法内是使用了catch，不会抛出异常
     * 
     * @param c
     */
    public static void closeSilently(Socket c) {
        try {
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Closeable c) {
        try {
            log.info("close Closeable:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Object c) {
        try {
            log.info("close Object:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    public static void closeAndLog(Socket c) {
        try {
            log.info("close Socket:{}", c);
            close(c);
        } catch (Throwable e) {
            log.info("", e);
        }
    }

    private CloseableHelper() {
    }
}
