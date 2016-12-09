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

import cn.wantedonline.puppy.spring.annotation.AfterConfig;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.CharsetTools;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.concurrent.BaseSchedulable;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.concurrent.WrappedConcurrentCircularQueue;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * <pre>
 *     统计中心
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/11/29.
 */
@Service
public class StatisticManager {
    private static final String COUNT_STAT_FILE_NAME = "COUNTSTAT.stat";
    private static final String STREAM_STAT_FILE_NAME = "STREAM.stat";
    private static final String TIME_SPAN_STAT_FILE_NAME = "TIMESPAN.stat";
    private Logger log = Log.getLogger(StatisticManager.class);

    @Config
    private boolean openCountStat = true;
    @Config
    private boolean openNioWorkerStat = true;
    @Config
    private boolean openStreamStat = true;
    @Config
    private boolean openTimeSpanStat = true;
    @Config
    private boolean openCmdCountStat = true;

    @Config
    private int snapshotDay = 7;
    @Config
    private int snapshotMilliseconds = 600000;// 每隔10min记录一次

    private static final int dayms = 3600 * 1000 * 24;
    private int snapshotSize = dayms / snapshotMilliseconds * snapshotDay;

    private boolean init = false;

    private WrappedConcurrentCircularQueue<CountStat.CountStatSnapshot> countStatSnapshotsData;
    private WrappedConcurrentCircularQueue<StreamStat.StreamStatSnapshot> streamStatSnapshotsData;
    private WrappedConcurrentCircularQueue<TimeSpanStat.TimeSpanSnapshot> timeSpanSnapshotsData;

    {
        if (openCountStat) {
            countStatSnapshotsData = new WrappedConcurrentCircularQueue<>(snapshotSize);
        }

        if (openStreamStat) {
            streamStatSnapshotsData = new WrappedConcurrentCircularQueue<>(snapshotSize);
        }

        if (openTimeSpanStat) {
            timeSpanSnapshotsData = new WrappedConcurrentCircularQueue<>(snapshotSize);
        }
    }

    @Autowired
    private StreamStat streamStat;
    @Autowired
    private CountStat countStat;
    @Autowired
    private NioWorkerStat nioWorkerStat;
    @Autowired
    private TimeSpanStat timeSpanStat;

    private NioWorkerStat.NioWorkerStatSnapshot nioWorkerStatSnapshot;

    private BaseSchedulable schedulable = new BaseSchedulable() {
        @Override
        public void process() throws Throwable {
            if (AssertUtil.isNotNull(countStatSnapshotsData)) {
                countStatSnapshotsData.addToHead(countStat.tickAndReset());
            }

            if (AssertUtil.isNotNull(streamStatSnapshotsData)) {
                streamStatSnapshotsData.addToHead(streamStat.tickAndReset());
            }

            if (AssertUtil.isNotNull(timeSpanSnapshotsData)) {
                timeSpanSnapshotsData.addToHead(timeSpanStat.tickAndReset());
            }

            if (openNioWorkerStat) {
                nioWorkerStatSnapshot = nioWorkerStat.tickNioWorkerStatSnapshot();
            }
        }
    };

    @AfterConfig
    public synchronized void init() {
        if (init) {
            return;
        }
        schedulable.scheduleAtFixedRateWithDelayMs(getSnapshotInitialDelay(), snapshotMilliseconds);
    }

    public void readStatisticData() {
        if (openTimeSpanStat) {
            timeSpanSnapshotsData = readStatisticSnapshotsDataFromFile(TIME_SPAN_STAT_FILE_NAME);
        }
        if (openCountStat) {
            countStatSnapshotsData = readStatisticSnapshotsDataFromFile(COUNT_STAT_FILE_NAME);
        }
        if (openStreamStat) {
            streamStatSnapshotsData = readStatisticSnapshotsDataFromFile(STREAM_STAT_FILE_NAME);
        }
    }

    public synchronized void writeStatisticData() {
        try {
            writeStatisticSnapshotsData2File(COUNT_STAT_FILE_NAME, countStatSnapshotsData);
            writeStatisticSnapshotsData2File(TIME_SPAN_STAT_FILE_NAME, timeSpanSnapshotsData);
            writeStatisticSnapshotsData2File(STREAM_STAT_FILE_NAME, streamStatSnapshotsData);
        } catch (IOException e) {
            log.error("write statistic data to file failed, IO Error... error info {}", e);
        }
    }

    private WrappedConcurrentCircularQueue readStatisticSnapshotsDataFromFile(String fileName) {
        return readStatisticSnapshotsDataFromFile(fileName, CharsetTools.UTF_8);
    }

    private WrappedConcurrentCircularQueue readStatisticSnapshotsDataFromFile(String fileName, Charset charset) {
        String filePath = getClass().getProtectionDomain().getCodeSource().getLocation().getPath() + fileName;
        File file = new File(filePath);
        try(BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), charset))) {
            StringBuilder text = new StringBuilder();
            if (file.exists()) {
                String lineText = "";
                while (AssertUtil.isNotNull(lineText = reader.readLine())) {
                    text.append(lineText);
                }
                return JSONObject.parseObject(text.toString(), WrappedConcurrentCircularQueue.class);
            }
        } catch (FileNotFoundException e) {
            log.error("read statistic snapshots data error, not found file,if it is the first time read, you can ignore it. error info",e);
        } catch (IOException e) {
            log.error("read statistic snapshots data error, file is bad... error info", e);
        }
        return null;
    }

    private void writeStatisticSnapshotsData2File(String fileName, WrappedConcurrentCircularQueue data) throws IOException {
        writeStatisticSnapshotsData2File(fileName, data, CharsetTools.UTF_8);
    }

    private void writeStatisticSnapshotsData2File(String fileName, WrappedConcurrentCircularQueue data, Charset charset) throws IOException {
        if (AssertUtil.isNotEmptyCollection(data)) {
            while (data.isWriting()); //当在写数据时，等待它写完
            data.prohibitWrite(); //禁止写入数据,事实上，使用迭代器就好了，复制一份数据
            JSON.writeJSONString(new FileOutputStream(fileName), CharsetTools.UTF_8, data);
        }
    }

    /**
     * 对齐整点时间
     * @return
     */
    private long getSnapshotInitialDelay() {
        String truncateDateStr = DateStringUtil.DEFAULT.now().substring(0, 15) + "0:00";// 这是每隔10min
        Date truncateDate = DateStringUtil.DEFAULT.parse(truncateDateStr);
        return truncateDate.getTime() + snapshotMilliseconds - System.currentTimeMillis();
    }

}
