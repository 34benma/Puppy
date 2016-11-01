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

package cn.wantedonline.puppy.httpserver.util.concurrent;

import java.io.Serializable;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 支持并发的HashSet 采用山寨办法实现的支持并发的HashSet
 */
public class ConcurrentHashSet<T> extends AbstractSet<T> implements Serializable {

    private static final long serialVersionUID = -354041681348976608L;
    /**
     * 将hashset中的对象放到map中
     */
    private Map<T, Boolean> map;
    /**
     * 集合的默认大小
     */
    private static final int DefaultCapacity = 16;

    /**
     * 默认构造方法
     */
    public ConcurrentHashSet() {
        this(DefaultCapacity);
    }

    /**
     * 指定集合大小的构造方法
     * 
     * @param initailCapacity
     */
    public ConcurrentHashSet(int initailCapacity) {
        map = new ConcurrentHashMap<T, Boolean>(initailCapacity);
    }

    /**
     * 获得迭代器
     */
    @Override
    public Iterator<T> iterator() {
        return map.keySet().iterator();
    }

    /**
     * 当前集合中元素的数量
     */
    @Override
    public int size() {
        return map.size();
    }

    /**
     * 当前集合中是否包含特定的对象
     */
    @Override
    public boolean contains(Object o) {
        return map.containsKey(o);
    }

    /**
     * 在集合中加入新的元素
     */
    @Override
    public boolean add(T o) {
        Boolean answer = ((ConcurrentHashMap<T, Boolean>) map).putIfAbsent(o, Boolean.TRUE);
        return answer == null;
    }

    /**
     * 删除集合中的元素
     */
    @Override
    public boolean remove(Object o) {
        return map.remove(o) != null;
    }

    /**
     * 清空集合中的所有元素
     */
    @Override
    public void clear() {
        map.clear();
    }
}
