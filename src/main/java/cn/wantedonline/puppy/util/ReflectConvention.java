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

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.springframework.beans.BeanUtils;

/**
 * 反射涉及到的一些常用方法
 * 
 * <pre>
 *  org.springframework.beans.BeanUtils
 *  Introspector.getBeanInfo 
 *  org.springframework.util.ReflectionUtils
 * 
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/10/30
 */
public class ReflectConvention {

    /**
     * 是否拥有Spring的支持
     */
    public static final boolean SPRING_ENABLE;

    static {
        SPRING_ENABLE = isClassFound("org.springframework.beans.BeanUtils");
        if (!SPRING_ENABLE) {
            System.err.println("ReflectConvention.SPRING_ENABLE=false");
        }
    }

    /**
     * 获得类的属性的getter方法
     * 
     * @param clazz 类
     * @param field 属性
     * @return
     * @throws SecurityException 安全异常
     * @throws NoSuchMethodException 没有此方法异常
     */
    public static Method buildGetterMethod(Class<?> clazz, Field field) throws SecurityException, NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (pd == null) {
                throw new NoSuchMethodException(clazz.getName() + "." + fieldName);
            }
            return pd.getReadMethod();
        }
        String prefix = "get";
        if (field.getType().equals(boolean.class)) {
            prefix = "is";
        }
        String getterStr = prefix + capitalize(field.getName());
        return clazz.getDeclaredMethod(getterStr);
    }

    /**
     * 获得类的属性的setter方法
     * 
     * @param clazz 类
     * @param field 属性
     * @param parameterType 属性的类型
     * @return
     * @throws SecurityException 安全异常
     * @throws NoSuchMethodException 没有此方法异常
     */
    public static Method buildSetterMethod(Class<?> clazz, Field field, Class<?> parameterType) throws SecurityException, NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (pd == null) {
                throw new NoSuchMethodException(clazz.getName() + "." + fieldName);
            }
            return pd.getWriteMethod();
        }
        String getterStr = "set" + capitalize(field.getName());
        return clazz.getDeclaredMethod(getterStr, parameterType);
    }

    /**
     * 将字符串的首字母变成大写
     * 
     * @param name
     * @return
     */
    private static String capitalize(String name) {
        return name.substring(0, 1).toUpperCase() + name.substring(1);
    }

    /**
     * 利用递归找一个类的指定属性，如果找不到，去父亲里面找直到最上层Object对象为止。
     * 
     * @param clazz 类
     * @param fieldName 数次那个
     * @return
     * @throws Exception
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws Exception {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(fieldName);
            } catch (NoSuchFieldException ex) {
                if (clazz.getSuperclass() == null) {
                    throw ex;
                }
                field = getDeclaredField(clazz.getSuperclass(), fieldName);
            }
        }
        return field;
    }

    /**
     * 利用递归找一个类的指定方法，如果找不到，去父亲里面找直到最上层Object对象为止。
     * 
     * @param clazz 类
     * @param methodName 方法名称
     * @param classes 方法的形参类型
     * @return
     * @throws Exception
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... classes) throws Exception {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, classes);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, classes);
            } catch (NoSuchMethodException ex) {
                if (clazz.getSuperclass() == null) {
                    throw ex;
                }
                method = getDeclaredMethod(clazz.getSuperclass(), methodName, classes);
            }
        }
        return method;
    }

    /**
     * 获得data对象field属性的值
     * 
     * @param data 对象
     * @param field 属性
     * @return
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object getValue(Object data, Field field) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = buildGetterMethod(field.getDeclaringClass(), field);
        return method.invoke(data);
    }

    /**
     * 加载指定的类，并返回是否加载成功
     * 
     * @param clazzName 类的全名
     * @return
     */
    public static boolean isClassFound(String clazzName) {
        try {
            Class.forName(clazzName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 加载指定的若干个类，并返回是否加载成功
     * 
     * @param clazzNames 类全名列表
     * @return
     */
    public static boolean isClassFound(String... clazzNames) {
        try {
            for (String clazzName : clazzNames) {
                Class.forName(clazzName);
            }
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 将对象data的属性field设置为值value
     * 
     * @param data 对象
     * @param field 属性
     * @param value 值
     * @throws SecurityException
     * @throws NoSuchMethodException
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static void setValue(Object data, Field field, Object value) throws SecurityException, NoSuchMethodException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?> clazz = field.getDeclaringClass();
        Method method = buildSetterMethod(clazz, field, field.getType());
        method.invoke(data, value);
    }

    /**
     * 默认的构造方法
     */
    private ReflectConvention() {
    }
}
