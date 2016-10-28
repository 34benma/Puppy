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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

import cn.wantedonline.puppy.util.UnitConverter.TimeUnit;
import org.slf4j.Logger;

/**
 * Created by louiswang on 16/10/28.
 */
public class DateStringUtil {
    private static final int DEFAULT_GETBETWEEN_ERROR_RESULT = Integer.MAX_VALUE;
    private static final Logger log = Log.getLogger();
    private static final ConcurrentHashMap<String, DateStringUtil> allDateStringUtil = new ConcurrentHashMap<String, DateStringUtil>(2);

    /** yyyy-MM-dd HH:mm:ss格式的DateStringUtil */
    private static final DateStringUtil DEFAULT_DATE_STRING_UTIL = getInstance(DateUtil.DF_DEFAULT);

    /** yyyy-MM-dd HH:mm:ss格式的DateStringUtil */
    public static final DateStringUtil DEFAULT = DEFAULT_DATE_STRING_UTIL;

    /** yyyy-MM-dd格式的DateStringUtil */
    private static final DateStringUtil DEFAULT_DATE_STRING_UTIL_DAY = getInstance(DateUtil.DF_DEFAULT_DAY);

    /** yyyy-MM-dd格式的DateStringUtil */
    public static final DateStringUtil DEFAULT_DAY = DEFAULT_DATE_STRING_UTIL_DAY;

    /** yyyy.MM.dd格式的DateStringUtil */
    private static final DateStringUtil DOT_DATE_STRING_UTIL = getInstance(DateUtil.DF_yyyydotMMdotdd);

    /** yyyy.MM.dd格式的DateStringUtil */
    public static final DateStringUtil DOT_DAY = DOT_DATE_STRING_UTIL;

    /**
     * 在date的某个时间点（年、月、日、时、分、秒）加上一个特定的值
     *
     * @param date 要更改的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 增加的量
     * @return
     */
    @SuppressWarnings("deprecation")
    public String add(Date date, int calendarField, int amount) {
        if (amount == 0) {
            return getDateFormat().format(date);
        }
        return getDateFormat().format(DateUtils.add(date, calendarField, amount));
    }

    /**
     * 在date的某个特定点（年、月、日、时、分、秒）加上一个特定的值
     *
     * @param date 用字符串表示的时间
     * @param calendarField 时间点（年、月、日、时、分、秒）
     * @param amount 增加的量
     * @return
     */
    @SuppressWarnings("deprecation")
    public String add(String date, int calendarField, int amount) {
        try {
            if (amount == 0) {
                return date;
            }
            DateFormat df = getDateFormat();
            return df.format(DateUtils.add(df.parse(date), calendarField, amount));
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return date;
        }
    }

    /**
     * 在今天基础上增加年数后的新时间字符串
     *
     * @param year 要增加的年数
     * @return
     */
    public String addYears(int year) {
        return addYears(new Date(), year);
    }

    /**
     * 在指定的时间date基础上增加年数后的新时间字符串
     *
     * @param date
     * @param year
     * @return
     */
    public String addYears(Date date, int year) {
        return add(date, Calendar.YEAR, year);
    }

    /**
     * 在由字符串表示的时间date上增加年数后的新的时间字符串
     *
     * @param date 字符串表示的时间
     * @param year 要增加的年数
     * @return
     */
    public String addYears(String date, int year) {
        return add(date, Calendar.YEAR, year);
    }

    /**
     * 在今天的基础上增加月数后的新时间字符串
     *
     * @param month
     * @return
     */
    public String addMonths(int month) {
        return addMonths(new Date(), month);
    }

    /**
     * 在date的基础上增加月数后的新时间字符串
     *
     * @param date
     * @param month
     * @return
     */
    public String addMonths(Date date, int month) {
        return add(date, Calendar.MONTH, month);
    }

    /**
     * 在由字符串表示的时间date上增加月数后的新时间字符串
     *
     * @param date
     * @param month
     * @return
     */
    public String addMonths(String date, int month) {
        return add(date, Calendar.MONTH, month);
    }

    /**
     * 在今天的时间基础上增加星期数后的新时间字符串
     *
     * @param week
     * @return
     */
    public String addWeeks(int week) {
        return addWeeks(new Date(), week);
    }

    /**
     * 在date的基础上增加星期后的新时间字符串
     *
     * @param date
     * @param week
     * @return
     */
    public String addWeeks(Date date, int week) {
        return add(date, Calendar.WEEK_OF_YEAR, week);
    }

