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

package cn.wantedonline.puppy.httpserver.util.command;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.StringTokenizer;

import cn.wantedonline.puppy.util.DateUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.StringTools;
import org.slf4j.Logger;

/**
 * 命令服务类，主要完成命令的执行、监控、中断
 */
public class CommandService {

    /**
     * 处理执行结果的模式
     */
    protected enum CmdMode {
        /**
         * 使用更兼容的方式处理执行结果：可以处理刷新当前行的情况
         */
        compatible,
        /**
         * 不处理执行结果
         */
        ignore,
        /**
         * 使用简单方式处理执行结果
         */
        simple;
    }

    /**
     * 用于监控 通过判断 当前执行命令是否还有输出流,来决定是否中断这个命令
     *
     */
    protected class CmdMonitor {

        /**
         * 是否已提交中断
         */
        private boolean interrupt;
        /**
         * 命令被中断时提交的信息
         */
        private String interuptedMsg = "";
        /**
         * 监控时提交中断的信息
         */
        private String interuptMsg = "";
        /**
         * 上次获得输出流时间
         */
        protected long lastProcessTime;
        /**
         * 上次获得输出流时间
         */
        protected String lastProcessTimeStr;
        /**
         * 进程描述对象
         */
        private Process process;
        /**
         * 执行execute()方法使用的线程
         */
        @SuppressWarnings("unused")
        private Thread processThread;
        @SuppressWarnings("unused")
        private BufferedReader reader;

        /**
         * 监控器构造方法
         * 
         * @param process 输出流
         * @param reader
         */
        public CmdMonitor(Process process, BufferedReader reader) {
            this.process = process;
            this.processThread = Thread.currentThread();
            lastProcessTime = System.currentTimeMillis();
            this.reader = reader;
            lastProcessTimeStr = now();
        }

        /**
         * 执行中断，中断信息为msg
         * 
         * @param msg 中断信息
         */
        public void interrupt(String msg) {
            interrupt = true;
            this.interuptMsg = MessageFormat.format("EXECUTE {0} - currentTime:{1},lastProcessTime:{2}\n", msg, now(), lastProcessTimeStr);

            try {
                process.getInputStream().close();
            } catch (IOException e) {
                log.error("try to close reader encount exception", e);
            }
            try {
                process.destroy();
            } catch (Exception e) {
                log.error("try to destory process encount exception", e);
            }
        }

        /**
         * 监控当前命令是否超过了指定的时间，超过后就中断
         * 
         * @param tolerantSec 指定的时间
         * @return
         */
        private boolean monitor(int tolerantSec) {
            if (System.currentTimeMillis() - lastProcessTime > tolerantSec * 1000) {
                interrupt("TIMEOUT(>" + tolerantSec + ")");
                return true;
            }
            return false;
        }

        /**
         * 获得当前时间
         * 
         * @return
         */
        public String now() {
            return DateUtil.UNSAFE_DF_DEFAULT.format(new Date());
        }

        /**
         * 监控器描述
         */
        @Override
        public String toString() {
            return interuptedMsg + interuptMsg;
        }

        /**
         * 更新上次获得输出流的时间
         */
        public void updateLastProcessTime() {
            lastProcessTime = System.currentTimeMillis();
            lastProcessTimeStr = now();
        }
    }

    private final static Logger log = Log.getLogger();
    /**
     * 当前操作系统的类型
     */
    private static final int OS_TYPE = getOsType();

    /**
     * 获得当前操作系统的编号，0为linux，1为windows
     * 
     * @return
     */
    private static int getOsType() {
        String osName = System.getProperty("os.name");
        if (osName.startsWith("Linux")) {
            return 0;
        } else if (osName.startsWith("Windows")) {
            return 1;// 不处理老掉牙的win98,win95
        }
        return -1;
    }

    /**
     * 读入的命令行缓冲
     */
    private StringBuilder _readLineBuffer = new StringBuilder();

