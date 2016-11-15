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

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主要完成数据类型的转换，能够完成单个对象的转换，容器中对象的整体转换
 * 
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since 2010-6-3 下午04:33:18
 */
public class ValueUtil {

    public static final String[] REF_ARRAY_STRING = new String[0];
    public static final Boolean[] REF_ARRAY_BOOLEAN = new Boolean[0];
    public static final Byte[] REF_ARRAY_BYTE = new Byte[0];
    public static final Character[] REF_ARRAY_CHARACTER = new Character[0];
    public static final Short[] REF_ARRAY_SHORT = new Short[0];
    public static final Integer[] REF_ARRAY_INTEGER = new Integer[0];
    public static final Long[] REF_ARRAY_LONG = new Long[0];
    public static final Float[] REF_ARRAY_FLOAT = new Float[0];
    public static final Double[] REF_ARRAY_DOUBLE = new Double[0];
    private static final IllegalArgumentException classNotBasicTypeException = new IllegalArgumentException("classNotBasicType");

    /**
     * 基本型的转型方法列表
     */
    private static final Map<Type, Method> valueOfMethod = initValueOfMethod();

    /**
     * 字符串型转换成boolean型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static boolean getBoolean(String value, boolean defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        if (value.equals("true") || value.equalsIgnoreCase("y") || value.equals("1")) {
            return true;
        }
        return false;
    }

    /**
     * 字符串型转换成byte型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static byte getByte(String value, byte defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Byte.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 字符串转换成Character型，直接取第一个字符
     * 
     * @param str
     * @return
     */
    public static Character getCharacter(String str) {
        return str.charAt(0);
    }

    /**
     * 字符串转换成char型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static char getCharacter(String value, char defaultValue) {
        if (StringTools.isEmpty(value)) {
            return defaultValue;
        }
        return value.charAt(0);
    }

    /**
     * 字符串转换成char型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static double getDouble(String value, double defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Double.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String 转换成 float型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static float getFloat(String value, float defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Float.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String 型转换成Integer 型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static int getInteger(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String型转换成 Long型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static long getLong(String value, long defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Long.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * String 型 转换成 Short型
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static short getShort(String value, short defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            return Short.valueOf(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * 如果传入value为null就返回defaultValue否则就返回value
     */
    public static String getString(String value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        return value;
    }

    /**
     * 获取由String类型转到基本类型的方法
     * 
     * @param clazz
     * @return
     */
    protected static Method getValueOfMethod(Class<?> clazz) {
        Method m = valueOfMethod.get(clazz);
        if (m == null) {
            throw classNotBasicTypeException;
        }
        return m;
    }

    /**
     * 获得基本类型-基本类型转到String类型的方法的 映射列表
     * 
     * @return
     */
    private static Map<Type, Method> initValueOfMethod() {
        Map<Type, Method> valueOfMethods = new HashMap<Type, Method>();
        try {
            valueOfMethods.put(int.class, Integer.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Integer.class, Integer.class.getMethod("valueOf", String.class));
            valueOfMethods.put(long.class, Long.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Long.class, Long.class.getMethod("valueOf", String.class));
            valueOfMethods.put(float.class, Float.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Float.class, Float.class.getMethod("valueOf", String.class));
            valueOfMethods.put(double.class, Double.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Double.class, Double.class.getMethod("valueOf", String.class));
            valueOfMethods.put(short.class, Short.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Short.class, Short.class.getMethod("valueOf", String.class));
            valueOfMethods.put(boolean.class, Boolean.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Boolean.class, Boolean.class.getMethod("valueOf", String.class));
            valueOfMethods.put(byte.class, Byte.class.getMethod("valueOf", String.class));
            valueOfMethods.put(Byte.class, Byte.class.getMethod("valueOf", String.class));
            valueOfMethods.put(String.class, String.class.getMethod("valueOf", Object.class));
            // 由于Character.valueOf方法的参数为char，对此需要使用一个特殊的方法
            valueOfMethods.put(char.class, ValueUtil.class.getMethod("getCharacter", String.class));
            valueOfMethods.put(Character.class, ValueUtil.class.getMethod("getCharacter", String.class));
        } catch (Exception e) {
        }
        return valueOfMethods;
    }

    /**
     * <pre>
     * 将在Collection中存放的对象存放到 Array中
     * 此方法可以处理 原始数据类型,故返回值是 Object,须得到值后再转型
     * @param <T>
     * @param coll  Collection引用
     * @param clazz  Collection中对象的类型
     * @return
     */
    public static <T> Object toArray(Collection<T> coll, Class<T> clazz) {
        Object result = Array.newInstance(clazz, coll.size());
        int i = 0;
        for (T obj : coll) {
            Array.set(result, i++, obj);
        }
        return result;
    }

