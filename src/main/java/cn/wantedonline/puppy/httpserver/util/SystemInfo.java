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

package cn.wantedonline.puppy.httpserver.util;

import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.HttpUtil;
import cn.wantedonline.puppy.util.StringTools;
import cn.wantedonline.puppy.util.ValueUtil;
import com.sun.management.GcInfo;

import java.lang.management.*;
import java.util.*;

/**
 * Created by louiswang on 16/10/31.
 */
public class SystemInfo {
    private static String serverStartupTime;

    public static final String LAUNCHER_NAME;
    public static final int PID;
    public static final String HOSTNAME;
    public static final String COMMAND_FULL;
    public static final String COMMAND;
    public static final String COMMAND_SHORT;

    static {
        String sunjavacommand = System.getProperty("sun.java.command");
        String command_full = "";
        String command_short = "";
        String command = "";
        if (StringTools.isNotEmpty(sunjavacommand)) {
            command_full = sunjavacommand;
            command = command_full.split(" ")[0];
            command_short = command.substring(command.lastIndexOf('.') + 1);
        }
        COMMAND_FULL = command_full;
        COMMAND = command;
        COMMAND_SHORT = command_short;

        String launcher_name = ManagementFactory.getRuntimeMXBean().getName();
        command_short = !"Launch".equals(command_short) && command_short.endsWith("Launch") ? command_short.substring(0, command_short.length() - "Launch".length()) : command_short;
        String hostName = "UNKNOWN";
        int pid = -1;
        String ip = HttpUtil.getLocalSampleIP();
        String pidAtHostName = ManagementFactory.getRuntimeMXBean().getName();
        int idx = pidAtHostName.indexOf('@');
        if (idx > 0) {
            pid = ValueUtil.getInteger(pidAtHostName.substring(0, idx), pid);
            hostName = pidAtHostName.substring(idx + 1);
        }
        String first = command_short;
        String second = ip.startsWith("192.168.") || ip.startsWith("10.10.") || ip.startsWith("10.11.") ? ip : hostName;
        launcher_name = first + "@" + second;
        PID = pid;
        HOSTNAME = hostName;
        LAUNCHER_NAME = launcher_name;
    }

    public static String getCommand(String sunjavacommand, boolean getShort) {
        String command_full = "";
        String command_short = "";
        String command = "";
        if (StringTools.isNotEmpty(sunjavacommand)) {
            command_full = sunjavacommand;
            command = command_full.split(" ")[0];
            command_short = command.substring(command.lastIndexOf('.') + 1);
        }
        return getShort ? command_short : command;
    }

    private static String formatMemoryUsage(MemoryUsage mem) {
        String fmt = "初始化:%-10s 已使用:%-10s 最大:%-10s";
        return String.format(fmt, HumanReadableUtil.byteSize(mem.getInit()), HumanReadableUtil.byteSize(mem.getUsed()), HumanReadableUtil.byteSize(mem.getMax()));
    }

    private static String getContrastString(long before, long after) {
        long sub = after - before;
        if (sub > 0) {
            return "↑" + HumanReadableUtil.byteSize(after);
        } else if (sub < 0) {
            return "↓" + HumanReadableUtil.byteSize(after);
        }
        return "";
    }