    /**
     * 在由字符串表示的时间date上增加星期后的新时间字符串
     *
     * @param date
     * @param week
     * @return
     */
    public String addWeeks(String date, int week) {
        return add(date, Calendar.WEEK_OF_YEAR, week);
    }

    /**
     * 在今天的时间基础上增加天数后的新时间字符串
     *
     * @param day
     * @return
     */
    public String addDays(int day) {
        return addDays(new Date(), day);
    }

    /**
     * 在date的基础上增加天数后的新时间字符串
     *
     * @param date
     * @param day
     * @return
     */
    public String addDays(Date date, int day) {
        return add(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * 在由字符串表示的date上增加天数后的新时间字符串
     *
     * @param date
     * @param day
     * @return
     */
    public String addDays(String date, int day) {
        return add(date, Calendar.DAY_OF_MONTH, day);
    }

    /**
     * 在现在的时间基础上增加小时数后的新时间字符串
     *
     * @param hour
     * @return
     */
    public String addHours(int hour) {
        return addHours(new Date(), hour);
    }

    /**
     * 在date的时间基础上增加小时数后的新时间字符串
     *
     * @param date
     * @param hour
     * @return
     */
    public String addHours(Date date, int hour) {
        return add(date, Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * 在由字符串表示的时间date上增加小时数后的新时间字符串
     *
     * @param date
     * @param hour
     * @return
     */
    public String addHours(String date, int hour) {
        return add(date, Calendar.HOUR_OF_DAY, hour);
    }

    /**
     * 在现在的时间基础上增加分钟数后的新时间字符串
     *
     * @param miniute
     * @return
     */
    public String addMiniutes(int miniute) {
        return addMiniutes(new Date(), miniute);
    }

    /**
     * 在date的时间基础上增加分钟数后的新时间字符串
     *
     * @param date
     * @param miniute
     * @return
     */
    public String addMiniutes(Date date, int miniute) {
        return add(date, Calendar.MINUTE, miniute);
    }

    /**
     * 在由字符串表示的时间date上增加分钟数后的新时间字符串
     *
     * @param date
     * @param miniute
     * @return
     */
    public String addMiniutes(String date, int miniute) {
        return add(date, Calendar.MINUTE, miniute);
    }

    /**
     * 在现在的时间基础上增加秒数后的新时间字符串
     *
     * @param second
     * @return
     */
    public String addSeconds(int second) {
        return addSeconds(new Date(), second);
    }

    /**
     * 在date的时间基础上增加秒数后的新时间字符串
     *
     * @param date
     * @param second
     * @return
     */
    public String addSeconds(Date date, int second) {
        return add(date, Calendar.SECOND, second);
    }

    /**
     * 在由字符串表示的时间date上增加秒数后的新时间字符串
     *
     * @param date
     * @param second
     * @return
     */
    public String addSeconds(String date, int second) {
        return add(date, Calendar.SECOND, second);
    }

    /**
     * 求两时间相减的年数差值(自然年),date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getYearsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year, false);
    }

    /**
     * 求两时间相减的年数差值(自然年),date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public long getYearsBetween(String date1, String date2) {
        return getInterval(date1, date2, UnitConverter.TimeUnit.year, false);
    }

    /**
     * 求两时间相减的年数差值(自然年),并向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getYearsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year, true);
    }

    /**
     * 求两时间相减的年数差值(自然年),并向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public long getYearsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year, true);
    }

    /**
     * 求两时间相减的年数差值(非自然年,用365天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public static long getYears365Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year365, false);
    }

    /**
     * 求两时间相减的年数差值(非自然年,用365天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return
     */
    public long getYears365Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year365, false);
    }

    /**
     * 求两时间相减的年数差值(非自然周,用365天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public static long getYears365BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.year365, true);
    }

    /**
     * 求两时间相减的年数差值(非自然周,用365天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getYears365BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.year365, true);
    }

    /**
     * 求两时间相减的月数差值(自然月),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonthsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month, false);
    }

    /**
     * 求两时间相减的月数差值(自然月),date1-date2
     *
     * @return date1-date2
     */
    public long getMonthsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month, false);
    }

