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

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * 提供将时间或者硬盘空间大小转化成人们方便阅读格式的方法
 * 
 * @author 迅雷 ZengDong
 * @since V0.1.0
 */
public class HumanReadableUtil {

    /**
     * 硬件厂商的标准中每个字节单位含有的字节数
     */
    private static final double[] BYTE_SIZE = {
        1,
        1024,
        1024 * 1024,
        1024 * 1024 * 1024,
        1024d * 1024 * 1024 * 1024
    };
    /**
     * 字节单位表示
     */
    private static final String[] BYTE_SIZE_FORMAT = {
        "B",
        "KB",
        "MB",
        "GB",
        "TB"
    };
    /**
     * 操作系统中每个单位含有的字节数
     */
    private static final double[] BYTE_SIZE_HD = {
        1,
        1000,
        1000 * 1000,
        1000 * 1000 * 1000,
        1000d * 1000 * 1000 * 1000
    };
    /**
     * 
     */
    private static final double[] BYTE_SIZE_THRESHOLD = {
        1,
        999,
        999 * 1024,
        999 * 1024 * 1024,
        999d * 1024 * 1024 * 1024
    };
    /**
     * 
     */
    private static final String NA = "N/A";
    /**
     * 每种时间单位对应的毫秒数
     */
    private static final long[] TIME_SPAN = {
        1,
        1000,
        1000 * 60,
        1000 * 60 * 60,
        1000 * 60 * 60 * 24,
        1000l * 60 * 60 * 24 * 365
    };
    /**
     * 时间单位(中文)
     */
    private static final String[] TIME_SPAN_FORMAT_CH = {
        "毫秒",
        "秒",
        "分",
        "时",
        "天",
        "年"
    };
    /**
     * 时间单位(英文)
     */
    private static final String[] TIME_SPAN_FORMAT_EN = {
        "ms ",
        "sec ",
        "min ",
        "hour ",
        "day ",
        "year "
    };

    /**
     * 以硬件厂商的标准将文件大小转化成标准格式
     * 
     * @param length 文件字节长度
     * @return
     */
    public static String byteSize(long size) {
        return byteSize(size, false);
    }

    /**
     * 用GB,MB,KB,B动态显示文件(或硬盘)大小 保留两位数
     * 
     * @param bytes 字节大小
     * @param byHDStandard 硬盘厂商的标准：40GB=40,000MB=40,000,000KB=40,000,000,000byte； 操作系统的算法：40GB=40,960MB=41,943,040KB=42,949,672,960byte； 为true时，表明按照硬盘厂商的标准显示
     * @return String 文件(或硬盘)大小显示
     */
    public static String byteSize(long bytes, boolean byHDStandard) {
        if (bytes < 0) {
            return NA;
        }
        double[] byte_size = BYTE_SIZE;
        double[] byte_size_threadshold = BYTE_SIZE_THRESHOLD;
        if (byHDStandard) {
            byte_size = BYTE_SIZE_HD;
            byte_size_threadshold = BYTE_SIZE_HD;
        }
        int i = 5;
        DecimalFormat df = new DecimalFormat("##.##");
        while (--i >= 0) {
            if (bytes >= byte_size_threadshold[i]) {
                return df.format((bytes / byte_size[i])) + BYTE_SIZE_FORMAT[i];
            }
        }
        return "";
    }

    /**
     * 将用毫秒表示的时间转换成用年月日表示的时间
     * 
     * @param span 时间的毫秒数表示
     * @return 时间的年月日的格式
     */
    public static String timeSpan(long span) {
        return timeSpan(span, 0, false);
    }

    /**
     * 将毫秒格式的时间转化为**年**月**日**时**分**秒**毫秒的格式
     * 
     * @param span 时间的毫秒数表示
     * @param max_len 最多显示的时间单位个数
     * @param chinese 是否为中文
     * @return
     */
    public static String timeSpan(long span, int max_len, boolean chinese) {
        long sp = span;
        int maxlen = max_len;
        if (sp < 0) {
            return NA;
        }
        String[] format = chinese ? TIME_SPAN_FORMAT_CH : TIME_SPAN_FORMAT_EN;
        if (maxlen <= 0) {
            maxlen = 3;
        }
        long tmp = 0;
        int index = 6;
        StringBuilder sb = new StringBuilder("");
        while (--index >= 0) {
            if ((tmp = sp / TIME_SPAN[index]) > 0) {
                sp = sp % TIME_SPAN[index];
                sb.append(tmp);
                sb.append(format[index]);
                if (--maxlen <= 0) {
                    break;
                }
            }
        }
        return sb.toString();
    }

    /**
     * @param 小数
     * @return 百分比格式的字符串
     */
    public static String percentStr(double d, int iDig, int fDig) {
        NumberFormat num = NumberFormat.getPercentInstance();
        num.setMaximumIntegerDigits(iDig); // 小数点前面最多显示几位的
        num.setMaximumFractionDigits(fDig); // 小数点后面最多显示几位
        return num.format(d);
    }

    /**
     * @param 小数
     * @return 百分比格式的字符串
     */
    public static String percentStrSimple(double d) {
        return percentStr(d, 3, 2);
    }
}
