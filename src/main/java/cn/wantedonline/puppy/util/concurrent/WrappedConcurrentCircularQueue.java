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
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <pre>
 *     包装的ConcurrentCircularQueue,提供额外的两种功能
 *     1 可以判断是否在写数据
 *     2 可以禁止外部写入数据
 *     3 可以不指定maxSize,默认InitSize是16，maxSize是Integer.MAX_VALUE - 2
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/12/2.
 */
public class WrappedConcurrentCircularQueue<E> extends ConcurrentCircularQueue<E> implements Serializable{
    private static final int defalutMaxSize = Integer.MAX_VALUE - 2;
    /**
     * 当正在进行写操作的时候，容器数据正在变化，这时统计是不准确的
     */
    private AtomicBoolean isWriting = new AtomicBoolean();

    /**
     * 当禁止写数据时，调用写方法无效，要写入的数据直接丢弃并且返回null
     */
    private volatile boolean prohibitWriting = false;

    public WrappedConcurrentCircularQueue() {
        super(15, defalutMaxSize);
    }

    public WrappedConcurrentCircularQueue(int maxSize) {
        super(maxSize);
    }

    @Override
    public E addToTail(Object obj) {
        if (prohibitWriting) {
            return null;
        }
        while (!isWriting.compareAndSet(false, true));
        E rtn = super.addToTail(obj);
        while (!isWriting.compareAndSet(true, false));
        return rtn;
    }

    @Override
    public E addToHead(Object obj) {
        if (prohibitWriting) {
            return null;
        }
        while (!isWriting.compareAndSet(false, true));
        E rtn = super.addToHead(obj);
        while (!isWriting.compareAndSet(true, false));
        return rtn;
    }

    @Override
    public E removeFromHead() throws NoSuchElementException {
        if (prohibitWriting) {
            return null;
        }
        while (!isWriting.compareAndSet(false, true));
        E rtn = super.removeFromHead();
        while (!isWriting.compareAndSet(true, false));
        return rtn;
    }

    @Override
    public E removeFromTail() throws NoSuchElementException {
        if (prohibitWriting) {
            return null;
        }
        while (!isWriting.compareAndSet(false, true));
        E rtn = super.removeFromTail();
        while (!isWriting.compareAndSet(true, false));
        return rtn;
    }

    public boolean isWriting() {
        return isWriting.get();
    }

    public void prohibitWrite() {
        prohibitWriting = true;
    }

    public void setCanWrite() {
        prohibitWriting = false;
    }
}
