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

package cn.wantedonline.puppy.jdbc.util;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import cn.wantedonline.puppy.util.ValueUtil;

/**
 * 被包装增强的Map，可以用指定的类型来get其中的值
 *
 * @since V0.5.0
 * @author thunder
 */
public abstract class WrappedMap<K, V> implements Map<K, V>, KeyValueGetter {

    @SuppressWarnings("rawtypes")
    private static final WrappedMap EMPTY_MAP = new EmptyWrappedMap();

    /**
     * 返回一个空的没有数据的WrappedMap，且不允许往里面写入内容
     */
    public static final <K, V> WrappedMap<K, V> emptyMap() {
        return EMPTY_MAP;
    }

    private static class EmptyWrappedMap<K, V> extends WrappedMap<K, V> implements Serializable {

        private static final long serialVersionUID = -6377292972691697800L;

        private EmptyWrappedMap() {
        }

        @Override
        public String getString(String key) {
            return null;
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Override
        public Set<K> keySet() {
            return Collections.emptySet();
        }

        @Override
        public Collection<V> values() {
            return Collections.emptySet();
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return Collections.emptySet();
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Map) && ((Map<?, ?>) o).isEmpty();
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

    protected Map<K, V> inner = new LinkedHashMap<K, V>();

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return inner.get(key);
    }

    @Override
    public V put(K key, V value) {
        return inner.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return inner.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        inner.putAll(m);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public Set<K> keySet() {
        return inner.keySet();
    }

    @Override
    public Collection<V> values() {
        return inner.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return inner.entrySet();
    }

    @Override
    public boolean equals(Object o) {
        return inner.equals(o);
    }

    @Override
    public int hashCode() {
        return inner.hashCode();
    }

    @Override
    public String toString() {
        return inner.toString();
    }

    /**
     * 转换为对应的类型是通过值字符串进行的，需要指定这个值字符串是如何获取到的
     */
    @Override
    public abstract String getString(String key);

    @Override
    public String getString(String key, String defaultValue) {
        String value = getString(key);
        return null == value ? defaultValue : value;
    }

    @Override
    public Boolean getBool(String key, Boolean defaultValue) {
        return ValueUtil.getBoolean(getString(key), defaultValue);
    }

    @Override
    public Integer getInt(String key, Integer defaultValue) {
        return ValueUtil.getInteger(getString(key), defaultValue);
    }

    @Override
    public Long getLong(String key, Long defaultValue) {
        return ValueUtil.getLong(getString(key), defaultValue);
    }

    @Override
    public Float getFloat(String key, Float defaultValue) {
        return ValueUtil.getFloat(getString(key), defaultValue);
    }

    @Override
    public Double getDouble(String key, Double defaultValue) {
        return ValueUtil.getDouble(getString(key), defaultValue);
    }
}
