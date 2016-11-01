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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程工厂类，提供创建线程的功能
 */
public class NamedThreadFactory implements ThreadFactory {

    /**
     * 线程组
     */
    protected final ThreadGroup group;
    /**
     * 保证原子操作的整数
     */
    protected final AtomicInteger threadNumber = new AtomicInteger(1);
    /**
     * 名字前缀
     */
    protected final String namePrefix;
    /**
     * 默认优先级
     */
    protected int priority = Thread.NORM_PRIORITY;
    /**
     * 是否为守护线程
     */
    protected boolean daemon = false;

    /**
     * 构造方法
     * 
     * @param namePrefix
     * @param priority
     * @param daemon
     */
    public NamedThreadFactory(String namePrefix, int priority, boolean daemon) {
        this(namePrefix);
        this.daemon = daemon;
        this.priority = priority;
    }

    /**
     * 构造方法
     * 
     * @param namePrefix
     * @param priority
     */
    public NamedThreadFactory(String namePrefix, int priority) {
        this(namePrefix);
        this.priority = priority;
    }

    /**
     * 构造方法
     * 
     * @param namePrefix
     */
    public NamedThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    /**
     * 创建一个新的线程
     */
    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
