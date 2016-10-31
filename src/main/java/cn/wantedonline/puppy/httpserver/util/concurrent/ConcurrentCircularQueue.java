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

/**
 * Created by wangcheng on 2016/10/31.
 */

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 * This is a fast double-ended circular queue.
 *
 * 抄自：
 * http://www.codeforge.cn/read/178902/CircularQueue.java__html
 * http://www.koders.com/java/fid0C8B64975D8AF4E5926D47A3FDA350ED3A5C2286.aspx
 * phex_2.1.4.80源码，phex是一款使用java编写得，基于Gnutella协议的p2p软件
 *
 * 修改：
 * 1.增加了泛型支持
 * 2.通过setMaxSize() 可以修改Queue Size,内部发现需要收缩则进行shrink
 *
 * 还可参考：
 * 顺序队列(sequence queue)
 * http://blog.csdn.net/acb0y/article/details/5671395
 * http://sjjp.tjuci.edu.cn/sjjg/datastructure/ds/web/zhanhuoduilie/zhanhuoduilie3.2.2.1.htm
 *
 * Java非阻塞算法简介
 * http://www.ibm.com/developerworks/cn/java/j-jtp04186/
 * Java 理论与实践: 流行的原子
 * https://www.ibm.com/developerworks/cn/java/j-jtp11234/
 *
 * 并发解决原理:关键在于headIdx和tailIdx是唯一读取修改,只要保证不让两个线程对同一个idx同时进行修改,即能解决并发问题,
 * 解决方法:使用了CPU支持的CAS原子操作.让CAS操作不成功则重试,总会成功一次的且只能成功一次.
 *
 * Bug:
 * 0.只能单向使用，顺时针或逆时针例如addToTail和removeFromHead配套使用，但不能addToTail和addToHead混合使用！[因为两个指针同时移动会导致是否已满判断出错!!]
 * 1.不能使用自动扩容功能,否则会扩容与插入会产生数据丢失,暂时没有办法修正.
 *
 * TODO:
 * 1.尝试引入信号量,解决插入和扩容的问题
 */
public class ConcurrentCircularQueue<E> implements List<E> {
    private int size;
    private Object[] elements;
    private AtomicInteger headIdx = new AtomicInteger(0);
    private AtomicInteger tailIdx = new AtomicInteger(0);
    private volatile boolean lock = false;

    public ConcurrentCircularQueue(int maxSize) {
        this(maxSize, maxSize);
    }

    private ConcurrentCircularQueue(int initalSize, int maxSize) {
        if (maxSize < 1) {
            throw new RuntimeException("Min size of the CircularQueue is 1");
        }

        size = maxSize + 1;
        elements = new Object[initalSize+1];
    }

    public E addToTail(Object obj) {
        while (true) {
            while (lock){
                //扩容，等待
            }
            Object dropObj = null;
            if (isFull()) {
                dropObj = removeFromHead();
            }
            ensureCapacity();
            int curIdx = tailIdx.get();
            int newIdx = nextIndex(curIdx);
            if (curIdx == tailIdx.get()) {
                if (tailIdx.compareAndSet(curIdx, newIdx)) {
                    elements[curIdx] = obj;
                    return (E) dropObj;
                }
            }
        }
    }

    public E addToHead(Object obj) {
        while (true) {
            while (lock) {
                //扩容，等待
            }
            Object dropObj = null;
            if (isFull()) {
                dropObj = removeFromTail();
            }
            ensureCapacity();
            int curIdx = headIdx.get();
            int newIdx = preIndex(curIdx);
            if (curIdx == headIdx.get()) {
                if (headIdx.compareAndSet(curIdx, newIdx)) {
                    elements[newIdx] = obj;
                    return (E) dropObj;
                }
            }
        }
    }

    public E removeFromHead() throws NoSuchElementException {
        while (true) {
            while (lock) {}
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            int curIdx = headIdx.get();
            int newIdx = nextIndex(curIdx);
            if (curIdx == headIdx.get()) {
                if (headIdx.compareAndSet(curIdx, newIdx)) {
                    Object obj = elements[curIdx];
                    elements[curIdx] = null;
                    return (E)obj;
                }
            }
        }
    }

    public E removeFromTail() throws NoSuchElementException {
        while (true) {
            while (lock) {}
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            int curIdx = tailIdx.get();
            int newIdx = preIndex(curIdx);
            if (curIdx == tailIdx.get()) {
                if (tailIdx.compareAndSet(curIdx, newIdx)) {
                    Object obj = elements[newIdx];
                    elements[newIdx] = null;
                    return (E)obj;
                }
            }
        }
    }

