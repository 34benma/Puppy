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

import static cn.wantedonline.puppy.util.DateUtilHelper.ADD;
import static cn.wantedonline.puppy.util.DateUtilHelper.CEIL;
import static cn.wantedonline.puppy.util.DateUtilHelper.ROLL;
import static cn.wantedonline.puppy.util.DateUtilHelper.ROUND;
import static cn.wantedonline.puppy.util.DateUtilHelper.SET;
import static cn.wantedonline.puppy.util.DateUtilHelper.TRUNCATE;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by louiswang on 16/10/28.
 */
public class DateUtil {
    /*
     * >>>>>>常用DateFormat格式
     */
    public static final String DF_yyyyMMddHHmmss = "yyyyMMddHHmmss";
    public static final String DF_yyyyMMddHHmm = "yyyyMMddHHmm";
    public static final String DF_yyMMddHHmmss = "yyMMddHHmmss";
    public static final String DF_yyyyMMdd = "yyyyMMdd";
    public static final String DF_yyyydotMMdotdd = "yyyy.MM.dd";
    public static final String DF_yyyyMM = "yyyyMM";
    public static final String DF_yyMMdd = "yyMMdd";
    public static final String DF_yyyy_MM_dd_HHmmss = "yyyy-MM-dd HH:mm:ss";
    public static final String DF_yy_MM_dd_HHmmss = "yy-MM-dd HH:mm:ss";
    public static final String DF_yyyy_MM_dd = "yyyy-MM-dd";
    public static final String DF_yy_MM_dd = "yy-MM-dd";
    public static final String DF_DEFAULT = DF_yyyy_MM_dd_HHmmss;
    public static final String DF_DEFAULT_DAY = DF_yyyy_MM_dd;
    public static final String DF_DEFAULT_GMT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    /*
     * >>>>>>非线程安全的DateFormat,一般只用于当前时间的format/parse
     */
    public static final DateFormat UNSAFE_DF_yyyyMMddHHmmss = new SimpleDateFormat(DF_yyyyMMddHHmmss);
    public static final DateFormat UNSAFE_DF_yyMMddHHmmss = new SimpleDateFormat(DF_yyMMddHHmmss);
    public static final DateFormat UNSAFE_DF_yyyyMMdd = new SimpleDateFormat(DF_yyyyMMdd);
    public static final DateFormat UNSAFE_DF_yyyyMM = new SimpleDateFormat(DF_yyyyMM);
    public static final DateFormat UNSAFE_DF_yyMMdd = new SimpleDateFormat(DF_yyMMdd);
    public static final DateFormat UNSAFE_DF_yyyy_MM_dd_HHmmss = new SimpleDateFormat(DF_yyyy_MM_dd_HHmmss);
    public static final DateFormat UNSAFE_DF_yy_MM_dd_HHmmss = new SimpleDateFormat(DF_yy_MM_dd_HHmmss);
    public static final DateFormat UNSAFE_DF_yyyy_MM_dd = new SimpleDateFormat(DF_yyyy_MM_dd);
    public static final DateFormat UNSAFE_DF_yy_MM_dd = new SimpleDateFormat(DF_yy_MM_dd);
    public static final DateFormat UNSAFE_DF_DEFAULT = UNSAFE_DF_yyyy_MM_dd_HHmmss;
    public static final DateFormat UNSAFE_DF_DEFAULT_DAY = UNSAFE_DF_yyyy_MM_dd;
    /*
     * >>>>>>线程安全的DateFormat 的 ThreadLocal类
     */
    public static final ThreadLocal<DateFormat> DEFAULT_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT);
    public static final ThreadLocal<DateFormat> DEFAULT_DAY_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT_DAY);
    public static final ThreadLocal<DateFormat> GMT_DF_FACOTRY = makeDateFormatPerThread(DF_DEFAULT_GMT, Locale.US, true, TimeZone.getTimeZone("GMT"));
    private static final int DEFAULT_COMPARE_YEAR = 1986;// 为了对比自然月/自然年,要把两个要对比的Year先置成统一,来判断是否要取整

    /**
     * 获得线程安全DateFormat 工厂类:ThreadLocal
     *
     * @param pattern
     * @param locale
     * @param lenient
     * @param zone
     * @return
     */
    public static ThreadLocal<DateFormat> makeDateFormatPerThread(final String pattern, final Locale locale, final boolean lenient, final TimeZone zone) {
        return new ThreadLocal<DateFormat>() {

            @Override
            protected synchronized DateFormat initialValue() {
                try {
                    DateFormat df = locale == null ? new SimpleDateFormat(pattern) : new SimpleDateFormat(pattern, locale);
                    df.setLenient(lenient);
                    if (zone != null) {
                        df.setTimeZone(zone);
                    }
                    return df;
                } catch (Exception e) {
                    return null;
                }
            }
        };
    }

    /**
     * 获得线程安全DateFormat 工厂类:ThreadLocal
     */
    public static ThreadLocal<DateFormat> makeDateFormatPerThread(final String pattern) {
        return makeDateFormatPerThread(pattern, null, true, null);
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上设定为一个特定值
     *
     * @param calendar Calendar类型表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要设置的值
     * @return
     */
    public static Calendar set(Calendar calendar, int calendarField, int amount) {
        calendar.set(calendarField, amount);
        return calendar;
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上设定为一个特定值
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要设置的值
     * @return
     */
    public static Date set(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上设定为一个特定值
     *
     * @param timeMillis 用毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount
     * @return
     */
    public static long set(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.set(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上增加一个数量
     *
     * @param calendar Calendar类型是时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要增加的数量
     * @return Calendar类型的时间
     */
    public static Calendar add(Calendar calendar, int calendarField, int amount) {
        calendar.add(calendarField, amount);
        return calendar;
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上增加一个数量
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要增加的量
     * @return Date类型的时间
     */
    public static Date add(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.add(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上增加一个数量
     *
     * @param timeMillis 毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要增加的量
     * @return 毫秒数表示的时间
     */
    public static long add(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.add(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上滚动一个数量
     *
     * @param calendar Calendar表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要滚动的数量
     * @return Calendar类型的滚动后的时间
     */
    public static Calendar roll(Calendar calendar, int calendarField, int amount) {
        calendar.roll(calendarField, amount);
        return calendar;
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上滚动一个数量
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要滚动的量
     * @return Date类型的滚动后的时间
     */
    public static Date roll(Date date, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.roll(calendarField, amount);
        return calendar.getTime();
    }

    /**
     * 将某时间的时间点（年、月、日、时、分、秒）上滚动一个数量
     *
     * @param timeMillis 毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 要滚动的数量
     * @return 毫秒数表示的时间
     */
    public static long roll(long timeMillis, int calendarField, int amount) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        calendar.roll(calendarField, amount);
        return calendar.getTimeInMillis();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向上取整
     *
     * @param calendar Calendar类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用Calendar类型的时间
     */
    public static Calendar ceil(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar;
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向上取整
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的Date类型的时间
     */
    public static Date ceil(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar.getTime();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向上取整
     *
     * @param timeMillis 毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用毫秒数表示的时间
     */
    public static long ceil(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, CEIL);
        return calendar.getTimeInMillis();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）四舍五入
     *
     * @param calendar Calendar类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的Calendar类型的时间
     */
    public static Calendar round(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar;
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）四舍五入
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的Date类型的时间
     */
    public static Date round(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar.getTime();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）四舍五入
     *
     * @param timeMillis 毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用毫秒数表示的时间
     */
    public static long round(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, ROUND);
        return calendar.getTimeInMillis();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向下取整
     *
     * @param calendar 用Calendar表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用毫秒数表示的时间
     */
    public static Calendar truncate(Calendar calendar, int calendarField) {
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar;
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向下取整
     *
     * @param date Date类型的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用Date类型表示的时间
     */
    public static Date truncate(Date date, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar.getTime();
    }

    /**
     * 将时间的时间点（年、月、日、时、分、秒）向下取整
     *
     * @param timeMillis 用毫秒数表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @return 修改后的用毫秒数表示的时间
     */
    public static long truncate(long timeMillis, int calendarField) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        DateUtils.modify(calendar, calendarField, TRUNCATE);
        return calendar.getTimeInMillis();
    }

    /**
     * 通用日期设置功能:oper 须辅助使用DateUtilHelper来使用
     *
     * <pre>
     * 组合 org.apache.commons.lang.time.DateUtils里面的功能:
     *  ceil(向上取整),
     *  round(四舍五入),
     *  truncate(向下取整)
     * 及Calendar默认的:
     *  set
     *  add
     *  roll
     *
     * 六大操作,提供处理Calendar的多次修改快捷方法,如
     * Calendar c = ...;// 内容为2010-10-25 12:00:02
     * Calendar r = oper(c,
     *                 DateUtilHelper.add(Calendar.DAY_OF_MONTH, 1),
     *                 DateUtilHelper.set(Calendar.YEAR, 1986),
     *                 DateUtilHelper.truncate(Calendar.MINUTE))
     *
     * 则r的结果是：1986-10-26 12:00:00
     * </pre>
     *
     * @param calendar Calendar类型的时间
     * @param opers 若干个操作
     * @return 修改后的用Calendar类型表示的时间
     */
    public static Calendar oper(Calendar calendar, long... opers) {
        for (long operMagic : opers) {
            int amount = DateUtilHelper.getAmount(operMagic);
            int operType = DateUtilHelper.getOperType(operMagic);
            int calendarField = DateUtilHelper.getCalendarField(operMagic);
            switch (operType) {
                case SET:
                    calendar.set(calendarField, amount);
                    break;
                case ADD:
                    calendar.add(calendarField, amount);
                    break;
                case ROLL:
                    calendar.roll(calendarField, amount);
                    break;
                case CEIL:
                    DateUtils.modify(calendar, calendarField, operType);
                    break;
                case ROUND:
                    DateUtils.modify(calendar, calendarField, operType);
                    break;
                case TRUNCATE:
                    DateUtils.modify(calendar, calendarField, operType);
                    break;
            }
        }
        return calendar;
    }

    /**
     * 通用日期设置功能:oper 须辅助使用DateUtilHelper来使用
     *
     * @param date Date类型的时间
     * @param opers 若干个操作
     * @return 修改后的用Date类型表示的时间
     */
    public static Date oper(Date date, long... opers) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        Calendar r = oper(calendar, opers);
        return r.getTime();
    }

    /**
     * 通用日期设置功能:oper 须辅助使用DateUtilHelper来使用
     *
     * @param timeMillis 用毫秒数表示的时间
     * @param opers 若干个操作
     * @return 修改后的用毫秒数表示的时间
     */
    public static long oper(long timeMillis, long... opers) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeMillis);
        Calendar r = oper(calendar, opers);
        return r.getTimeInMillis();
    }

    /**
     * 计算两时间对应时长单位(TimeUnit)的时差
     *
     * @param date1
     * @param date2
     * @param timeUnit 时间单位
     * @param ceil 是否向上取整
     * @return date1-date2（以timeUnit为单位的结果）
     */
    public static long getInterval(Date date1, Date date2, UnitConverter.TimeUnit timeUnit, boolean ceil) {
        if (timeUnit == UnitConverter.TimeUnit.month) { // 特殊处理自然月
            return getMonthsBetween(date1, date2, ceil);
        }
        if (timeUnit == UnitConverter.TimeUnit.year) { // 特殊处理自然年
            return getYearsBetween(date1, date2, ceil);
        }
        return (long) UnitConverter.convertTime(date1.getTime() - date2.getTime(), UnitConverter.TimeUnit.millisecond, timeUnit, ceil);
    }

    public static long getInterval(Date date1, Date date2, UnitConverter.TimeUnit timeUnit) {
        return getInterval(date1, date2, timeUnit, false);
    }

    /**
     * <pre>
     ***********
     ** private*
     ***********
     * <pre>
     */
    /**
     * 求两时间相减的年数差值(自然年),date1-date2
     *
     * @param date1
     * @param date2
     * @param ceil true表示 向上取整
     * @return date1-date2
     */
    private static long getYearsBetween(Date date1, Date date2, boolean ceil) {
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        int year1 = c.get(Calendar.YEAR);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time1 = c.getTimeInMillis();

        c.setTime(date2);
        int year2 = c.get(Calendar.YEAR);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time2 = c.getTimeInMillis();

        long result = year1 - year2;
        return ceil(result, time1, time2, ceil);
    }

    /**
     * 求两时间相减的月数差值(自然月),date1-date2
     *
     * @param date1
     * @param date2
     * @param ceil true表示 向上取整
     * @return date1-date2
     */
    private static long getMonthsBetween(Date date1, Date date2, boolean ceil) {
        Calendar c = Calendar.getInstance();
        c.setTime(date1);
        int month1 = c.get(Calendar.MONTH);
        int year1 = c.get(Calendar.YEAR);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time1 = c.getTimeInMillis();

        c.setTime(date2);
        int month2 = c.get(Calendar.MONTH);
        int year2 = c.get(Calendar.YEAR);
        c.set(Calendar.MONTH, 0);
        c.set(Calendar.YEAR, DEFAULT_COMPARE_YEAR);
        long time2 = c.getTimeInMillis();
        // /////////////////////////////////////////////////////////////////////////////////////////////////////////////////////>>>>>>>>>>>>>
        long result = (year1 - year2) * 12l + (month1 - month2);
        return ceil(result, time1, time2, ceil);
    }

    /**
     * 计算time1和time2之间的差值对result的影响，根据是否向上取整，影响的计算方式不同
     *
     * @param result 被影响的数
     * @param time1 第一个时间
     * @param time2 第二个时间
     * @param ceil 是否向上取整
     * @return 修改后的数
     */
    private static long ceil(long result, long time1, long time2, boolean ceil) {
        long ret = result;
        if (ceil) {
            // 需要向上取整
            if (ret == 0) {
                long diff = time1 - time2;
                if (diff > 0) {
                    ret = 1; // 1
                } else if (diff < 0) {
                    ret = -1; // -1
                }
            }
        } else {
            // 需要向下取整,默认
            if (ret != 0) {
                long diff = time1 - time2;
                if (diff != 0) {
                    if (ret > 0 && diff < 0) {
                        ret -= 1;
                    } else if (ret < 0 && diff > 0) {
                        ret += 1;
                    }
                }
            }
        }
        return ret;
    }
}