    /**
     * 求两时间相减的月数差值(自然月),并向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonthsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month, true);
    }

    /**
     * 求两时间相减的月数差值(自然月),并向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonthsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month, true);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用31天来算),date1-date2O
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths31Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month31, false);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用31天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonths31Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month31, false);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用31天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths31BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month31, true);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用31天来算),向上取整,date1-date2
     *
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMonths31BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month31, true);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用30天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths30Between(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month30, false);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用30天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getMonths30Between(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month30, false);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用30天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMonths30BetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.month30, true);
    }

    /**
     * 求两时间相减的月数差值(非自然月,用30天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getMonths30BetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.month30, true);
    }

    /**
     * 求两时间相减的周数差值(自然周,用7天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getWeeksBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.week, false);
    }

    /**
     * 求两时间相减的周数差值(自然周,用7天来算),date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getWeeksBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.week, false);
    }

    /**
     * 求两时间相减的周数差值(自然周,用7天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getWeeksBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.week, true);
    }

    /**
     * 求两时间相减的周数差值(自然周,用7天来算),向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getWeeksBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.week, true);
    }

    /**
     * 求两时间相减的天数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getDaysBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.day, false);
    }

    /**
     * 求两时间相减的天数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getDaysBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.day, false);
    }

    /**
     * 求两时间相减的天数差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getDaysBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.day, true);
    }

    /**
     * 求两时间相减的天数差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getDaysBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.day, true);
    }

    /**
     * 求两时间相减的小时差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getHoursBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.hour, false);
    }

    /**
     * 求两时间相减的小时差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getHoursBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.hour, false);
    }

    /**
     * 求两时间相减的小时差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getHoursBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.hour, true);
    }

    /**
     * 求两时间相减的小时差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getHoursBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.hour, true);
    }

    /**
     * 求两时间相减的分钟差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMinutesBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.minute, false);
    }

    /**
     * 求两时间相减的分钟差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getMinutesBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.minute, false);
    }

    /**
     * 求两时间相减的分钟差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMinutesBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.minute, true);
    }

    /**
     * 求两时间相减的分钟差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getMinutesBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.minute, true);
    }

    /**
     * 求两时间相减的秒数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getSecondsBetween(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.second, false);
    }

    /**
     * 求两时间相减的秒数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getSecondsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.second, false);
    }

    /**
     * 求两时间相减的秒数差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getSecondsBetweenCeil(Date date1, Date date2) {
        return getInterval(date1, date2, TimeUnit.second, true);
    }

    /**
     * 求两时间相减的秒数差值,向上取整,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2,如果解析失败,返回Integer.MAX_VALUE
     */
    public long getSecondsBetweenCeil(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.second, true);
    }

    /**
     * 求两时间相减的毫秒数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public static long getMillisecondsBetween(Date date1, Date date2) {
        return date1.getTime() - date2.getTime();
    }

    /**
     * 求两时间相减的毫秒数差值,date1-date2
     *
     * @param date1
     * @param date2
     * @return date1-date2
     */
    public long getMillisecondsBetween(String date1, String date2) {
        return getInterval(date1, date2, TimeUnit.millisecond, false);
    }

    /**
     * 上一个月的时间
     *
     * @return
     */
    public String lastMonthToday() {
        return addMonths(-1);
    }

    /**
     * 上个星期的时间
     *
     * @return
     */
    public String lastWeekToday() {
        return addWeeks(-1);
    }

    /**
     * 下个月的时间
     *
     * @return
     */
    public String nextMonthToday() {
        return addMonths(1);
    }

    /**
     * 下个星期的时间
     *
     * @return
     */
    public String nextWeekToday() {
        return addWeeks(1);
    }

    /**
     * 获取明天的时间
     *
     * @return
     */
    public String tomorrow() {
        return addDays(1);
    }

    /**
     * 获取昨天的时间
     *
     * @return
     */
    public String yesterday() {
        return addDays(-1);
    }

    /**
     * 获取现在的时间
     *
     * @return
     */
    public String now() {
        return dateFormatUnsafe.format(System.currentTimeMillis());
    }

    /**
     * 获取现在时间之后若干秒的时间
     *
     * @param second
     * @return
     */
    public String afterNow(long second) {
        return getDateFormat().format(System.currentTimeMillis() + second * 1000);
    }

    /**
     * 获取现在时间之前若干秒的时间
     *
     * @param second
     * @return
     */
    public String beforeNow(long second) {
        return afterNow(-second);
    }

    /**
     * 把unix时间（秒）转换成指定pattern的字符串
     */
    public String unix2String(long unixTime) {
        return getDateFormat().format(new Date(unixTime * 1000));
    }

    /**
     * 把字符串时间转换成unix时间（秒）
     */
    public long string2Unix(String dateStr) throws Exception {
        Date date = getDateFormat().parse(dateStr);
        return date.getTime() / 1000;
    }

    /**
     * 把unix时间（毫秒）转换成指定pattern的字符串
     */
    public String msunix2String(long unixTime) {
        return getDateFormat().format(new Date(unixTime));
    }

    /**
     * 把字符串时间转换成unix时间（毫秒）
     */
    public long string2Unixms(String dateStr) throws Exception {
        Date date = getDateFormat().parse(dateStr);
        return date.getTime();
    }

    /**
     * 格式化时间成yyyy-MM-dd HH:mm:ss
     *
     * @param date
     * @return
     */
    public String format(Date date) {
        return getDateFormat().format(date);
    }

    /**
     * 解析yyyy-MM-dd HH:mm:ss或yyyy-MM-dd格式
     *
     * @param dateStr
     * @return
     */
    public Date parse(String dateStr) {
        try {
            return getDateFormat().parse(dateStr);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return null;
        }
    }

    /**
     * 日期字符串处理通用方法
     *
     * @param calendar
     * @param opers
     * @return
     */
    public String oper(Calendar calendar, long... opers) {
        return format(DateUtil.oper(calendar, opers).getTime());
    }

    /**
     * 日期字符串处理通用方法
     *
     * @param date
     * @param opers
     * @return
     */
    public String oper(Date date, long... opers) {
        return format(DateUtil.oper(date, opers));
    }

    /**
     * 日期字符串处理通用方法
     *
     * @param dateStr 可以是yyyy-MM-dd HH:mm:ss或yyyy-MM-dd格式
     * @param opers 操作列,使用DateUtil.add(),set(),roll(),ceil(),round(),truncate()方法来构建
     * @return 操作后的结果字符串
     */
    public String oper(String dateStr, long... opers) {
        try {
            DateFormat df = getDateFormat();
            Date d = DateUtil.oper(df.parse(dateStr), opers);
            return df.format(d);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return dateStr;
        }
    }

    /**
     * 时间格式
     */
    private final String dateForamtPattern;
    /**
     * 时间格式工厂
     */
    private final ThreadLocal<DateFormat> dateFormatFactory;
    /**
     * 不安全的时间格式
     */
    private final DateFormat dateFormatUnsafe;

    /**
     * 私有的构造方法
     *
     * @param dateForamtPattern
     */
    private DateStringUtil(String dateForamtPattern) {
        this.dateForamtPattern = dateForamtPattern;
        this.dateFormatFactory = DateUtil.makeDateFormatPerThread(dateForamtPattern);
        this.dateFormatUnsafe = new SimpleDateFormat(dateForamtPattern);
    }

    /**
     * 获得时间格式化器
     *
     * @return
     */
    public DateFormat getDateFormat() {
        return dateFormatFactory.get();
    }

    /**
     * 获得时间格式
     *
     * @return
     */
    public String getDateForamtPattern() {
        return dateForamtPattern;
    }

    /**
     * 获得date1和date2之间的差值，用timeUnit表示，并可设置是否向上取整
     *
     * @param date1
     * @param date2
     * @param timeUnit 差值的单位
     * @param ceil 是否向上取整
     * @return date1-date2
     */
    public long getInterval(String date1, String date2, TimeUnit timeUnit, boolean ceil) {
        DateFormat df = getDateFormat();
        try {
            return DateUtil.getInterval(df.parse(date1), df.parse(date2), timeUnit, ceil);
        } catch (Exception e) {
            log.error(dateForamtPattern, e);
            return DEFAULT_GETBETWEEN_ERROR_RESULT;
        }
    }

    /**
     * 获得date1和date2之间的差值，用timeUnit表示，并可设置是否向上取整
     *
     * @param date1
     * @param date2
     * @param timeUnit 差值的单位
     * @param ceil 是否向上取整
     * @return date1-date2
     */
    public static long getInterval(Date date1, Date date2, TimeUnit timeUnit, boolean ceil) {
        return DateUtil.getInterval(date1, date2, timeUnit, ceil);
    }

    /**
     * DateStringUtil构造工厂方法
     *
     * @param dateForamtPattern
     * @return
     */
    public static DateStringUtil getInstance(String dateForamtPattern) {
        DateStringUtil dsu = allDateStringUtil.get(dateForamtPattern);
        if (dsu == null) {
            dsu = new DateStringUtil(dateForamtPattern);
            allDateStringUtil.putIfAbsent(dateForamtPattern, dsu);
        }
        return dsu;
    }
}
