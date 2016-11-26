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
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.AssertUtil;
import io.netty.handler.codec.http.HttpHeaders;
import org.springframework.stereotype.Service;

/**
 * <pre>
 *     普通文本返回值处理器
 * </pre>
 * @author wangcheng
 * @since V0.1.0 on 16/11/25.
 */
@Service
public class TextResponseHandler implements Handler {

    @Config
    private String responseReturnNull = "Response is null";

    public String buildContentString(ContextAttachment attach, Object cmdReturnObj) {
        HttpResponse response = attach.getResponse();
        response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=" + response.getContentCharset());
        StringBuilder content = _buildContentString(attach, cmdReturnObj);
        return content.toString();
    }

    public Object handleThrowable(ContextAttachment attach, Throwable ex) throws Exception {
        return null;
    }

    private StringBuilder _buildContentString(ContextAttachment attach, Object cmdReturnObj) {
        StringBuilder content = new StringBuilder();
        content.append(AssertUtil.isNull(cmdReturnObj) ? responseReturnNull : cmdReturnObj);
        return content;
    }


}
