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

import cn.wantedonline.puppy.httpserver.util.codec.Hex;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Created by wangcheng on 2016/11/1.
 */
public class StringHelper {
    public interface KeyValueStringBuilder {
        public void append(StringBuilder tmp, Object key, Object value);
    }

    private static Method getOurStackTrace;

    static {
        try {
            getOurStackTrace = Throwable.class.getDeclaredMethod("getOurStackTrace");
            getOurStackTrace.setAccessible(true);
        } catch (Exception e) {}
    }

    public static StringBuilder append(StringBuilder tmp, Object... args) {
        for (Object s : args) {
            tmp.append(s);
        }
        return tmp;
    }

    public static String concate(Object... args) {
        if (args.length < 4) {
            String result = "";
            for (Object s : args) {
                result += s;
            }
            return result;
        }
        return append(new StringBuilder(), args).toString();
    }

    public static <K, V> String concateKeyValue(KeyValueStringBuilder keyValueStringBuilder, Map<K, V> keyvalue) {
        return concateKeyValue(new StringBuilder(), keyValueStringBuilder, keyvalue).toString();
    }

    public static String concateKeyValue(KeyValueStringBuilder keyValueStringBuilder, Object... keyvalue) {
        return concateKeyValue(new StringBuilder(), keyValueStringBuilder, keyvalue).toString();
    }

    public static <K, V> StringBuilder concateKeyValue(StringBuilder tmp, KeyValueStringBuilder keyValueStringBuilder, Map<K, V> keyvalue) {
        for (Map.Entry<K, V> entry : keyvalue.entrySet()) {
            keyValueStringBuilder.append(tmp, entry.getKey(), entry.getValue());
        }
        return tmp;
    }

    public static StringBuilder concateKeyValue(StringBuilder tmp, KeyValueStringBuilder keyValueStringBuilder, Object... keyvalue) {
        MapUtil.checkKeyValueLength(keyvalue);
        for (int i = 0; i < keyvalue.length; i++) {
            keyValueStringBuilder.append(tmp, keyvalue[i++], keyvalue[i]);
        }
        return tmp;
    }

    public static String concateWithSplit(String splitStr, Object... args) {
        return concateWithSplit(new StringBuilder(), splitStr, args).toString();
    }

    public static StringBuilder concateWithSplit(StringBuilder tmp, String splitStr, Object... args) {
        if (args.length == 0) {
            return tmp;
        }
        int endIndex = args.length - 1;
        for (int i = 0; i < endIndex; i++) {
            tmp.append(args[i]).append(splitStr);
        }
        tmp.append(args[endIndex]);
        return tmp;
    }

    public static StringBuilder emphasizeTitle(String title, char corner, char linechar, char verticalchar) {
        return emphasizeTitle(new StringBuilder(), title, corner, linechar, verticalchar);
    }

    public static StringBuilder emphasizeTitle(StringBuilder tmp, String title, char corner, char linechar, char verticalchar) {
        StringBuilder line;
        try {
            line = printLine(title.getBytes("GBK").length, corner, linechar);
            tmp.append(line);
            tmp.append(verticalchar).append(title).append(verticalchar).append('\n');
            tmp.append(line);
        } catch (UnsupportedEncodingException e) {
        }
        return tmp;
    }

    private static StackTraceElement[] getOurStackTrace(Throwable ex) {
        try {
            StackTraceElement[] ste = (StackTraceElement[]) getOurStackTrace.invoke(ex);
            return ste;
        } catch (Exception e) {
            return null;
        }
    }

    public static StringBuilder printLine(int len, char linechar) {
        return printLine(new StringBuilder(), len, linechar);
    }

    public static StringBuilder printLine(int len, char corner, char linechar) {
        return printLine(new StringBuilder(), len, corner, linechar);
    }

    public static StringBuilder printLine(StringBuilder tmp, int len, char linechar) {
        for (int i = 0; i < len; i++) {
            tmp.append(linechar);
        }
        tmp.append('\n');
        return tmp;
    }

