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

package cn.wantedonline.puppy.httpserver.handler;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/25.
 */
public abstract class HandlerManager<T extends Handler> {
    private List<T> handlerChain = new ArrayList<T>(1);

    public void addFirst(T handler) {
        handlerChain.add(0, handler);
    }

    public void addLast(T handler) {
        handlerChain.add(handler);
    }

    public T removeFirst() {
        if (handlerChain.isEmpty()) {
            return null;
        }
        return handlerChain.remove(0);
    }

    public T removeLast() {
        if (handlerChain.isEmpty()) {
            return null;
        }
        return handlerChain.remove(handlerChain.size()-1);
    }

    public List<T> getHandlerChain() {
        return handlerChain;
    }
}