    private int _readLineRemainChar = -1;
    /**
     * cmd处理模式,默认使用兼容方式
     */
    protected CmdMode cmdMode = CmdMode.compatible;
    /**
     * cmd监控
     */
    protected CmdMonitor cmdMonitor;
    /**
     * 要真正执行的命令序列
     */
    protected String[] commandArray;
    /**
     * 要真正执行的命令序列 的 字符串表示
     */
    protected String commandArrayStr;
    /**
     * 命令生成时间 的字符串表示
     */
    protected String createTime = DateUtil.UNSAFE_DF_DEFAULT.format(new Date()) + " ";
    /**
     * 当前正在刷新的行(会被下一行覆盖)
     */
    protected String flushingLine = "";
    /**
     * 当前执行的行是否要被重刷(被下一行回车覆盖)
     */
    protected boolean flushingProcessingLing;
    /**
     * 是否执行完
     */
    protected boolean processComplete;
    /**
     * 执行中具体结果
     */
    protected StringBuilder processingDetail = new StringBuilder();
    /**
     * 执行中当前获取的行
     */
    protected String processingLine;
    /**
     * 当前执行的行数，获取一行时其自动累加
     */
    protected int processingLineIndex = 0;
    /**
     * 执行结果
     */
    protected boolean success = false;
    /**
     * 是否使用linux的bin/sh来执行当前命令 其作用主要是为了解决: Java具有使用Runtime.exec对本地程序调用进行重定向的能力，但是用重定向或者管道进行命令调用将会出错的问题 注意:在Java中调用本地程序会破坏平台独立性规则 搜索关键词:使用Runtime.exec重定向本地程序调用
     * http://hi.baidu.com/javaroad/blog/item/a56d74e7ce7fba28b8382053.html
     */
    private boolean useShell = false;

    /**
     * 生成[/bin/sh,-c,cmd字符串]方式执行命令
     */
    public CommandService(String commandString) {
        this(commandString, true);
    }

    /**
     * 无法分配内存时的处理对象
     */
    public static CantAllocateMemErrorHandler cantAllocateMemErrorHandler;

    /**
     * <pre>
     * 传入cmd字符串
     * 1.userShell = true,则会在linux系统中生成[/bin/sh,-c,cmd字符串]
     * 2.userShell = false,则会分析字元
     * </pre>
     * 
     * @param commandString 命令序列的字符串表示
     * @param useShell 是否使用Shell
     */
    public CommandService(String commandString, boolean useShell) {
        this.useShell = useShell;
        if (this.useShell) {
            this.commandArray = buildOsSpcArgs(commandString);
        } else {
            this.commandArray = buildArgs(commandString);
        }
        init();
    }

    /**
     * 直接传入cmd[]
     */
    public CommandService(String[] command) {
        this.commandArray = command;
        this.useShell = false;
        init();
    }

    /**
     * 将字符串命令转换为数组格式的命令
     * 
     * @param commandString 要转换的字符串命令
     * @return
     */
    private String[] buildArgs(String commandString) {
        StringTokenizer st = new StringTokenizer(commandString);
        String[] cmdarray = new String[st.countTokens()];
        for (int i = 0; st.hasMoreTokens(); i++) {
            cmdarray[i] = st.nextToken();
        }
        return cmdarray;
    }

    /**
     * 根据操作系统的不同将字符串命令转换为数组格式的命令
     * 
     * @param commandString 要转换的字符串命令
     * @return
     */
    private String[] buildOsSpcArgs(String commandString) {
        switch (OS_TYPE) {
        case 0:
            String[] tmp = {
                "/bin/sh",
                "-c",
                commandString
            };
            return tmp;
        case 1:
            String[] tmp1 = {
                "cmd.exe",
                "/c",
                commandString
            };
            return tmp1;
        }
        return null;
    }

    /**
     * 检查当前命令是否要 准备状态,而非已经执行完
     */
    public void checkIsPreparing() {
        if (processComplete) {
            throw new IllegalStateException("cmd is already completed:" + commandArrayStr);
        }
    }

    /**
     * 获得相同命令的新vo
     */
    public CommandService copy() {
        CommandService cmd = new CommandService(this.commandArray);
        cmd.useShell = this.useShell;
        return cmd;
    }

    /**
     * 执行当前命令
     * 
     * @return
     */
    public CommandService execute() {
        return execute("UTF-8");
    }

    public CommandService execute(String charset) {
        return execute(charset, log);
    }