    /**
     * <pre>
     * Collection -> Array
     * 将在Collection中存放的对象存放到 Array中
     * 此方法使用引用refArray的方式,不能处理 原始数据类型,但返回值支持泛型
     * @param <T>
     * @param coll
     * @param refArray
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Collection<T> coll, T[] refArray) {
        Object result = Array.newInstance(refArray.getClass().getComponentType(), coll.size());
        int i = 0;
        for (T obj : coll) {
            Array.set(result, i++, obj);
        }
        return (T[]) result;
    }

    /**
     * <pre>
     * Collection<String> -> Collection<T> (原集合类型)
     * 将容器中String类型的数据转换成目标类型，并存放在新的容器中
     * 将传入的字符串Collection转化为指定类型的Collection（只支持基本型）
     * @param <T>
     * @param strList  原容器的引用
     * @param componentClazz 返回结果中存放的数据类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> valueOf(Collection<String> strList, Class<T> componentClazz) {
        Method method = getValueOfMethod(componentClazz);
        if (strList == null || strList.size() == 0) {
            return Collections.emptyList();
        }
        try {
            Collection<T> result = strList.getClass().newInstance();
            for (String a : strList) {
                try {
                    result.add((T) method.invoke(null, a));
                } catch (Exception e) {// TODO:是否打日志
                }
            }
            return result;
        } catch (Exception e1) {
            return Collections.emptyList();
        }
    }

    /**
     * <pre>
     * Collection<String> -> Collection<T> (新集合类型)
     * 将容器中String类型的数据转换成目标类型，并存放在心的容器中，新的容器的引用可以提前指定
     * 将传入的字符串Collection转化为指定类型的集合（只支持基本型）
     * @param <T>
     * @param strList 原容器的引用
     * @param componentClazz 目标类型
     * @param result 新容器的引用
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> valueOf(Collection<String> strList, Class<T> componentClazz, Collection<T> result) {
        Method method = getValueOfMethod(componentClazz);
        if (strList != null) {
            for (String a : strList) {
                try {
                    result.add((T) method.invoke(null, a));
                } catch (Exception e) {// TODO:是否打日志
                }
            }
        }
        return result;
    }

    /**
     * 将指定类型转化成目标类型
     * 
     * @param <T> 泛型
     * @param obj 待转换的对象
     * @param destClazz 目标类类型
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    public static <T> T valueOf(Object obj, Class<T> destClazz) throws Exception {
        Method method = getValueOfMethod(destClazz);
        return (T) method.invoke(null, obj.toString());
    }

    /**
     * 将指定类型转化为目标类型，如果转化失败返回默认的类型
     * 
     * @param <T>
     * @param obj 待转化的对象
     * @param destClazz 目标类型
     * @param defaultValue 默认值
     * @return
     */
    public static <T> T valueOf(Object obj, Class<T> destClazz, T defaultValue) {
        try {
            return valueOf(obj, destClazz);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * <pre>
     * Collection<String> 
     *   --valueOf()-->   List<T>
     *   --toArray()-->   T[]
     * 将容器中String类型的对象转化为指定类型，并存放于Array数组中
     * 因为使用指定componentClazz,故支持原始数据类型,但返回值是Object,须再转型
     * </pre>
     * 
     * @param <T>
     * @param strList
     * @param componentClazz
     * @return
     */
    @SuppressWarnings({
        "unchecked",
        "rawtypes",
        "cast"
    })
    public static <T> Object valueOfToArray(Collection<String> strList, Class<T> componentClazz) {
        return toArray((Collection<T>) valueOf(strList, componentClazz, new ArrayList(strList.size())), componentClazz);
    }

    /**
     * <pre>
     * Collection<String> 
     *   --valueOf()-->   List<T>
     *   --toArray()-->   T[]
     * 将容器中String类型的对象转换成新类型并放入数组中
     * 因为使用refArray,故不支持原始数据类型
     * </pre>
     * 
     * @param <T>
     * @param strList 原容器的引用
     * @param refArray 新类型数组的引用
     * @return
     */
    @SuppressWarnings({
        "unchecked",
        "rawtypes",
        "cast"
    })
    public static <T> T[] valueOfToArray(Collection<String> strList, T[] refArray) {
        return toArray((Collection<T>) valueOf(strList, refArray.getClass().getComponentType(), new ArrayList(strList.size())), refArray);
    }

    /**
     * 默认构造方法
     */
    private ValueUtil() {
    }
}