    private int mapIndex(int index) throws IndexOutOfBoundsException {
        if (index >= elements.length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index +", Size: " + elements.length);
        }
        return (index + headIdx.get()) % elements.length;
    }

    private int nextIndex(int idx) {
        if (idx == elements.length-1) {
            return 0;
        } else {
            return idx + 1;
        }
    }

    private int preIndex(int idx) {
        if (idx == 0) {
            return elements.length - 1;
        } else {
            return idx -1;
        }
    }

    @Override
    public void clear() {
        headIdx.set(0);
        tailIdx.set(0);

        for (int i = 0; i < elements.length; i++) {
            elements[i] = null;
        }
    }

    public E getFirst() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) elements[headIdx.get()];
    }

    public E getLast() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        int index = preIndex(tailIdx.get());
        return (E) elements[index];
    }

    @Override
    public E get(int index) {
        int idx = mapIndex(index);
        return (E) elements[idx];
    }

    public int getSize() {
        if (headIdx.get() <= tailIdx.get()) {
            return tailIdx.get() - headIdx.get();
        } else {
            return elements.length - headIdx.get() + tailIdx.get();
        }
    }

    public int getCapacity() {
        return size - 1;
    }

    @Override
    public boolean isEmpty() {
        return headIdx.get() == tailIdx.get();
    }

    public boolean isFull() {
        if (elements.length == size) {
            return nextIndex(tailIdx.get()) == headIdx.get();
        }
        return false;
    }

    @Override
    public Iterator<E> iterator() {
        return new CircularQueueIterator();
    }

    public void setMaxSize(int maxSize) {
        int newSize = maxSize + 1;
        if (this.size == newSize) {
            return;
        }
        boolean shrink = newSize < elements.length;
        if (shrink) {
            synchronized (this) {
                lock = true;
                Object[] newElements = new Object[newSize];
                int newHeadIdx = tailIdx.get() - maxSize;
                if (newHeadIdx >= 0) {
                    System.arraycopy(elements, newHeadIdx, newElements, 0, newSize);
                    this.headIdx.set(headIdx.get() > tailIdx.get() ? 0 : headIdx.get());
                } else {
                    System.arraycopy(elements, 0, newElements, newSize - (tailIdx.get() + 1), tailIdx.get() + 1);
                    System.arraycopy(elements, elements.length+newHeadIdx, newElements, 0, maxSize - tailIdx.get());
                    this.headIdx.set(headIdx.get() > tailIdx.get() ? 0 : maxSize - tailIdx.get() + headIdx.get());
                }
                this.tailIdx.set(maxSize);
                this.size = newSize;
                this.elements = newElements;

                lock = false;
            }
        } else {
            this.size = newSize;
            ensureCapacity();
        }
    }

    private void ensureCapacity() {
        if (elements.length == size) {
            return;
        }
        if (nextIndex(tailIdx.get()) != headIdx.get()) {
            return;
        }
        synchronized (this) {
            if (elements.length == size) {
                return;
            }
            if (nextIndex(tailIdx.get()) != headIdx.get()) {
                return;
            }
            lock = true;
            int newSize = Math.min(elements.length*2, size);
            Object[] newElements = new Object[newSize];
            if (headIdx.get() <= tailIdx.get()) {
                System.arraycopy(elements, headIdx.get(), newElements, headIdx.get(), tailIdx.get() - headIdx.get());
            } else {
                int newHeadIdx = newSize - (elements.length - headIdx.get());
                if (tailIdx.get() > 0) {
                    System.arraycopy(elements, 0, newElements, 0, tailIdx.get());
                }
                System.arraycopy(elements, headIdx.get(), newElements, newHeadIdx, elements.length - headIdx.get());
                headIdx.set(newHeadIdx);
            }
            elements = newElements;
            lock = false;
        }
    }

    private class CircularQueueIterator implements Iterator<E> {
        int originalTail;
        int nextElement;
        private Object[] originalElements;

        public CircularQueueIterator() {
            nextElement = headIdx.get();
            originalTail = tailIdx.get();
            originalElements = new Object[elements.length];
            System.arraycopy(elements, 0, originalElements, 0, elements.length);
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        private int nextIndex(int idx) {
            if (idx == originalElements.length - 1) {
                return 0;
            } else {
                return idx + 1;
            }
        }

        @Override
        public boolean hasNext() {
            return nextElement != originalTail;
        }

        @Override
        public E next() throws NoSuchElementException {
            if (nextElement == originalTail) {
                throw new NoSuchElementException();
            }

            Object obj = originalElements[nextElement];
            originalElements[nextElement] = null;
            nextElement = nextIndex(nextElement);
            return (E) obj;
        }
    }

    @Override
    public int size() {
        return getSize();
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Object[] toArray() {
        Object[] copyElements = new Object[getSize()];
        Iterator<E> it = iterator();
        int i = 0;
        while (it.hasNext() && i < copyElements.length) {
            copyElements[i] = it.next();
        }
        return copyElements;
    }

    @Override
    public <T> T[] toArray(T[] a) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(E e) {
        addToTail(e);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        Iterator<?> e = c.iterator();
        while (e.hasNext()) {
            if (!contains(e.next())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        Iterator<? extends E> it = c.iterator();
        while (it.hasNext()) {
            add(it.next());
        }
        return true;
    }

    @Override
    public boolean addAll(int index, Collection<? extends E> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @SuppressWarnings("unchecked")
    @Override
    public E set(int index, E element) {
        int idx = mapIndex(index);
        Object obj = elements[idx];
        elements[idx] = element;
        return (E) obj;
    }

    @Override
    public void add(int index, E element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public E remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        int index = headIdx.get();
        if (o == null) {
            for (int i = 0; i < getSize(); i++) {
                if (elements[index] == null) {
                    return i;
                }
                index = nextIndex(index);
            }
        } else {
            for (int i = 0; i < size; i++) {
                if (o.equals(elements[i])) {
                    return i;
                }
                index = nextIndex(index);
            }
        }
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        Object[] elementData = toArray();
        if (o == null) {
            for (int i = elementData.length - 1; i >= 0; i--) {
                if (elementData[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = elementData.length - 1; i >= 0; i--) {
                if (o.equals(elementData[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public ListIterator<E> listIterator() {
        throw new UnsupportedOperationException();
    }

    @Override
    public ListIterator<E> listIterator(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public List<E> subList(int fromIndex, int toIndex) {
        throw new UnsupportedOperationException();
    }
}
