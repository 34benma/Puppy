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

package cn.wantedonline.puppy.util.concurrent;

import java.util.Comparator;
import java.util.LinkedList;

/**
 * ArenaList is this type of list,
 * can set a fixed size(size > 0)
 * we need give a comparator to compare elements with each other
 * when reach the size, if we add newer element into it,
 * this list will remove the smallest element from the list
 * two goals must to achieve of this type of list:
 * 1> Thread-safe
 * 2> have a good performance
 *
 * @author wangcheng
 * @Date   2016-2-19
 *
 * @param <E>
 */
public class ArenaList<E> {
    /*the arena capacity*/
    private final int capacity;
    /*need user to define£¬int r = compare(e1, e2),when r > 0£¬e1 > e2,r = 0, e1 = e2, r < 0, e1 < e2*/
    private final Comparator<E> comparator;

    private final LinkedList<E> values;
    /*if there is an element in list e1, that e1 equal e2, which e2 is we want to add,
     * if keepNewerElement is false,we will drop e2,else drop e1*/
    private final boolean keepNewerElement;
    /*at this moment,the element count, when eCount < size, we can add new element into list and will not remove the last element*/
    private volatile int eCount = 0;

    public ArenaList(int capacity, Comparator<E> comparator) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("size can't be negative number");
        }

        this.capacity = capacity;
        this.comparator = comparator;
        this.keepNewerElement = false;
        this.values = new LinkedList<E>();
    }


    public ArenaList(int capacity, Comparator<E> comparator, boolean keepNewerElement) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("size can't be negative number");
        }

        this.capacity = capacity;
        this.comparator = comparator;
        this.keepNewerElement = keepNewerElement;
        this.values = new LinkedList<E>();
    }



    /**
     * add e into list with thread-safe
     * @param e
     */
    public void add(E e) {
        if (null == e) {
            throw new NullPointerException("Null Element.");
        }

        synchronized (values) {
            int index = findElementIndex(e);
            if (values.size() >= capacity) {
                if (keepNewerElement && index <= values.size()) {
                    values.removeLast();
                } else if (index < values.size()){
                    //e is in the middle of this list
                    values.removeLast();
                } else {
                    //e is at the end of this list and e equals the minElement, no need to add.
                    return;
                }
            }
            values.add(index, e);
            eCount++;
        }
    }

    /**
     * get the max size of this list
     * @return
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * get the real size at this moment of this list
     * size() <= getCapacity()
     * @return
     */
    public int size() {
        return values.size();
    }
    /**
     * remove e from list with thread-safe
     * @param e
     * @return
     */
    public boolean remove(E e) {
        if (null == e) {
            throw new NullPointerException("Null Element.");
        }
        synchronized (values) {
            if(values.remove(e)) {
                eCount--;
                return true;
            }
            return false;
        }
    }
    /**
     * remove the index element from list with thread safe
     * @param index
     * @return
     */
    public E remove(int index) {
        checkBounds(index);
        synchronized (values) {
            return values.remove(index);
        }
    }

    public E removeFirst() {
        synchronized (values) {
            return values.removeFirst();
        }
    }

    public E removeLast() {
        synchronized (values) {
            return values.removeLast();
        }
    }

    /**
     * get the largest element
     * @return
     */
    public E get() {
        return values.getFirst();
    }
    /**
     * get the least element
     * @return
     */
    public E getLast() {
        return values.getLast();
    }

    public E get(int index) {
        checkBounds(index);
        return values.get(index);
    }

    public boolean isEmpty() {
        return values.size() == 0;
    }

    public void printList() {
        System.out.print("[");
        for (int i = 0; i < values.size(); i++) {
            System.out.print(values.get(i));
            if (i < values.size() - 1) {
                System.out.print(", ");
            }
        }
        System.out.print("]");
        System.out.println();
    }


    //------------------------------Private Method----
    /**
     * compare two element
     * check comparator
     * @param e1
     * @param e2
     * @return
     */
    private int compareTwoElement(E e1, E e2) {
        if (null == comparator) {
            throw new NullPointerException("comparator can't be Null");
        }
        return comparator.compare(e1, e2);
    }

    private void checkBounds(int index) {
        if (index < 0 || index > values.size() - 1) {
            throw new IndexOutOfBoundsException("index:" + index + "not in [0," + values.size() + "]");
        }
    }
    /**
     * find the index of e should be
     * @param e
     * @return
     */
    private int findElementIndex(E e) {
        int index = 0;
        if (values.size() == 0) {
            return index;
        }
        //values.size() > 0
        E minElement = values.getLast();
        //the largest element at the index of 0
        int high = values.lastIndexOf(minElement);

        E maxElement = values.getFirst();
        //the least element at the index of values.size() - 1
        int low = values.indexOf(maxElement);

        if (compareTwoElement(minElement, e) >= 0) {
            //minElement >= e
            index = high + 1;
            return index;
        }

        if (compareTwoElement(maxElement, e) <= 0) {
            //maxElement <= e
            return index;
        }

        //only one element
        if (low == high) {
            if (capacity == 1) {
                return index;
            }
            if (compareTwoElement(minElement, e) > 0) {
                //minElement > e
                index = high + 1;
                return index;
            } else if (compareTwoElement(minElement, e) < 0) {
                //minElement < e
                return index;
            } else {
                //minElement = maxElement = e
                return index;
            }
        }

        //more than one element and e must at the middle of this list,use binary search to find index
        int middle = 0;
        while (low < high) {
            middle = (low + high) / 2;
            E minddleE = values.get(middle);
            if (compareTwoElement(minddleE, e) == 0) {
                //find it.
                index = middle;
                return index;
            }

            if (compareTwoElement(minddleE, e) > 0) {
                //middleE > e
                low = middle;
            } else {
                //middleE < e
                high = middle;
            }
            if (high - low == 1) {
                //if there only two element, e is in the middle of this list,low will never be higher or equal high
                //at this moment,should check out
                //[4,2,1],3 need to insert into it or [4,3,1],2 need to insert into it
                return low + 1;
            }

        }
        //at this moment,low >= high,and (middle + 1)is we should find
        index = middle + 1;
        return index;
    }

}