    public static StringBuilder printLine(StringBuilder tmp, int len, char corner, char linechar) {
        tmp.append(corner);
        for (int i = 0; i < len; i++) {
            tmp.append(linechar);
        }
        tmp.append(corner);
        tmp.append('\n');
        return tmp;
    }

    public static StringBuilder printThrowable(StringBuilder tmp, Throwable ex) {
        if (null != ex) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            ex.printStackTrace(pw);
            tmp.append(stringWriter).append('\n');
        }
        return tmp;
    }

    public static StringBuilder printThrowable(Throwable ex) {
        return printThrowable(new StringBuilder(), ex);
    }

    public static String printThrowableSimple(Throwable ex) {
        return printThrowableSimple(ex, 8);
    }

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
     * 以16进制 打印字节数组
     *
     * @param bytes
     * @return
     */
    public static String printHexString(byte[] bytes) {
        if (bytes == null) {
            return "";
        }
        StringBuilder buffer = new StringBuilder(bytes.length);
        int startIndex = 0;
        int column = 0;
        for (int i = 0; i < bytes.length; i++) {
            column = i % 16;
            switch (column) {
                case 0:
                    startIndex = i;
                    fixHexString(buffer, Integer.toHexString(i), 8).append(": ");
                    buffer.append(toHex(bytes[i]));
                    buffer.append(" ");
                    break;
                case 15:
                    buffer.append(toHex(bytes[i]));
                    buffer.append(" ");
                    buffer.append(filterString(bytes, startIndex, column + 1));
                    buffer.append("\n");
                    break;
                default:
                    buffer.append(toHex(bytes[i]));
                    buffer.append(" ");
            }
        }
        if (column != 15) {
            for (int i = 0; i < (15 - column); i++) {
                buffer.append("   ");
            }
            buffer.append(filterString(bytes, startIndex, column + 1));
            buffer.append("\n");
        }

        return buffer.toString();
    }

    /**
     * 将hexStr格式化成length长度16进制数，并在后边加上h
     *
     * @param hexStr String
     * @return StringBuilder
     */
    private static StringBuilder fixHexString(StringBuilder buf, String hexStr, int length) {
        if (hexStr == null || hexStr.length() == 0) {
            buf.append("00000000h");
        } else {
            int strLen = hexStr.length();
            for (int i = 0; i < length - strLen; i++) {
                buf.append("0");
            }
            buf.append(hexStr).append("h");
        }
        return buf;
    }

    /**
     * 将字节转换成16进制显示
     *
     * @param b byte
     * @return String
     */
    private static String toHex(byte b) {
        char[] buf = new char[2];
        byte bt = b;
        for (int i = 0; i < 2; i++) {
            buf[1 - i] = Hex.DIGITS_LOWER[bt & 0xF];
            bt = (byte) (bt >>> 4);
        }
        return new String(buf);
    }

    /**
     * 过滤掉字节数组中0x0 - 0x1F的控制字符，生成字符串
     *
     * @param bytes byte[]
     * @param offset int
     * @param count int
     * @return String
     */
    private static String filterString(byte[] bytes, int offset, int count) {
        byte[] buffer = new byte[count];
        System.arraycopy(bytes, offset, buffer, 0, count);
        for (int i = 0; i < count; i++) {
            if (buffer[i] >= 0x0 && buffer[i] <= 0x1F) {
                buffer[i] = 0x2e;
            }
        }
        return new String(buffer);
    }

    public static String digestString(String src) {
        return digestString(src, 50);
    }

    public static String digestString(String src, int lengthThreshold) {
        if (src.length() > lengthThreshold * 2 + 20) {
            return src.substring(0, lengthThreshold) + "...(" + src.length() + ")..." + src.substring(src.length() - lengthThreshold, src.length());
        }
        return src;
    }

    private StringHelper() {
    }
}
