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

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *     线程工厂类：创建指定命名前缀的线程
 * </pre>
 * @author louiswang
 */
public class NamedThreadFactory implements ThreadFactory {
    protected final ThreadGroup group;
    /**
     * 指定创建的线程名称前缀
     */
    protected final String namePrefix;
    /**
     * 是否为守护线程
     */
    protected boolean daemon = false;

    protected final AtomicInteger threadNum = new AtomicInteger(1);
    /**
     * 默认创建的线程优先级
     */
    protected int priority = Thread.NORM_PRIORITY;

    public NamedThreadFactory(String namePrefix) {
        SecurityManager s = System.getSecurityManager();
        group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
        this.namePrefix = namePrefix;
    }

    public NamedThreadFactory(String namePrefix, int priority) {
        this(namePrefix);
        this.priority = priority;
    }

    public NamedThreadFactory(String namePrefix, int priority, boolean daemon) {
        this(namePrefix);
        this.daemon = daemon;
        this.priority = priority;
    }

    @Override
    public Thread newThread(Runnable r) {
        Thread t = new Thread(group, r, namePrefix+threadNum.getAndIncrement(), 0);
        t.setDaemon(daemon);
        t.setPriority(priority);
        return t;
    }

    public String getNamePrefix() {
        return namePrefix;
    }
}
