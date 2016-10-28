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

import org.springframework.beans.BeanUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by louiswang on 16/10/28.
 */
public class ReflectConvention {
    public static final boolean SPRING_ENABLE;

    static {
        SPRING_ENABLE = isClassFound("org.springframework.beans.BeanUtils");
        if (!SPRING_ENABLE) {
            System.err.println("ReflectConvention.SPRING_ENALBE=false");
        }
    }

    private ReflectConvention() {}

    private static String captialize(String name) {
        return name.substring(0,1).toUpperCase() + name.substring(1);
    }

    public static Method buildGetterMethod(Class<?> clazz, Field field) throws SecurityException, NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (null == pd) {
                throw new NoSuchMethodError(clazz.getName() + "." + fieldName);
            }
            return pd.getReadMethod();
        }
        String prefix = "get";
        if (field.getType().equals(boolean.class)) {
            prefix = "is";
        }
        String getterStr = prefix + captialize(field.getName());
        return clazz.getDeclaredMethod(getterStr);
    }

    public static Method buildSetterMethod(Class<?> clazz, Field field, Class<?> parameterType) throws NoSuchMethodException {
        if (SPRING_ENABLE) {
            String fieldName = field.getName();
            PropertyDescriptor pd = BeanUtils.getPropertyDescriptor(clazz, fieldName);
            if (null == pd) {
                throw new NoSuchMethodError(clazz.getName() + "." + fieldName);
            }
            return pd.getWriteMethod();
        }
        String setterStr = "set" + captialize(field.getName());
        return clazz.getDeclaredMethod(setterStr, parameterType);
    }

    public static boolean isClassFound(String clazzName) {
        try {
            Class.forName(clazzName);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

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
     * 将data的field属性设置成value
     * @param data
     * @param field
     * @param value
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    public static void setValue(Object data, Field field, Object value) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> clazz = field.getDeclaringClass();
        Method method = buildSetterMethod(clazz, field, field.getType());
        method.invoke(data, value);
    }

    /**
     * 获得data对象的field属性值
     * @param data
     * @param field
     * @return
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     * @throws NoSuchMethodException
     */
    public static Object getValue(Object data, Field field) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        Class<?> clazz = field.getDeclaringClass();
        Method method = buildGetterMethod(clazz, field);
        return method.invoke(data);
    }

    /**
     * 递归查找给定类中的字段，如果找不到，返回null
     * @param clazz
     * @param fieldName
     * @return
     */
    public static Field getDeclaredField(Class<?> clazz, String fieldName) throws NoSuchFieldException {
        Field field = null;
        try {
            field = clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            try {
                field = clazz.getField(fieldName);
            } catch (NoSuchFieldException ex) {
                if (null == clazz.getSuperclass()) {
                    throw ex;
                }
                field = getDeclaredField(clazz.getSuperclass(), fieldName);
            }

        }
        return field;
    }

    /**
     * 递归查找给定类中的方法，如果找不到，返回null
     * @param clazz
     * @param methodName
     * @param paraClasses
     * @return
     */
    public static Method getDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paraClasses) throws NoSuchMethodException {
        Method method = null;
        try {
            method = clazz.getDeclaredMethod(methodName, paraClasses);
        } catch (NoSuchMethodException e) {
            try {
                method = clazz.getMethod(methodName, paraClasses);
            } catch (NoSuchMethodException ex) {
                if (null == clazz.getSuperclass()) {
                    throw ex;
                }
                method = getDeclaredMethod(clazz.getSuperclass(), methodName, paraClasses);
            }
        }
        return method;
    }
}