    public static StringBuilder getGarbageCollectorInfo(StringBuilder tmp) {
        List<GarbageCollectorMXBean> gcs = ManagementFactory.getGarbageCollectorMXBeans();
        tmp.append("GarbageCollectorMXBeans\n");
        String gcFmt = "%-23s %-8s %-20s %s\n";
        tmp.append(String.format(gcFmt, "GCName", "Count", "Time", "MemoryPoolNames"));
        Set<String> poolNames = new LinkedHashSet<String>();
        for (GarbageCollectorMXBean gc : gcs) {
            tmp.append(String.format(gcFmt, gc.getName(), gc.getCollectionCount(), HumanReadableUtil.timeSpan(gc.getCollectionTime()), Arrays.toString(gc.getMemoryPoolNames())));
            for (String n : gc.getMemoryPoolNames()) {
                poolNames.add(n);
            }
        }
        gcFmt = "%-23s %-5s %-20s %-20s %s\n";
        long serverStartTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        tmp.append(String.format(gcFmt, "+ LastGcInfo", "Id", "StartTime", "EndTime", "Duration"));
        for (GarbageCollectorMXBean gc : gcs) {
            if (gc instanceof com.sun.management.GarbageCollectorMXBean) {
                com.sun.management.GarbageCollectorMXBean g = (com.sun.management.GarbageCollectorMXBean) gc;
                GcInfo gi = g.getLastGcInfo();
                if (gi == null || gi.getStartTime() == 0) {
                    continue;
                }
                DateStringUtil dsu = DateStringUtil.getInstance("HH:mm:ss.S");
                String start = dsu.format(new Date(serverStartTime + gi.getStartTime()));
                String end = dsu.format(new Date(serverStartTime + gi.getEndTime()));
                tmp.append(String.format(gcFmt, "  " + gc.getName(), gi.getId(), start, end, HumanReadableUtil.timeSpan(gi.getDuration())));
                Map<String, MemoryUsage> before = gi.getMemoryUsageBeforeGc();
                Map<String, MemoryUsage> after = gi.getMemoryUsageAfterGc();
                String muFmt = "%-23s %-30s %-14s %-14s %-14s %-14s\n";
                tmp.append(String.format(muFmt, "", "poolName", "init", "used", "committed", "max"));
                for (String name : poolNames) {
                    MemoryUsage mu = before.get(name);
                    MemoryUsage mu1 = after.get(name);
                    if (mu == null) {
                        continue;
                    }
                    tmp.append(String.format(muFmt, "", name, HumanReadableUtil.byteSize(mu.getInit()), HumanReadableUtil.byteSize(mu.getUsed()), HumanReadableUtil.byteSize(mu.getCommitted()),
                            HumanReadableUtil.byteSize(mu.getMax())));
                    String init = getContrastString(mu.getInit(), mu1.getInit());
                    String used = getContrastString(mu.getUsed(), mu1.getUsed());
                    String committted = getContrastString(mu.getCommitted(), mu1.getCommitted());
                    String max = getContrastString(mu.getMax(), mu1.getMax());
                    tmp.append(String.format(muFmt, "", "", init, used, committted, max));
                }
            }
        }
        return tmp;
    }

    public static StringBuilder getMemoryInfo(StringBuilder tmp) {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        tmp.append("\nmemoryMXBean\n");
        tmp.append("已使用内存:\t\t").append(HumanReadableUtil.byteSize(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())).append("\n");
        tmp.append("已分配内存:\t\t").append(HumanReadableUtil.byteSize(Runtime.getRuntime().totalMemory())).append("\n");
        tmp.append("最大内存:\t\t").append(HumanReadableUtil.byteSize(Runtime.getRuntime().maxMemory())).append("\n");
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        tmp.append("堆内存:\t\t\t").append(formatMemoryUsage(memoryMXBean.getHeapMemoryUsage())).append("\n");
        tmp.append("非堆内存:\t\t").append(formatMemoryUsage(memoryMXBean.getNonHeapMemoryUsage())).append("\n");
        tmp.append("待回收对象数:\t\t").append(memoryMXBean.getObjectPendingFinalizationCount()).append("\n");
        tmp.append("\n");
        return tmp;
    }

    public static StringBuilder getMemoryAndGcInfo(StringBuilder tmp) {
        return getGarbageCollectorInfo(getMemoryInfo(tmp));
    }

    public static String getServerStartupTime() {
        if (serverStartupTime == null) {
            serverStartupTime = DateStringUtil.DEFAULT.format(new Date(ManagementFactory.getRuntimeMXBean().getStartTime()));
        }
        return serverStartupTime;
    }

