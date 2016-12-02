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

import cn.wantedonline.puppy.httpserver.common.HttpServerConfig;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.DateStringUtil;
import io.netty.channel.nio.NioEventLoop;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.ThreadProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
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
@Component
public class NioWorkerStat {
    private List<NioEventLoop> workExecutors;
    private boolean inited = false;
    private final String headinfoFmt = "%-10s %-25s %-10s %-10s %-16s %-12s %-16s\n";
    private final String threadinfoFmt = "%-10s %-25s %-10s %-11s";
    private final String taskinfoFmt = "%-16s %-12s %-16s";

    public NioWorkerStatSnapshot tickNioWorkerStatSnapshot() {
        NioWorkerStatSnapshot snapshot = new NioWorkerStatSnapshot();
        snapshot.setNioWorkerStatusBeanList(workExecutors);
        return snapshot;
    }

    public class NioWorkerStatSnapshot {
        private Date date;
        private List<NioWorkerStatusBean> statusBeenList = new ArrayList<>(HttpServerConfig.PROCESSOR_NUM*2);

        public NioWorkerStatSnapshot() {
            this.date = new Date();
        }

        public void setNioWorkerStatusBeanList(List<NioEventLoop> workExecutors) {
            if (inited) {
                for (NioEventLoop worker : workExecutors) {
                    NioWorkerStatusBean bean = new NioWorkerStatusBean();
                    ThreadProperties tp = worker.threadProperties();
                    bean.setThreadId(tp.id());
                    bean.setThreadName(tp.name());
                    bean.setThreadPriority(tp.priority());
                    bean.setThreadStatus(tp.state());
                    bean.setEventLoopStatus(NioWorkerStatus(worker));
                    bean.setI_O_ratio(worker.getIoRatio());
                    bean.setPendingTasks(worker.pendingTasks());
                    statusBeenList.add(bean);
                }
            }
        }

        @Override
        public String toString() {
            return "NioWorkerStatSnapshot{" +
                    "date=" + DateStringUtil.DEFAULT.format(date) +
                    ", statusBeenList=" + statusBeenList +
                    '}';
        }
    }

    public class NioWorkerStatusBean {
        private long threadId;
        private String threadName;
        private int threadPriority;
        private Thread.State threadStatus;
        private int pendingTasks;
        private int I_O_ratio;
        private String EventLoopStatus;

        public long getThreadId() {
            return threadId;
        }

        public void setThreadId(long threadId) {
            this.threadId = threadId;
        }

        public String getThreadName() {
            return threadName;
        }

        public void setThreadName(String threadName) {
            this.threadName = threadName;
        }

        public int getThreadPriority() {
            return threadPriority;
        }

        public void setThreadPriority(int threadPriority) {
            this.threadPriority = threadPriority;
        }

        public Thread.State getThreadStatus() {
            return threadStatus;
        }

        public void setThreadStatus(Thread.State threadStatus) {
            this.threadStatus = threadStatus;
        }

        public int getPendingTasks() {
            return pendingTasks;
        }

        public void setPendingTasks(int pendingTasks) {
            this.pendingTasks = pendingTasks;
        }

        public int getI_O_ratio() {
            return I_O_ratio;
        }

        public void setI_O_ratio(int i_O_ratio) {
            I_O_ratio = i_O_ratio;
        }

        public String getEventLoopStatus() {
            return EventLoopStatus;
        }

        public void setEventLoopStatus(String eventLoopStatus) {
            EventLoopStatus = eventLoopStatus;
        }

        @Override
        public String toString() {
            return "NioWorkerStatusBean{" +
                    "threadId=" + threadId +
                    ", threadName='" + threadName + '\'' +
                    ", threadPriority=" + threadPriority +
                    ", threadStatus=" + threadStatus +
                    ", pendingTasks=" + pendingTasks +
                    ", I_O_ratio=" + I_O_ratio +
                    ", EventLoopStatus='" + EventLoopStatus + '\'' +
                    '}';
        }
    }

    private String workerThreadStat(NioEventLoop worker) {
        StringBuilder tmp = new StringBuilder();
        if (AssertUtil.isNotNull(worker)) {
            ThreadProperties tp = worker.threadProperties();
            tmp.append(String.format(threadinfoFmt, tp.id(), tp.name(), tp.priority(), tp.state().name()));
        }
        return tmp.toString();
    }

    private String workerTaskStat(NioEventLoop w) {
        StringBuilder tmp = new StringBuilder();
        if (AssertUtil.isNotNull(w)) {
            tmp.append(String.format(taskinfoFmt,w.pendingTasks(),w.getIoRatio(), NioWorkerStatus(w)));
        }
        return tmp.toString();
    }

    private static String NioWorkerStatus(NioEventLoop w) {
        return w.isShutdown() ? "ShutDown" : w.isShuttingDown() ? "ShuttingDown" : w.isTerminated() ? "Terminated" : "Running";
    }

    public String statNioWorkers() {
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

    public void registerWorkers(NioEventLoopGroup eventLoopGroup) {
        Iterator<EventExecutor> iterator = eventLoopGroup.iterator();
        workExecutors = new ArrayList<>(eventLoopGroup.executorCount());
        while(iterator.hasNext()) {
            workExecutors.add((NioEventLoop) iterator.next());
        }
        inited = true;
    }

    private NioWorkerStat() {}
}
