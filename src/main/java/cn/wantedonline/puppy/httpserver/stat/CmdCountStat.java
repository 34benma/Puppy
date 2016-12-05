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

package cn.wantedonline.puppy.httpserver.stat;

import cn.wantedonline.puppy.spring.annotation.Config;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * <pre>
 *     Cmd次数统计
 *     继承BaseChannelEvent类仅仅是为标识这是一个统计类，并不实现BaseChannelEvent的任何方法
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 2016/12/2.
 */
@Component
public class CmdCountStat extends BaseChannelEvent {
    @Config
    private int maxCmdStat = 10;

    public class CmdStatBean implements Comparable {
        private String cmdName;
        private AtomicInteger count;

        public CmdStatBean(String cmdName, int count) {
            this.cmdName = cmdName;
            this.count.compareAndSet(0, 1);
        }

        public String getCmdName() {
            return cmdName;
        }

        public void setCmdName(String cmdName) {
            this.cmdName = cmdName;
        }

        public int getCount() {
            return count.get();
        }

        public void setCount(int count) {
            this.count.set(count);
        }

        @Override
        public String toString() {
            return "CmdStatBean{" +
                    "cmdName='" + cmdName + '\'' +
                    ", count=" + count +
                    '}';
        }

        @Override
        public int compareTo(Object o) {
            if (o instanceof CmdStatBean) {
                return this.getCount() - ((CmdStatBean) o).getCount();
            }
            return 0;
        }
    }

}