    /**
     * 执行当前命令
     * 
     * @param charset 命令的编码格式
     * @return
     */
    public synchronized CommandService execute(String charset, Logger logger) {
        checkIsPreparing();
        try {
            if (StringTools.isEmpty(charset)) {
                charset = "UTF-8";
            }
            logger.info("RUN CMD:{} ({})", commandArrayStr, cmdMode);
            Process process = new ProcessBuilder(commandArray).redirectErrorStream(true).start();
            BufferedReader reader = null;
            try {
                if (cmdMode != CmdMode.ignore) {
                    InputStreamReader innerIs = new InputStreamReader(process.getInputStream(), charset);

                    if (cmdMode == CmdMode.simple) {
                        LineNumberReader lr = new LineNumberReader(innerIs);
                        reader = lr;
                        cmdMonitor = new CmdMonitor(process, reader);

                        while ((processingLine = readLineSimple(lr)) != null) {
                            cmdMonitor.updateLastProcessTime();
                            processLine();
                            if (!flushingProcessingLing) {
                                processingLineIndex++;
                                // lineReader.getLineNumber()
                                // 测试发现lineReader.getLineNumber()其实就是这里要标志的processingLineIndex
                            }
                        }
                    } else {
                        reader = new BufferedReader(innerIs);
                        cmdMonitor = new CmdMonitor(process, reader);
                        // 在windows平台上，运行被调用程序的DOS窗口在程序执行完毕后往往并不会自动关闭，从而导致Java应用程序阻塞在waitfor()。
                        // 导致该现象的一个可能的原因是，该可执行程序的标准输出比较多，而运行窗口的标准输出缓冲区不够大。
                        // 解决的办法是，利用Java提供的Process类提供的方法让Java虚拟机截获被调用程序的DOS运行窗口的标准输出，
                        // 在waitfor()命令之前读出窗口的标准输出缓冲区中的内容。
                        while ((processingLine = readLineCompatible(reader)) != null) {
                            cmdMonitor.updateLastProcessTime();
                            processLine();
                            if (!flushingProcessingLing) {
                                processingLineIndex++;
                            }
                        }
                    }
                }

                int existCode = process.waitFor();// 这里一直阻塞等待结果

                if (existCode == 0) {
                    success = true;
                    logger.info("RUN CMD OK:{}", commandArrayStr);
                } else {
                    success = false;
                    logger.error("RUN CMD ERR:{},CODE:{}", commandArrayStr, existCode);
                }
            } catch (Exception e) {
                if (isInterrupt()) {
                    cmdMonitor.interuptedMsg = "[INTERRUPTED(" + e.getClass().getSimpleName() + ")]";
                    logger.error("RUN CMD {}:{}", new Object[] {
                        cmdMonitor,
                        commandArrayStr,
                        e
                    });
                } else {
                    logger.error("RUN CMD EXCEPTION:{}", commandArrayStr, e);
                }
            } finally {
                processComplete = true;
                if (process != null) {
                    try {
                        if (reader != null) {
                            reader.close();
                        }
                        process.getInputStream().close();
                    } catch (Exception e2) {
                    }
                    process.destroy();
                }
            }
        } catch (IOException e) {
            logger.error("RUN CMD ERR:{}", commandArrayStr, e);
            String error = e.toString().toLowerCase();
            if (cantAllocateMemErrorHandler != null && error.contains("cannot allocate memory")) {
                logger.error("\n====Waiting to resolve the fatal Exception====\n");
                cantAllocateMemErrorHandler.handleCantAllocateMemError(e);
            }
        }
        return this;
    }

    /**
     * 获得命令模式
     * 
     * @return
     */
    public CmdMode getCmdMode() {
        return cmdMode;
    }

    /**
     * 获得命令序列的字符串表示
     * 
     * @return
     */
    public String getCommandArrayStr() {
        return commandArrayStr;
    }

    /**
     * 获得执行中的结果
     * 
     * @return
     */
    public StringBuilder getProcessingDetail() {
        return processingDetail;
    }

    /**
     * 初始化，将命令的数组格式转化为字符串格式，并设置success为false
     */
    private void init() {
        commandArrayStr = Arrays.toString(commandArray);
        success = false;
    }

    /**
     * 中断当前命令
     * 
     * @param interuptMsg
     */
    public void interrupt(String interuptMsg) {
        if (cmdMode == CmdMode.ignore) {
            throw new IllegalAccessError("cant interrupt cmd with [ignore] CmdMode:" + commandArrayStr);
        }
        if (cmdMonitor == null) {
            throw new IllegalStateException("cmd isnot started:" + commandArrayStr);
        }
        cmdMonitor.interrupt(interuptMsg);
    }

    /**
     * 当前命令是否被中断
     * 
     * @return
     */
    public boolean isInterrupt() {
        if (cmdMonitor == null) {
            return false;
        }
        return cmdMonitor.interrupt;
    }

    /**
     * 当前命令是否处理完成
     * 
     * @return
     */
    public boolean isProcessComplete() {
        return processComplete;
    }

