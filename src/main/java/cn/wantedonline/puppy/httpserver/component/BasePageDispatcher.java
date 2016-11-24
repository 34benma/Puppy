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

import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.AttributeKey;

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

    @Override
    public void init() {

    }

    public abstract void dispatch(ContextAttachment attachment);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);

    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {

        super.channelReadComplete(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
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
                        //参考Tomcat的处理
//                        ctx.writeAndFlush();
                    }
                    request.setRemoteAddress(ctx.channel().remoteAddress());
                    request.setLocalAddress(ctx.channel().localAddress());
                    requestReceived(ctx, attach);
                } else {
                    throw new RuntimeException("can't reslove message: " + msg);
                }
            } finally {
                ctx.writeAndFlush(msg);
            }
        }
    }

    private void requestReceived(ChannelHandlerContext ctx, ContextAttachment attachment) {
        HttpResponse response = new HttpResponse(attachment);
        attachment.registerNewMessage(response);
        dispatch(attachment);
    }

    public ContextAttachment getAttach(ChannelHandlerContext ctx) {
        ctx.attr(HTTP_ATTACH_KEY).setIfAbsent(new ContextAttachment(ctx));
        return (ContextAttachment)ctx.attr(HTTP_ATTACH_KEY);
    }


}
