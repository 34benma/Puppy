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

/**
 * <pre>
 *  通用日期设置功能:DateUtil.oper()的帮助类
 *  用于生成 单个操作的 MagicNum
 *  
 * @author ZengDong
 * @since V0.1.0 on 2016/10/30
 */
public class DateUtilHelper {

    public static final int SET = 5;
    public static final int ADD = 4;
    public static final int ROLL = 3;
    public static final int CEIL = 2;
    public static final int ROUND = 1;
    public static final int TRUNCATE = 0;

    /**
     * 生成set calendarField成amount的MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long set(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - SET * 100 - calendarField;
        }
        return amount * 1000l + SET * 100 + calendarField;
    }

    /**
     * 生成 对应calendarField add(amount)的MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long add(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - ADD * 100 - calendarField;
        }
        return amount * 1000l + ADD * 100 + calendarField;
    }

    /**
     * 生成 对应calendarField roll(amount)的MagicNum
     * 
     * @param calendarField
     * @param amount
     * @return
     */
    public static long roll(int calendarField, int amount) {
        if (amount < 0) {
            return amount * 1000l - ROLL * 100 - calendarField;
        }
        return amount * 1000l + ROLL * 100 + calendarField;
    }

    /**
     * 生成 ceil calendarField的MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long ceil(int calendarField) {
        return CEIL * 100 + calendarField;
    }

    /**
     * 生成 round calendarField的MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long round(int calendarField) {
        return ROUND * 100 + calendarField;
    }

    /**
     * 生成 truncate calendarField的MagicNum
     * 
     * @param calendarField
     * @return
     */
    public static long truncate(int calendarField) {
        return TRUNCATE * 100 + calendarField;
    }

    /**
     * 从MagicNum中取出amount字段
     * 
     * @param operMagic
     * @return
     */
    public static int getAmount(long operMagic) {
        return (int) (operMagic / 1000);
    }

    /**
     * 从MagicNum中取出操作动作字段
     * 
     * @param operMagic
     * @return
     */
    public static int getOperType(long operMagic) {
        int r = (int) (operMagic % 1000 / 100);
        return Math.abs(r);
    }

    /**
     * 从MagicNum中取出CalendarField字段
     * 
     * @param operMagic
     * @return
     */
    public static int getCalendarField(long operMagic) {
        int r = (int) (operMagic % 100);
        return Math.abs(r);
    }
}
