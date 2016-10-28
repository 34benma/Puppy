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

import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by wangcheng on 2016/10/27.
 */
public class BeanUtil {
    private static final Logger logger = Log.getLogger();
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

    public static <T> T getTypedBean(ApplicationContext context, Class<T> clazz) throws BeansException {
        for (String name : context.getBeanNamesForType(clazz)) {
            return (T) context.getBean(name);
        }
        return null;
    }

    public static <T> T getTypedBean(ApplicationContext context, String name) throws BeansException {
        return (T) context.getBean(name);
    }

    public static <T> T getTypedBean(Class<T> clazz) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), clazz);
    }

    public static <T> T getTypedBean(String name) throws BeansException {
        return getTypedBean(SpringBootstrap.getContext(), name);
    }

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

    public static <T> Map<String, T> getTypedBeans(Class<T> clazz) throws BeansException {
        return getTypedBeans(SpringBootstrap.getContext(), clazz);
    }

    /**
     * 吞掉异常，需要判断空指针
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getTypedBeanSilently(Class<T> clazz) {
        try {
            return getTypedBean(clazz);
        } catch (Throwable e) {
            logger.info("{}",e.toString());
            return null;
        }
    }

    /**
     * 吞掉异常，需要判断空指针
     * @param name
     * @param <T>
     * @return
     */
    public static <T> T getTypedBeansSilently(String name) {
        try {
            return getTypedBean(name);
        } catch (Throwable e) {
            logger.info("{}",e.toString());
            return null;
        }
    }

    /**
     * 吞掉异常，需要判断空指针
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> Map<String, T> getTypedBeansSilently(Class<T> clazz) {
        try {
            return getTypedBeans(clazz);
        } catch (Throwable e) {
            logger.info("{}", e.toString());
            return null;
        }
    }

    public static boolean isBasicType(Type clazz) {
        return basicTypes.contains(clazz);
    }

    public static boolean isNotBasicType(Type clazz) {
        return !basicTypes.contains(clazz);
    }

}
