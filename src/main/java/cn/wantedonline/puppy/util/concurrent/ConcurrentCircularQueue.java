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

package cn.wantedonline.puppy.util.concurrent;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
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
 *
 *  @author 迅雷 zengdong
 *  @author wangcheng
 *  @since V0.2.0 on 2016-12-01
 */
public class ConcurrentCircularQueue<E> implements List<E>, Serializable {

    /**
     * The size of the queue. There is always one unused element in the queue.
     */
    private int size;

    /**
     * The queue contents.
     */
    private Object[] elements;

    /**
     * The head index of the queue.
     */
    private AtomicInteger headIdx = new AtomicInteger(0);

    /**
     * The tailhead index of the queue.
     */
    private AtomicInteger tailIdx = new AtomicInteger(0);

    /**
     * 当在扩容的时候，增加、移除暂时不能进入。
     */
    private volatile boolean lock = false;



    /**
     * The minimum maxSize is 1. Creates a circularQueue with a initial size of 10.
     */
    public ConcurrentCircularQueue(int maxSize) {
        this(maxSize, maxSize);// 初始化最大容量
    }

    /**
     * Creates a CircularQueue with a initialSize and a maxSize. The queue is dynamicaly expanded until maxSize is reached. This behaivor is usefull for large queues that might not always get totaly
     * filled. For queues that are always filled it is adviced to set initialSize and maxSize to the same value. The minimum maxSize is 1.
     */
    public ConcurrentCircularQueue(int initialSize, int maxSize) {// private掉了~暂时不允许自定义初始容量
        // this is asserted
        if (maxSize < 1) {
            throw new RuntimeException("Min size of the CircularQueue is 1");
        }

        size = maxSize + 1;
        elements = new Object[initialSize + 1];
    }

    /**
     * Adds a object to the tail of the queue. If the queue is full a element from the head is dropped to free space.
     * 
     * @return dropObj
     */
    @SuppressWarnings("unchecked")
    public E addToTail(Object obj) {
        while (true) {
            while (lock) {// 当扩容的时候，等待一下下。
            }
            Object dropObj = null;
            if (isFull()) {// drop the head element
                dropObj = removeFromHead();
            }// 判断是否Full需要在while内,否则tailIdx可能会越过headIdx.
            ensureCapacity();
            int curIdx = tailIdx.get();
            int newIdx = nextIndex(curIdx);
            if (curIdx == tailIdx.get()) {// 是否被其它线程抢先修改值
                if (tailIdx.compareAndSet(curIdx, newIdx)) {// 如果修改成功的话，即返回。CAS原子操作
                    elements[curIdx] = obj;
                    return (E) dropObj;

                }// 不成功，重试
            }
        }
    }

    /**
     * Adds a object to the head of the queue. If the queue is full a element from the tail is dropped to free space. The dropped element is returned.
     */
    @SuppressWarnings("unchecked")
    public E addToHead(Object obj) {
        while (true) {
            while (lock) {// 当扩容的时候，等待一下下。
            }
            Object dropObj = null;
            if (isFull()) {// drop the tail element
                dropObj = removeFromTail();
            }
            ensureCapacity();
            int curIdx = headIdx.get();
            int newIdx = prevIndex(curIdx);
            if (curIdx == headIdx.get()) {// 是否被其它线程抢先修改值
                if (headIdx.compareAndSet(curIdx, newIdx)) {// 如果修改成功的话，即返回。CAS原子操作
                    elements[newIdx] = obj;
                    return (E) dropObj;
                }// 不成功，重试
            }
        }
    }

    /**
     * Removes and returns the element on the head of the queue
     * 
     * @throws NoSuchElementException if queue is empty.
     */
    @SuppressWarnings("unchecked")
    public E removeFromHead() throws NoSuchElementException {
        while (true) {
            while (lock) {// 当扩容的时候，等待一下下。
            }
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            int curIdx = headIdx.get();
            int newIdx = nextIndex(curIdx);
            if (curIdx == headIdx.get()) {// 是否被其它线程抢先修改值
                if (headIdx.compareAndSet(curIdx, newIdx)) {// 如果修改成功的话，即返回。CAS原子操作
                    Object obj = elements[curIdx];
                    elements[curIdx] = null;
                    return (E) obj;
                }// 不成功，重试，remove操作需要进行一次。
            }
        }
    }

