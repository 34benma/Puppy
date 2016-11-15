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

package cn.wantedonline.puppy.spring;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     Spring容器中Bean工具类
 *     提供的功能有：各种方式获取Bean
 *                 判断某个class是否是基本数据类型或String类型
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/10/28
 */
public class BeanUtil {
    private static final Set<Type> basicTypes = new HashSet<>();

    static {
        basicTypes.add(int.class);
        basicTypes.add(Integer.class);
        basicTypes.add(long.class);
        basicTypes.add(Long.class);
        basicTypes.add(float.class);
        basicTypes.add(Float.class);
        basicTypes.add(double.class);
        basicTypes.add(Double.class);
        basicTypes.add(short.class);
        basicTypes.add(Short.class);
        basicTypes.add(boolean.class);
        basicTypes.add(Boolean.class);
        basicTypes.add(char.class);
        basicTypes.add(Character.class);
        basicTypes.add(byte.class);
        basicTypes.add(Byte.class);
        basicTypes.add(String.class);
    }

    /**
     * 在传入的context中获取容器中的clazz类型的Bean，注意，如果有多个同类型的不同name的Bean在容器中，则只随机返回一个
     * @param context
     * @param clazz
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getTypedBean(ApplicationContext context, Class<T> clazz) throws BeansException {
        for (String name : context.getBeanNamesForType(clazz)) {
            return (T) context.getBean(name);
        }
        return null;
    }

    /**
     * 指定Bean名称获取Bean
     * @param context
     * @param name
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getTypedBean(ApplicationContext context, String name) throws BeansException {
        return (T) context.getBean(name);
    }

    /**
     * 获取本系统Spring容器中的Bean，如果有多个同类型不同名称的Bean则随机返回一个
     * @param clazz
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getTypedBean(Class<T> clazz) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), clazz);
    }

    /**
     * 获取本系统Spring容器中指定名称的Bean
     * @param name
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> T getTypedBean(String name) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), name);
    }

    /**
     * 获取指定context中某种类型的Bean，返回值是一个Map，key为bean的name，value是bean的引用
     * @param context
     * @param clazz
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> Map<String, T> getTypedBeans(ApplicationContext context, Class<T> clazz) throws BeansException {
        Map<?, ?> map = context.getBeansOfType(clazz);
        Map<String, T> r = new HashMap<>();
        for (Map.Entry<?, ?> e : map.entrySet()) {
            String beanName = e.getKey().toString();
            T bean = (T) e.getValue();
            r.put(beanName, bean);
        }
        return r;
    }

    /**
     * 获取本系统中指定类型的Bean集合，返回值是Map,key为bean的name，value为bean的引用
     * @param clazz
     * @param <T>
     * @return
     * @throws BeansException
     */
    public static <T> Map<String, T> getTypedBeans(Class<T> clazz) throws BeansException {
        return getTypedBeans(SpringBootstrap.getContext(), clazz);
    }

    /**
     * 获取本系统spring容器中指定类型的Bean，如果存在多个同类型不同名称的bean，则随机返回一个
     * 吞掉异常，需要判断空指针
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getTypedBeanSilently(Class<T> clazz) {
        try {
            return getTypedBean(clazz);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 获取本系统spring容器中指定名称的bean
     * 吞掉异常，需要判断空指针
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getTypedBeansSilently(String name) {
        try {
            return getTypedBean(name);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 获取本系统spring容器中全部同类型的bean，返回值为map，key是bean的name,value是bean的引用
     * 吞掉异常，需要判断空指针
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getTypedBeansSilently(Class<T> clazz) {
        try {
            return getTypedBeans(clazz);
        } catch (Throwable e) {
            return null;
        }
    }

    /**
     * 判断clazz是否是基本数据类型或String类型
     * @param clazz
     * @return
     */
    public static boolean isBasicType(Type clazz) {
        return basicTypes.contains(clazz);
    }

    /**
     * 判断clazz是否不是基本数据类型并且不是String类型
     * @param clazz
     * @return
     */
    public static boolean isNotBasicType(Type clazz) {
        return !basicTypes.contains(clazz);
    }

}
