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

import cn.wantedonline.puppy.httpserver.common.CmdMappers;
import io.netty.channel.ChannelHandler.Sharable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * <pre>
 *     Cmd命令分发器
 * </pre>
 * @author wangcheng
 * @since V0.1.0 on 2016/11/18.
 */
@Sharable
@Component
public class CmdPageDispatcher extends BasePageDispatcher {
    @Autowired
    private CmdMappers cmdMappers;
    @Override
    public void init() {
        cmdMappers.initAutoMap();
        cmdMappers.initCmdMapperDefinedMap();
        cmdMappers.initConfigMap();
        cmdMappers.printFuzzyMap();
    }


}
