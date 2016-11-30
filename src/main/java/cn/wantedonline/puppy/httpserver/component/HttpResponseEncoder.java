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

package cn.wantedonline.puppy.httpserver.component;


import cn.wantedonline.puppy.httpserver.common.HttpServerConfig;
import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * <pre>
 *     包装的响应解码器
 *     完成功能：
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/25.
 */
public class HttpResponseEncoder extends io.netty.handler.codec.http.HttpResponseEncoder {
    //这里没有注入到Spring容器中，只能通过这种方式获取Config
    private HttpServerConfig config = BeanUtil.getTypedBean(HttpServerConfig.class);

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, List<Object> out) throws Exception {
        super.encode(ctx, msg, out);
        //发包流量统计
        if (AssertUtil.isNotEmptyCollection(out)) {
            long bytes = 0;
            for (Object obj : out) {
                if (obj instanceof ByteBuf) {
                    bytes += ((ByteBuf)obj).readableBytes();
                }
            }
            if (bytes > 0) {
                config.streamStat.getOutbound().record(bytes);
            }
        }
    }
}
