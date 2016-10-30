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

package cn.wantedonline.puppy.httpserver.component;

import cn.wantedonline.puppy.httpserver.util.HttpServerConfig;
import cn.wantedonline.puppy.util.Log;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by wangcheng on 2016/10/30.
 */
@Service
public class TimeoutInterrupter {
    private static TimeoutInterrupter INSTANCE;

    private TimeoutInterrupter() {
        INSTANCE = this;
    }

    public static TimeoutInterrupter getInstance() {
        return INSTANCE;
    }

    @Autowired
    protected HttpServerConfig config;

    public interface AttachRegister {
        public void registerAttach(ContextAttachment attach);

        public void unregisterAttach(ContextAttachment attach);
    }

    private static final Logger logger = Log.getLogger();

    private Runnable _defaultInterrupter = new Runnable() {
        @Override
        public void run() {

        }

        private void close(ContextAttachment attach, String tips) {
            Channel channel = attach.
        }
    };
}