    /**
     * Removes and returns the element on the tail of the queue
     * 
     * @throws NoSuchElementException if queue is empty.
     */
    @SuppressWarnings("unchecked")
    public E removeFromTail() throws NoSuchElementException {
        while (true) {
            while (lock) {// 当扩容的时候，等待一下下。
            }
            if (isEmpty()) {
                throw new NoSuchElementException();
            }
            int curIdx = tailIdx.get();
            int newIdx = prevIndex(curIdx);
            if (curIdx == tailIdx.get()) {// 是否被其它线程抢先修改值
                if (tailIdx.compareAndSet(curIdx, newIdx)) {// 如果修改成功的话，即返回。CAS原子操作
                    Object obj = elements[newIdx];
                    elements[newIdx] = null;
                    return (E) obj;
                }// 不成功，重试，remove操作需要进行一次。
            }
        }
    }

    /**
     * Maps the given index into the index in the internal array.
     */
    private int mapIndex(int index) throws IndexOutOfBoundsException {
        if (index >= elements.length || index < 0) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + elements.length);
        }
        return (index + headIdx.get()) % elements.length;
    }

    /**
     * Gets the next index for the given index.
     */
    private int nextIndex(int idx) {
        if (idx == elements.length - 1) {
            return 0;
        } else {
            return idx + 1;
        }
    }

    /**
     * Gets the previous index for the given index.
     */
    private int prevIndex(int idx) {
        if (idx == 0) {
            return elements.length - 1;
        } else {
            return idx - 1;
        }
    }

    /**
     * Clears the queue. Afterwards no elements from the queue can be accessed anymore.
     */
    @Override
    public void clear() {
        headIdx.set(0);
        tailIdx.set(0);

        // Let gc do its work
        for (int i = 0; i < elements.length; ++i) {
            elements[i] = null;
        }
    }

    /**
     * Returns the head element of the queue.
     * 
     * @throws NoSuchElementException if queue is empty.
     */
    @SuppressWarnings("unchecked")
    public E getFirst() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        return (E) elements[headIdx.get()];
    }

    /**
     * Returns the tail element of the queue.
     * 
     * @throws NoSuchElementException if queue is empty.
     */
    @SuppressWarnings("unchecked")
    public E getLast() throws NoSuchElementException {
        if (isEmpty()) {
            throw new NoSuchElementException();
        }
        // adjust last index...
        int index = prevIndex(tailIdx.get());
        return (E) elements[index];
    }

    /**
     * Returns the element at index in the queue.
     * 
     * @throws IndexOutOfBoundsException if index is out of range
     */
    @Override
    @SuppressWarnings("unchecked")
    public E get(int index) throws IndexOutOfBoundsException {
        int idx = mapIndex(index);
        return (E) elements[idx];
    }

    /**
     * Returns the number of elements in the queue.
     */
    public int getSize() {
        if (headIdx.get() <= tailIdx.get()) {
            // H T
            // [ |x|x|x| | ]
            // 0 1 2 3 4 5
            return tailIdx.get() - headIdx.get();
        } else {
            // T H
            // [x| | | |x|x]
            // 0 1 2 3 4 5
            return elements.length - headIdx.get() + tailIdx.get();
        }
    }

    /**
     * Returns the maximum number of elements this queue can hold.
     */
    public int getCapacity() {
        return size - 1;
    }

    /**
     * Returns true is the queue is empty.
     */
    @Override
    public boolean isEmpty() {
        return headIdx.get() == tailIdx.get();
    }

    /**
     * Returns true if the queue is full.
     */
    public boolean isFull() {
        if (elements.length == size) {// the queue is fully expanded
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

        if (this.size == newSize) {// 如果数量没变化的话,那就不折腾了
            return;
        }

        // boolean shrink = newSize < size; // 2012-10-13 原来的应该判断是否shrink判断错了，要判断真实的elements的size
        boolean shrink = newSize < elements.length;
        if (shrink) {
            synchronized (this) {
                lock = true;// 锁上，其它插入、移除操作不可进入

                Object[] newElements = new Object[newSize];
                int newHeadIdx = tailIdx.get() - maxSize;
                if (newHeadIdx >= 0) {
                    System.arraycopy(elements, newHeadIdx, newElements, 0, newSize);
                    this.headIdx.set(headIdx.get() > tailIdx.get() ? 0 : headIdx.get());
                } else {
                    System.arraycopy(elements, 0, newElements, newSize - (tailIdx.get() + 1), tailIdx.get() + 1);
                    System.arraycopy(elements, elements.length + newHeadIdx, newElements, 0, maxSize - tailIdx.get());
                    this.headIdx.set(headIdx.get() > tailIdx.get() ? 0 : maxSize - tailIdx.get() + headIdx.get());
                }
                this.tailIdx.set(maxSize);
                this.size = newSize;
                this.elements = newElements;

                lock = false;// 释放锁
            }
        } else {
            this.size = newSize;
            ensureCapacity();

        }
    }

    private void ensureCapacity() {
        if (elements.length == size) {// 已扩容至最大限制
            return;
        }
        if (nextIndex(tailIdx.get()) != headIdx.get()) {
            return;
        }
        synchronized (this) {
            // 这里可以使用synchronized,不会造成严重的阻塞.
            // 因为System.arraycopy效率很高!复制36万个才花费1ms,2千万个才92ms!! by CPU:G630 2.7GHZ

            if (elements.length == size) {// 检查是不是上一个线程已经扩容过了?
                return;
            }
            if (nextIndex(tailIdx.get()) != headIdx.get()) {
                return;
            }

            lock = true;// 锁上，其它插入、移除操作不可进入

            // expand array and copy over
            int newSize = Math.min(elements.length * 2, size);
            Object[] newElements = new Object[newSize];
            if (headIdx.get() <= tailIdx.get()) {
                /**
                 * <pre>
                 *    H     T
                 * [ |x|x|x| | ]
                 *  0 1 2 3 4 5
                 * </pre>
                 */
                System.arraycopy(elements, headIdx.get(), newElements, headIdx.get(), tailIdx.get() - headIdx.get());
            } else {
                /**
                 * <pre>
                 *    T     H
                 * [x| | | |x|x]
                 *  0 1 2 3 4 5
                 * </pre>
                 */
                int newHeadIdx = newSize - (elements.length - headIdx.get());
                if (tailIdx.get() > 0) {
                    System.arraycopy(elements, 0, newElements, 0, tailIdx.get());
                }
                System.arraycopy(elements, headIdx.get(), newElements, newHeadIdx, elements.length - headIdx.get());
                headIdx.set(newHeadIdx);
            }
            elements = newElements;

            lock = false;// 释放锁
        }
    }

    /*
     * private void logQueue() { System.out.println( "-------------------------------" ); System.out.println( headIdx + " " + tailIdx + " " + getSize() ); System.out.print( "[ " ); for( int i = 0; i <
     * elements.length; i++ ) { System.out.print( elements[i] + " | " ); } System.out.println( " ]" ); System.out.println( "-------------------------------" ); }
     */

    /**
     * 通过复制一份出来遍历,解决并发修改与遍历的问题
     */
    private class CircularQueueIterator implements Iterator<E> {

        /**
         * originalTail
         */
        int originalTail;

        /**
         * Next element index.
         */
        int nextElement;

        /**
         * The queue contents.
         */
        private Object[] originalElements;

        public CircularQueueIterator() {
            nextElement = headIdx.get();
            originalTail = tailIdx.get();
            originalElements = new Object[elements.length];
            System.arraycopy(elements, 0, originalElements, 0, elements.length);// copy one for iterator.
        }

        @Override
        public boolean hasNext() {
            return nextElement != originalTail;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E next() throws NoSuchElementException {
            if (nextElement == originalTail) {
                throw new NoSuchElementException();
            }

            Object obj = originalElements[nextElement];
            originalElements[nextElement] = null;
            nextElement = nextIndex(nextElement);
            return (E) obj;
        }

        /**
         * This operation is not supported.
         */
        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        /**
         * Gets the next index for the given index.
         */
        private int nextIndex(int idx) {
            if (idx == originalElements.length - 1) {
                return 0;
            } else {
                return idx + 1;
            }
        }
    }

    /** the unImplement method for List,not sure for concurrent! */
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
