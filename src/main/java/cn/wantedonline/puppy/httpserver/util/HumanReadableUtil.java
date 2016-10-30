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

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class HumanReadableUtil {
    private static final double[] BYTE_SIZE = {
            1,
            1024,
            1024*1024,
            1024*1024*1024,
            1024d*1024*1024*1024
    };

    private static final String[] BYTE_SIZE_FORMAT = {
            "B",
            "KB",
            "MB",
            "GB",
            "TB"
    };

    private static final double[] BYTE_SIZE_HD = {
            1,
            1000,
            1000*1000,
            1000*1000*1000,
            1000d*1000*1000*1000
    };

    private static final double[] BYTE_SIZE_THRESHOLD = {
            1,
            999,
            999*1024,
            999*1024*1024,
            999*1024*1024*1024
    };

    private static final String NA = "N/A";

    private static final long[] TIME_SPAN = {
            1,
            1000,
            1000*60,
            1000*60*60,
            1000*60*60*24,
            1000l*60*60*24*365
    };

    private static final String[] TIME_SPAN_FORMAT_CH = {
            "毫秒",
            "秒",
            "分",
            "时",
            "天",
            "年"
    };

    private static final String[] TIME_SPAN_FORMAT_EN = {
            "ms ",
            "sec ",
            "min ",
            "hour ",
            "day ",
            "year "
    };

    public static String byteSize(long size) {
        return byteSize(size, false);
    }

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
        while(--i >= 0) {
            if (bytes > byte_size_threadshold[i]) {
                return df.format((bytes/byte_size[i])) + BYTE_SIZE_FORMAT[i];
            }
        }
        return "";
    }

    public static String timeSpan(long span) {
        return timeSpan(span, 0, false);
    }

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

    public static String percentStr(double d, int iDig, int fDig) {
        NumberFormat num = NumberFormat.getPercentInstance();
        num.setMaximumIntegerDigits(iDig);
        num.setMaximumFractionDigits(fDig);
        return num.format(d);
    }

    public static String percentStrSimple(double d) {
        return percentStr(d, 3, 2);
    }
}
