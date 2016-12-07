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

package cn.wantedonline.puppy.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;

/**
 *
 * @author 迅雷 zengdong
 * @author wangcheng
 * @since V0.2.0 on 16/12/2.
 */
public class StringHelper {
    private static Method getOurStackTrace;

    static {
        try {
            getOurStackTrace = Throwable.class.getDeclaredMethod("getOurStackTrace");
            getOurStackTrace.setAccessible(true);
        } catch (Exception e) {
        }
    }
    /**
     * 获得堆栈的所有元素
     *
     * @param ex
     * @return
     */
    private static StackTraceElement[] getOurStackTrace(Throwable ex) {
        try {
            StackTraceElement[] ste = (StackTraceElement[]) getOurStackTrace.invoke(ex);
            return ste;
        } catch (Exception e) {
            return null;
        }
    }
    /**
     * 简单打印Throwable信息，最多8个
     *
     * @param ex
     * @return
     */
    public static String printThrowableSimple(Throwable ex) {
        return printThrowableSimple(ex, 8);
    }

    /**
     * 精简打印一个throwable信息,不换行，由maxTraceLen指定信息数量
     *
     * @param ex
     * @param maxTraceLen 要打印堆栈信息的数量
     * @return
     */
    public static String printThrowableSimple(Throwable ex, int maxTraceLen) {
        if (null != ex) {
            StringBuilder s = new StringBuilder();
            s.append(ex.getClass().getSimpleName());// 这里不打印全称
            s.append(":");
            s.append(ex.getMessage());
            if (maxTraceLen > 0) {
                // TODO:这里并没有打印CauseThrowable相关的信息
                StackTraceElement[] trace = getOurStackTrace(ex);
                if (trace != null) {
                    int len = Math.min(trace.length, maxTraceLen);
                    for (int i = 0; i < len; i++) {
                        try {
                            StackTraceElement t = trace[i];
                            String clazzName = t.getClassName();
                            clazzName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
                            s.append("||");
                            s.append(clazzName);
                            s.append(".");
                            s.append(t.getMethodName());
                            s.append(":");
                            s.append(t.getLineNumber());
                        } catch (Exception e) {
                        }
                    }
                }
            }
            return s.toString();
        }
        return "";
    }

    /**
     * 记录调用栈，直到遇到非StringHelper的成员为止
     */
    public static StringBuilder printStackTraceSimple() {
        StringBuilder sb = new StringBuilder();
        StackTraceElement[] ste = Thread.currentThread().getStackTrace();
        if (ste != null) {
            for (int i = 0; i < ste.length; i++) {
                try {
                    StackTraceElement t = ste[i];
                    String clazzName = t.getClassName();
                    if (clazzName.equals(StringHelper.class.getName()) || (clazzName.equals(Thread.class.getName())) && t.getMethodName().equals("getStackTrace")) {
                        continue;
                    }
                    clazzName = clazzName.substring(clazzName.lastIndexOf(".") + 1, clazzName.length());
                    sb.append("||");
                    sb.append(clazzName);
                    sb.append(".");
                    sb.append(t.getMethodName());
                    sb.append(":");
                    sb.append(t.getLineNumber());
                } catch (Exception e) {
                }
            }
        }
        return sb;
    }

    /**
     * 打印 堆栈异常
     *
     * @param tmp
     * @param ex
     * @return
     */
    public static StringBuilder printThrowable(StringBuilder tmp, Throwable ex) {
        if (AssertUtil.isNotNull(ex)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            ex.printStackTrace(pw);
            tmp.append(stringWriter).append('\n');
        }
        return tmp;
    }

    /**
     * 打印 堆栈异常
     */
    public static StringBuilder printThrowable(Throwable ex) {
        return printThrowable(new StringBuilder(), ex);
    }

    private StringHelper() {
    }
}
