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

import java.util.Arrays;

/**
 * 解决 Arrays中大部分方法不能处理 Object的情况,也就是调用方法前对象类型未知的情况
 * 
 * @author ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/10/30
 */
public class ArraysUtil {

    /**
     * 判断两个对象（任意类型）是否相等
     * 
     * @param e1
     * @param e2
     * @return
     */
    public static boolean equals(Object e1, Object e2) {
        boolean eq = false;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = Arrays.equals((Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else if (null == e1 && null == e2) { // 双方都null，返回true
            eq = true;
        } else if (null != e1) {
            eq = e1.equals(e2);
        }
        return eq;
    }

    /**
     * 判断两个对象（任意类型）是否深层相等
     * 
     * @param e1
     * @param e2
     * @return
     */
    public static boolean deepEquals(Object e1, Object e2) {
        boolean eq = false;
        if (e1 instanceof Object[] && e2 instanceof Object[]) {
            eq = Arrays.deepEquals((Object[]) e1, (Object[]) e2);
        } else if (e1 instanceof byte[] && e2 instanceof byte[]) {
            eq = Arrays.equals((byte[]) e1, (byte[]) e2);
        } else if (e1 instanceof short[] && e2 instanceof short[]) {
            eq = Arrays.equals((short[]) e1, (short[]) e2);
        } else if (e1 instanceof int[] && e2 instanceof int[]) {
            eq = Arrays.equals((int[]) e1, (int[]) e2);
        } else if (e1 instanceof long[] && e2 instanceof long[]) {
            eq = Arrays.equals((long[]) e1, (long[]) e2);
        } else if (e1 instanceof char[] && e2 instanceof char[]) {
            eq = Arrays.equals((char[]) e1, (char[]) e2);
        } else if (e1 instanceof float[] && e2 instanceof float[]) {
            eq = Arrays.equals((float[]) e1, (float[]) e2);
        } else if (e1 instanceof double[] && e2 instanceof double[]) {
            eq = Arrays.equals((double[]) e1, (double[]) e2);
        } else if (e1 instanceof boolean[] && e2 instanceof boolean[]) {
            eq = Arrays.equals((boolean[]) e1, (boolean[]) e2);
        } else if (null == e1 && null == e2) { // 双方都null，返回true
            eq = true;
        } else if (null != e1) {
            eq = e1.equals(e2);
        }
        return eq;
    }

    /**
     * 将任意类型的对象深层的转化为字符串
     * 
     * <pre>
     * jdk中的Array.deepToString方法不能处理未知类型的对象,只能处理 Object[],这里作了些增强
     * 以处理反射新建出来的对象的问题
     * </pre>
     * 
     * @param array
     * @return
     */
    public static String deepToString(Object array) {
        if (array == null) {
            return "null";
        }
        Class<?> clazz = array.getClass();
        if (clazz.isArray()) {
            if (clazz == byte[].class) {
                return Arrays.toString((byte[]) array);
            } else if (clazz == short[].class) {
                return Arrays.toString((short[]) array);
            } else if (clazz == int[].class) {
                return Arrays.toString((int[]) array);
            } else if (clazz == long[].class) {
                return Arrays.toString((long[]) array);
            } else if (clazz == char[].class) {
                return Arrays.toString((char[]) array);
            } else if (clazz == float[].class) {
                return Arrays.toString((float[]) array);
            } else if (clazz == double[].class) {
                return Arrays.toString((double[]) array);
            } else if (clazz == boolean[].class) {
                return Arrays.toString((boolean[]) array);
            } else { // array is an array of object references
                return Arrays.deepToString((Object[]) array);
            }
        }
        return array.toString();
    }

    /**
     * 将任意类型的对象转化为字符串
     * 
     * <pre>
     * jdk中的Array.toString方法不能处理未知类型的对象,只能已知数组类型的情况,这里作了些增强
     * 以处理反射新建出来的对象的问题
     * </pre>
     * 
     * @param array
     * @return
     */
    public static String toString(Object array) {
        if (array == null) {
            return "null";
        }
        Class<?> clazz = array.getClass();
        if (clazz.isArray()) {
            if (clazz == byte[].class) {
                return Arrays.toString((byte[]) array);
            } else if (clazz == short[].class) {
                return Arrays.toString((short[]) array);
            } else if (clazz == int[].class) {
                return Arrays.toString((int[]) array);
            } else if (clazz == long[].class) {
                return Arrays.toString((long[]) array);
            } else if (clazz == char[].class) {
                return Arrays.toString((char[]) array);
            } else if (clazz == float[].class) {
                return Arrays.toString((float[]) array);
            } else if (clazz == double[].class) {
                return Arrays.toString((double[]) array);
            } else if (clazz == boolean[].class) {
                return Arrays.toString((boolean[]) array);
            } else { // array is an array of object references
                return Arrays.toString((Object[]) array);
            }
        }
        return array.toString();
    }
}
