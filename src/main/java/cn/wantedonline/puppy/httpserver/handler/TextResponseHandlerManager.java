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

import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.annotation.AfterBootstrap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/25.
 */
@Service
public class TextResponseHandlerManager extends HandlerManager<TextResponseHandler> {
    private static TextResponseHandlerManager INSTANCE;

    @Autowired
    private TextResponseHandler textResponseHandler;

    /**
     * 因为放入了Spring中，所以必须有一个公有构造函数
     */
    public TextResponseHandlerManager() {
        INSTANCE = this;
    }

    public static TextResponseHandlerManager getInstance() {
        return INSTANCE;
    }

    @AfterBootstrap
    public void initHandlerChain() {
        addFirst(textResponseHandler);
    }

    private void setContent(ContextAttachment attach, Object cmdReturnObj) {
        HttpResponse response = attach.getResponse();
        if (!response.isContentSetted()) {
            String contentStr = null;
            for (TextResponseHandler thr : getHandlerChain()) {
                contentStr = thr.buildContentString(attach, cmdReturnObj);
                response.setContentString(contentStr);
                return;
            }
        }
    }

    public void writeResponse(ContextAttachment attach, Object cmdReturnObj) {
        HttpResponse response = attach.getResponse();
        setContent(attach, cmdReturnObj);
        response.packagingCookies();
    }


}
