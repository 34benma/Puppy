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
 *     实现{@code ChannelEvent}接口，统计接口继承该接口实现自己想要实现的方法
 * </pre>
 *
 * @author wangcheng
 * @since V0.2.0 on 16/12/1.
 */
public class BaseChannelEvent implements ChannelEvent {
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void messageReceiveBegin(ContextAttachment attach) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void messageReceiveEnd(ContextAttachment attach) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void writeBegin(ContextAttachment attach) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }

    @Override
    public void writeEnd(ContextAttachment attach) {
        throw new UnsupportedOperationException("sub class did not override this method...");
    }
}
