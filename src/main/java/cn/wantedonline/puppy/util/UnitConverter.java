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

/**
 * 单位换算类,使得代码减少出现不好理解的数字,更易读
 *
 */
public class UnitConverter {

    public enum RmbUnit {
        fen(1), jiao(10), yuan(100);

        private long value;

        private RmbUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    public enum TimeUnit {
        millisecond(1),
        second(DateUtils.MILLIS_PER_SECOND),
        minute(DateUtils.MILLIS_PER_MINUTE),
        hour(DateUtils.MILLIS_PER_HOUR),
        day(DateUtils.MILLIS_PER_DAY),
        week(DateUtils.MILLIS_PER_DAY * 7),
        month31(DateUtils.MILLIS_PER_DAY * 31),
        month30(DateUtils.MILLIS_PER_DAY * 30),
        year365(DateUtils.MILLIS_PER_DAY * 365),

        month(DateUtils.MILLIS_PER_DAY * 30),
        year(DateUtils.MILLIS_PER_DAY * 365);

        private long value;

        private TimeUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    public enum ByteUnit {
        b(1), kb(1024l), mb(1024l * 1024), gb(1024l * 1024 * 1024), tb(1024l * 1024 * 1024 * 1024);

        private long value;

        private ByteUnit(long value) {
            this.value = value;
        }

        public long get() {
            return value;
        }
    }

    /**
     * 获得from数值是to数值的倍数，并可设置是否向上取整
     *
     * @param from from
     * @param to
     * @param ceil 是否向上取整
     * @return
     */
    private static double convert(long from, long to, boolean ceil) {
        if (ceil) {
            return ceil((double) from / to);
        }
        return (from / to);
    }

    /**
     * 获得向上取整的数值
     *
     * @param num
     * @return
     */
    private static double ceil(double num) {
        if (num < 0) {
            return -Math.ceil(-num);
        }
        return Math.ceil(num);
    }

    /**
     * 将单位为fromUnit的字节数fromValue转换为toUnit单位的字节数
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertByte(long fromValue, ByteUnit fromUnit, ByteUnit toUnit) {
        return (long) convertByte(fromValue, fromUnit, toUnit, false);
    }

    /**
     * 将单位为fromUnit的字节数fromValue转换为toUnit单位的字节数，并向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertByteCeil(long fromValue, ByteUnit fromUnit, ByteUnit toUnit) {
        return (long) convertByte(fromValue, fromUnit, toUnit, true);
    }

    /**
     * 将单位为fromUnit的字节数fromValue转换为toUnit单位的字节数，并可设置是否向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @param ceil 是否向上取整
     * @return 目标数值
     */
    public static double convertByte(long fromValue, ByteUnit fromUnit, ByteUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    /**
     * 将单位为fromUnit的时间fromValue转换为toUnit单位的时间
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertTime(long fromValue, TimeUnit fromUnit, TimeUnit toUnit) {
        return (long) convertTime(fromValue, fromUnit, toUnit, false);
    }

    /**
     * 将单位为fromUnit的时间fromValue转换为toUnit单位的时间，并向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertTimeCeil(long fromValue, TimeUnit fromUnit, TimeUnit toUnit) {
        return (long) convertTime(fromValue, fromUnit, toUnit, true);
    }

    /**
     * 将单位为fromUnit的时间fromValue转换为toUnit单位的时间，并可设置是否向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @param ceil 是否向上取整
     * @return 目标数值
     */
    public static double convertTime(long fromValue, TimeUnit fromUnit, TimeUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    /**
     * 将单位为fromUnit的人民币fromValue转换为toUnit单位的人民币
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertRmb(long fromValue, RmbUnit fromUnit, RmbUnit toUnit) {
        return (long) convertRmb(fromValue, fromUnit, toUnit, false);
    }

    /**
     * 将单位为fromUnit的人民币fromValue转换为toUnit单位的人民币，并向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @return 目标数值
     */
    public static long convertTimeCeil(long fromValue, RmbUnit fromUnit, RmbUnit toUnit) {
        return (long) convertRmb(fromValue, fromUnit, toUnit, true);
    }

    /**
     * 将单位为fromUnit的人民币fromValue转换为toUnit单位的人民币，并可设置是否向上取整
     *
     * @param fromValue 原数值
     * @param fromUnit 原单位
     * @param toUnit 目标单位
     * @param ceil 是否向上取整
     * @return 目标数值
     */
    public static double convertRmb(long fromValue, RmbUnit fromUnit, RmbUnit toUnit, boolean ceil) {
        return convert(fromValue * fromUnit.get(), toUnit.get(), ceil);
    }

    public static void main(String[] args) {
        System.out.println(convertTime(2, TimeUnit.day, TimeUnit.millisecond));
        System.out.println(convertTime(1, TimeUnit.year, TimeUnit.day));
        System.out.println(convertTimeCeil(1, TimeUnit.day, TimeUnit.year));
        System.out.println(convertByte(1, ByteUnit.kb, ByteUnit.b));
        System.out.println(Math.ceil(-3.2));
    }

    /**
     * 默认构造方法
     */
    private UnitConverter() {
    }
}
