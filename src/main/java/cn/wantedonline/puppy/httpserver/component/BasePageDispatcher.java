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
import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AttributeKey;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * <pre>
 *  上层业务请求分发器，不同的业务可以继承该类实现特殊的需求
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/18
 */
public abstract class BasePageDispatcher extends AbstractPageDispatcher {

    private static final AttributeKey<ContextAttachment> HTTP_ATTACH_KEY = AttributeKey.newInstance("HTTP_ATTACHMENT");

    @Autowired
    private HttpServerConfig config;

    @Override
    public void init() {

    }

    public abstract void dispatch(ContextAttachment attachment) throws Exception;

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        config.countStat.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        config.countStat.channelUnregistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        config.countStat.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        config.countStat.channelInactive(ctx);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        config.countStat.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (AssertUtil.isNotNull(msg)) {
            ContextAttachment attach = getAttach(ctx);
            try {
                if (msg instanceof HttpRequest) {
                    HttpRequest request = (HttpRequest) msg;
                    if (HttpHeaders.is100ContinueExpected(request)) {
                        //处理100 Continue http://www.w3.org/Protocols/rfc2616/rfc2616-sec8.html#sec8.2.3
                        ctx.writeAndFlush(new HttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE));
                    }
                    request.setRemoteAddress(ctx.channel().remoteAddress());
                    request.setLocalAddress(ctx.channel().localAddress());
                    attach.registerNewMessage(request);
                    requestReceived(ctx, attach);
                }

                if (msg instanceof HttpContent) {

                }
            } finally {
                ChannelFuture future = ctx.writeAndFlush(attach.getResponse().copy());
                attach.markWriteEnd();
                //次数统计
                config.countStat.responseSended(ctx, attach);
                //时间统计
                config.timeSpanStat.writeEnd(attach);
                future.addListener(attach);
            }
        }
    }

    private void requestReceived(ChannelHandlerContext ctx, ContextAttachment attachment) throws Exception {
        HttpResponse response = new HttpResponse(attachment);
        attachment.markWriteBegin();
        attachment.registerNewMessage(response);
        //次数统计
        config.countStat.requestReceived(ctx, attachment);
        //时间统计
        config.timeSpanStat.writeBegin(attachment);
        dispatch(attachment);
    }

    public ContextAttachment getAttach(ChannelHandlerContext ctx) {
        ctx.attr(HTTP_ATTACH_KEY).setIfAbsent(new ContextAttachment(ctx));
        return ctx.attr(HTTP_ATTACH_KEY).get();
    }


}
