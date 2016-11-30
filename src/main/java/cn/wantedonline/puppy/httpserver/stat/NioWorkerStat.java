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

package cn.wantedonline.puppy.httpserver.stat;

import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <pre>
 *     NioWorker监控类
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/27.
 */
public class NioWorkerStat {
    private static List<NioEventLoop> workExecutors;
    private static boolean inited = false;
    private static final String headinfoFmt = "%-10s %-25s %-10s %-10s %-16s %-12s %-16s\n";
    private static final String threadinfoFmt = "%-10s %-25s %-10s %-11s";
    private static final String taskinfoFmt = "%-16s %-12s %-16s";

    private static String workerThreadStat(NioEventLoop worker) {
        StringBuilder tmp = new StringBuilder();
        if (AssertUtil.isNotNull(worker)) {
            ThreadProperties tp = worker.threadProperties();
            tmp.append(String.format(threadinfoFmt, tp.id(), tp.name(), tp.priority(), tp.state().name()));
        }
        return tmp.toString();
    }

    private static String workerTaskStat(NioEventLoop w) {
        StringBuilder tmp = new StringBuilder();
        if (AssertUtil.isNotNull(w)) {
            tmp.append(String.format(taskinfoFmt,w.pendingTasks(),w.getIoRatio(), NioWorkerStatus(w)));
        }
        return tmp.toString();
    }

    private static String NioWorkerStatus(NioEventLoop w) {
        return w.isShutdown() ? "ShutDown" : w.isShuttingDown() ? "ShuttingDown" : w.isTerminated() ? "Terminated" : "Running";
    }

    public static String statNioWorkers() {
        StringBuilder tmp = new StringBuilder();
        tmp.append(String.format(headinfoFmt, "Thread-id", "Thread-name", "Priority","Status","PendingTasks","I/ORatio", "EventLoopStatus"));
        if (inited) {
            for (NioEventLoop worker : workExecutors) {
                tmp.append(workerThreadStat(worker));
                tmp.append(workerTaskStat(worker)).append("\n");
            }
        } else {
            return "WorkerEventLoop Not Init yet...";
        }
        return tmp.toString();
    }

    public static void registerWorkers(NioEventLoopGroup eventLoopGroup) {
        Iterator<EventExecutor> iterator = eventLoopGroup.iterator();
        workExecutors = new ArrayList<>(eventLoopGroup.executorCount());
        while(iterator.hasNext()) {
            workExecutors.add((NioEventLoop) iterator.next());
        }
        inited = true;
    }

    private NioWorkerStat() {}
}
