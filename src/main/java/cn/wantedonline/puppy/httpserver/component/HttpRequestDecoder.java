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
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.util.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpMessage;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectDecoder;
import io.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;

import java.util.Arrays;
import java.util.List;

/**
 * <pre>
 *     Http请求解码器
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/19.
 */
public class HttpRequestDecoder extends HttpObjectDecoder {
    //这里没有注入到Spring容器中，只能通过这种方式获取Config
    private HttpServerConfig config = BeanUtil.getTypedBean(HttpServerConfig.class);

    private Logger log = Log.getLogger(HttpRequestDecoder.class);

    public HttpRequestDecoder(int maxInitialLineLength, int maxHeaderSize, int maxChunkSize) {
        super(maxInitialLineLength, maxHeaderSize, maxChunkSize, true);
    }

    @Override
    protected boolean isDecodingRequest() {
        return true;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        //收包统计
        if (msg instanceof ByteBuf) {
            long bytes = ((ByteBuf) msg).readableBytes();
            if (bytes > 0) {
                config.streamStat.getInbound().record(bytes);
            }
        }
        //因为ByteToMessageDecoder自动回将buf清空，因此需要在调用ByteToMessageDecoder的read前收包统计
        super.channelRead(ctx, msg);
    }

    @Override
    protected HttpMessage createMessage(String[] initialLine) throws Exception {
        HttpMethod method = HttpMethod.valueOf(initialLine[0]);
        try {
           return new HttpRequest(HttpVersion.valueOf(initialLine[2]), method, initialLine[1]);
        } catch (Exception e) {
            String fix = initialLine[1] + " " + initialLine[2];
            int result = 0;
            for (result = fix.length(); result > 0; --result) {
                if (Character.isWhitespace(fix.charAt(result - 1))) {
                    break;
                }
            }
            String version = fix.substring(result);
            for (; result > 0; --result) {
                if (!Character.isWhitespace(fix.charAt(result - 1))) {
                    break;
                }
            }
            String uri = fix.substring(0, result);
             uri = uri.replaceAll("\t", "%09").replaceAll("\n", "%0D").replaceAll("\r", "%0A").replaceAll(" ", "+");
            log.error("parse httpRequest initialLine fail!\n\tori:{}\n\t      fix:{}\n\t      uri:{}\n\t  version:{}\n\t{}", new Object[] {
                    Arrays.toString(initialLine),
                    fix,
                    uri,
                    version,
                    e.getMessage()
            });
            return new HttpRequest(HttpVersion.valueOf(version), method, uri);
        }
    }

    @Override
    protected HttpMessage createInvalidMessage() {
        return new HttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET, "/bad-request", true);
    }
}
