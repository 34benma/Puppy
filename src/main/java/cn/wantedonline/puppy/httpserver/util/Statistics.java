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

package cn.wantedonline.puppy.httpserver.util;


import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import io.netty.channel.ChannelHandlerContext;

public interface Statistics {

    /**
     * 上报打开通道
     */
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e);

    /**
     * 上报开始接收消息,统计接收数据的流量
     */
    public void messageReceiving(MessageEvent e);

    /**
     * 上报接收消息完成,统计解码用时
     */
    public void messageReceived(ContextAttachment attach);

    /**
     * 上报开始发送消息,统计业务用时
     */
    public void writeBegin(ContextAttachment attach);

    /**
     * 上报发送消息完成,统计编码用时
     */
    public void writeComplete(ContextAttachment attach, WriteCompletionEvent e);

    /**
     * 上报处理中,发现通道被提前关闭
     */
    public void channelInterruptClosed(ChannelHandlerContext ctx);

    /**
     * 上报关闭通道
     */
    public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e);

}