    public static String getSytemInfo() {
        StringBuilder tmp = new StringBuilder();
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        tmp.append("runtimeMXBean\n");
        tmp.append("启动时间:\t\t").append(getServerStartupTime()).append("\n");
        tmp.append("已运行:\t\t\t").append(HumanReadableUtil.timeSpan(runtimeMXBean.getUptime())).append("\n");
        tmp.append("进程:\t\t\t").append(runtimeMXBean.getName()).append("\n");
        tmp.append("名称:\t\t\t").append(LAUNCHER_NAME).append("\n");
        tmp.append("虚拟机:\t\t\t").append(runtimeMXBean.getVmName()).append(" ").append(runtimeMXBean.getVmVersion()).append("\n");

        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        tmp.append("\n");
        tmp.append("ThreadMXBean\n");
        tmp.append("当前线程数:\t\t").append(threadMXBean.getThreadCount()).append("\n");
        tmp.append("后台线程数:\t\t").append(threadMXBean.getDaemonThreadCount()).append("\n");
        tmp.append("峰值线程数:\t\t").append(threadMXBean.getPeakThreadCount()).append("\n");
        tmp.append("已启动线程数:\t\t").append(threadMXBean.getTotalStartedThreadCount()).append("\n");
        tmp.append("\n");

        getMemoryInfo(tmp);
        getGarbageCollectorInfo(tmp);

        OperatingSystemMXBean operatingSystemMXBean = ManagementFactory.getOperatingSystemMXBean();
        tmp.append("\n");
        tmp.append("operatingSystemMXBean\n");
        tmp.append("操作系统:\t\t").append(operatingSystemMXBean.getName()).append(" ").append(operatingSystemMXBean.getVersion()).append("\n");
        tmp.append("体系结构:\t\t").append(operatingSystemMXBean.getArch()).append("\n");
        tmp.append("处理器个数:\t\t").append(operatingSystemMXBean.getAvailableProcessors()).append("\n");
        return tmp.toString();
    }

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private static final Comparator<ThreadInfo> allCpuTimeComparator = new Comparator<ThreadInfo>() {

        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2) {
            long id1 = o1.getThreadId();
            long id2 = o2.getThreadId();
            int r = (int) (threadMXBean.getThreadCpuTime(id2) - threadMXBean.getThreadCpuTime(id1));
            return r == 0 ? (int) (id2 - id1) : r;
        }
    };
    private static final Comparator<ThreadInfo> calcCpuTimeComparator = new Comparator<ThreadInfo>() {

        @Override
        public int compare(ThreadInfo o1, ThreadInfo o2) {
            Map<Long, Long> map = SystemMonitor.getCalcThreadsCpuTime();
            long id1 = o1.getThreadId();
            long id2 = o2.getThreadId();
            Long time1 = map.get(id1);
            Long time2 = map.get(id2);
            if (time1 == null) {
                time1 = 0l;
            }
            if (time2 == null) {
                time2 = 0l;
            }
            int r = (int) (time2 - time1);
            return r == 0 ? (int) (id2 - id1) : r;
        }
    };

    // http://www.gznc.edu.cn/yxsz/jjglxy/book/Java_api/java/lang/management/ThreadMXBean.html#isThreadCpuTimeEnabled()
    public static String getThreadsDetailInfo(String name, boolean onlyRunning, int maxFrames) {
        boolean nameFilter = !StringTools.isEmpty(name);
        ThreadInfo[] infos = null;
        synchronized (threadMXBean) {// 尝试synchronize来解决并发崩溃的问题
            infos = threadMXBean.dumpAllThreads(true, true);
        }

        boolean cpuTimeEnabled = false;
        try {
            cpuTimeEnabled = threadMXBean.isThreadCpuTimeEnabled();// 测试是否启用了线程 CPU 时间测量。
        } catch (UnsupportedOperationException e) {
        }
        boolean cpuTimeSupported = threadMXBean.isThreadCpuTimeSupported();// 测试 Java 虚拟机实现是否支持任何线程的 CPU 时间测量。支持任何线程 CPU 时间测定的 Java 虚拟机实现也支持当前线程的 CPU 时间测定。
        Map<Long, Long> lastCpuTimeMap = SystemMonitor.getCalcThreadsCpuTime();
        StringBuilder tmp = new StringBuilder();
        List<ThreadInfo> list = Arrays.asList(infos);

        String fmt = "%-8s%-14s%-8s%-8s%-25s%-20s%s\n";
        if (cpuTimeSupported) {
            if (lastCpuTimeMap == null) {
                Collections.sort(list, allCpuTimeComparator);
                tmp.append(String.format(fmt, "ID", "STATE ", "Blocks", "Waits", "CpuTime↓", "LastCpuTime", "Name"));
            } else {
                Collections.sort(list, calcCpuTimeComparator);
                tmp.append(String.format(fmt, "ID", "STATE ", "Blocks", "Waits", "CpuTime", "LastCpuTime↓", "Name"));
            }
        } else {
            tmp.append(String.format(fmt, "ID↑", "STATE ", "Blocks", "Waits", "CpuTime", "LastCpuTime", "Name"));
        }

        for (ThreadInfo info : list) {
            // for (int i = 0; i < infos.length; i++) {
            // ThreadInfo info = infos[i];
            if (isFilted(info, nameFilter, name, onlyRunning)) {
                continue;
            }
            long id = info.getThreadId();
            long cupTime = cpuTimeEnabled ? threadMXBean.getThreadCpuTime(id) : -1;
            String lastCpuTime = lastCpuTimeMap == null ? "" : lastCpuTimeMap.get(id) + "";
            tmp.append(String.format(fmt, id, info.getThreadState(), info.getBlockedCount(), info.getWaitedCount(), HumanReadableUtil.timeSpan(cupTime / 1000), lastCpuTime, info.getThreadName()));
        }
        tmp.append("cpuTimeSupported:").append(cpuTimeSupported).append("\tcpuTimeEnabled:").append(cpuTimeEnabled).append("\n");
        tmp.append("\n");
        tmp.append("\n");
        int i = -1;
        for (ThreadInfo info : list) {
            i++;
            // for (int i = 0; i < infos.length; i++) {
            // ThreadInfo info = infos[i];
            if (isFilted(info, nameFilter, name, onlyRunning)) {
                continue;
            }
            tmp.append("--------------- (");
            tmp.append(i);
            tmp.append(") ------------------------------------------------------------------------------------------------------------------------\n");
            tmp.append(printThreadInfo(info, maxFrames));
        }
        return tmp.toString();
    }

    public static boolean isFilted(ThreadInfo info, boolean nameFilter, String name, boolean onlyRunnable) {
        if (nameFilter) {
            if (!info.getThreadName().contains(name)) {
                return true;
            }
        }
        if (onlyRunnable) {
            if (Thread.State.RUNNABLE != info.getThreadState() && Thread.State.BLOCKED != info.getThreadState()) {
                return true;
            }
        }
        return false;
    }

    /**
     * 打印threadInfo,抄处 threadInfo.toString()
     *
     * @param info
     * @param maxFrames
     * @return
     */
    public static String printThreadInfo(ThreadInfo info, int maxFrames) {
        StringBuilder sb = new StringBuilder("\"" + info.getThreadName() + "\"" + " Id=" + info.getThreadId() + " " + info.getThreadState());
        if (info.getLockName() != null) {
            sb.append(" on " + info.getLockName());
        }
        if (info.getLockOwnerName() != null) {
            sb.append(" owned by \"" + info.getLockOwnerName() + "\" Id=" + info.getLockOwnerId());
        }
        if (info.isSuspended()) {
            sb.append(" (suspended)");
        }
        if (info.isInNative()) {
            sb.append(" (in native)");
        }
        sb.append('\n');
        int i = 0;
        StackTraceElement[] stackTrace = info.getStackTrace();
        for (; i < stackTrace.length && i < maxFrames; i++) {
            StackTraceElement ste = stackTrace[i];
            sb.append("\tat " + ste.toString());
            sb.append('\n');
            if (i == 0 && info.getLockInfo() != null) {
                Thread.State ts = info.getThreadState();
                switch (ts) {
                    case BLOCKED:
                        sb.append("\t-  blocked on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    case WAITING:
                        sb.append("\t-  waiting on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    case TIMED_WAITING:
                        sb.append("\t-  waiting on " + info.getLockInfo());
                        sb.append('\n');
                        break;
                    default:
                }
            }

            for (MonitorInfo mi : info.getLockedMonitors()) {
                if (mi.getLockedStackDepth() == i) {
                    sb.append("\t-  locked " + mi);
                    sb.append('\n');
                }
            }
        }
        if (i < stackTrace.length) {
            sb.append("\t...");
            sb.append('\n');
        }

        LockInfo[] locks = info.getLockedSynchronizers();
        if (locks.length > 0) {
            sb.append("\n\tNumber of locked synchronizers = " + locks.length);
            sb.append('\n');
            for (LockInfo li : locks) {
                sb.append("\t- " + li);
                sb.append('\n');
            }
        }
        sb.append('\n');
        return sb.toString();
    }

    private SystemInfo() {
    }
}