    /**
     * 当前命令是否执行成功
     * 
     * @return
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 监控当前命令是否超过了指定的时间
     * 
     * @param tolerantSec 指定的中断时间
     * @return
     */
    public boolean monitor(int tolerantSec) {
        if (cmdMonitor == null) {
            return false;
        }
        return cmdMonitor.monitor(tolerantSec);
    }

    protected void processLine() {
    }

    /**
     * 可以鉴定当前处理行是否是 只以 /r结尾的(也就是 当前行会被下一行覆盖)
     * 
     * @param br 命令行
     * @return
     * @throws IOException
     */
    private String readLineCompatible(BufferedReader br) throws IOException {
        _readLineBuffer.delete(0, _readLineBuffer.length());
        int readingChar = -1;
        int lastChar = -1;
        if (_readLineRemainChar != -1) {
            lastChar = _readLineRemainChar;
            if (_readLineRemainChar != 13 && _readLineRemainChar != 10) {
                _readLineBuffer.append((char) _readLineRemainChar);
            }
        }
        flushingProcessingLing = false;
        while ((readingChar = br.read()) != -1) {
            char c = (char) readingChar;
            updateLastProcessTimeForChar(c);
            if (readingChar == 10) {// \n
                if (lastChar == 13) { // \r\n
                    _readLineRemainChar = -1;
                    break;
                } else if (lastChar == 10) { // \n\n
                    _readLineRemainChar = readingChar;
                    break;
                } else {
                    lastChar = readingChar;
                    continue;
                }
            } else if (readingChar == 13) {// \r
                if (lastChar == 10) {// \n\r
                    _readLineRemainChar = -1;
                    break;
                } else {// \r\r 或 A\r
                    lastChar = readingChar;
                    continue;
                }
            } else {
                if (lastChar == 10) {// 当A遇到/n
                    _readLineRemainChar = readingChar;
                    break;
                } else if (lastChar == 13) {// 当A遇到/r,说明当前行是要被刷新覆盖的
                    _readLineRemainChar = readingChar;
                    flushingProcessingLing = true;
                    break;
                } else {
                    _readLineBuffer.append(c);
                    continue;
                }
            }
        }
        if (readingChar == -1) {
            if (_readLineBuffer.length() == 0) {
                return null;
            } else {
                _readLineRemainChar = -1;
            }
        }
        String result = _readLineBuffer.toString();
        if (!flushingProcessingLing) {
            processingDetail.append(result).append('\n');
            flushingLine = "";
        } else {
            flushingLine = result;
        }
        return result;
    }

    /**
     * 简单地处理当前输出行
     * 
     * @param lineReader 行数据读取器
     * @return
     * @throws IOException
     */
    private String readLineSimple(LineNumberReader lineReader) throws IOException {
        // _readLineBuffer= null;
        // flushingLine = "";
        // flushingProcessingLing = false;
        processingLine = lineReader.readLine();
        processingDetail.append(processingLine).append('\n');
        return processingLine;
    }

    /**
     * 设置命令模式
     * 
     * @param cmdMode
     */
    public void setCmdMode(CmdMode cmdMode) {
        checkIsPreparing();
        this.cmdMode = cmdMode;
    }

    /**
     * 默认打印所有信息
     */
    @Override
    public String toString() {
        return toString(0);
    }

    /**
     * 获得打印信息
     * 
     * @param type 0-打印所有,1-只打印命令以及最后一行,2-只打印命令
     * @return 要打印的信息
     */
    public String toString(int type) {
        // if (commandString == null)
        // return createTime + Arrays.toString(commandArray);
        // return createTime + commandString;

        StringBuilder tmp = new StringBuilder();
        if (!isProcessComplete()) {
            long span = cmdMonitor == null ? 0 : System.currentTimeMillis() - cmdMonitor.lastProcessTime;// 距离上次接收到信息的时间
            tmp.append(">>>(").append(span).append("MS)");
        }
        tmp.append(createTime);
        tmp.append(commandArrayStr);
        tmp.append('\n');

        if (type == 0) {
            tmp.append(processingDetail);
            tmp.append(flushingLine).append('\n');
        } else if (type == 1) {
            if (processingLine != null) {
                tmp.append(processingLine).append('\n');
            }
        }

        if (cmdMonitor != null) {
            tmp.append(cmdMonitor);
        }
        return tmp.toString();
    }

    /**
     * 这里特别针对一些程序进行处理不同的update时间戳
     * 
     * @param c
     */
    protected void updateLastProcessTimeForChar(char c) {
        // if (c == '-')
        // cmdMonitor.updateLastProcessTime();
    }
}
