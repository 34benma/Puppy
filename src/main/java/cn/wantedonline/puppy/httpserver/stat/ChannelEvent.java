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

import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import io.netty.channel.ChannelHandlerContext;

/**
 * <pre>
 *     通道事件集合，统计类可以实现该接口对通道某些数据进行统计
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/11/29.
 */
public interface ChannelEvent {

    public void channelRegistered(ChannelHandlerContext ctx);

    public void channelUnregistered(ChannelHandlerContext ctx);

    public void channelActive(ChannelHandlerContext ctx);

    public void channelInactive(ChannelHandlerContext ctx);

    public void channelRead(ChannelHandlerContext ctx, Object msg);

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause);

    public void messageReceiveBegin(ContextAttachment attach);

    public void messageReceiveEnd(ContextAttachment attach);

    public void writeBegin(ContextAttachment attach);

    public void writeEnd(ContextAttachment attach);
}
