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

import cn.wantedonline.puppy.exception.AbstractHttpServerError;
import cn.wantedonline.puppy.httpserver.common.ContentType;
import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.StringHelper;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.Set;

/**
 * <pre>
 *     普通文本返回值处理器
 * </pre>
 * @author wangcheng
 * @since V0.1.0 on 16/11/25.
 */
@Service
public class TextResponseHandler implements Handler {
    private static final Logger log = Log.getLogger();

    @Config
    private String responseReturnNull = "Response is null";

    @Config(resetable = true, split = ",")
    protected Set<String> logThrowableIgnoreList = Collections.emptySet();

    public String buildContentString(ContextAttachment attach, Object cmdReturnObj) {
        HttpResponse response = attach.getResponse();
        ContentType type = response.getInnerContentType();
        if (type.equals(ContentType.html)) {
            response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=" + response.getContentCharset());
        } else if (type.equals(ContentType.xml)) {
            response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/xml; charset=" + response.getContentCharset());
        } else {
            response.setHeaderIfEmpty(HttpHeaders.Names.CONTENT_TYPE, "text/plain; charset=" + response.getContentCharset());
        }
        StringBuilder content = _buildContentString(attach, cmdReturnObj);
        return content.toString();
    }

    public Object handleThrowable(ContextAttachment attach, Throwable ex) throws Exception {
        HttpRequest request = attach.getRequest();
        HttpResponse response = attach.getResponse();

        if (ex instanceof AbstractHttpServerError) {
            HttpResponseStatus status = ((AbstractHttpServerError)ex).getStatus();
            response.setStatus(status);
            return ex.getMessage();
        }

        return StringHelper.printThrowableSimple(ex);
    }

    private StringBuilder _buildContentString(ContextAttachment attach, Object cmdReturnObj) {
        StringBuilder content = new StringBuilder();
        content.append(AssertUtil.isNull(cmdReturnObj) ? responseReturnNull : cmdReturnObj);
        return content;
    }

    public void logThrowable(final ContextAttachment attach, final HttpRequest request, final HttpResponse response, final Throwable ex) {
        response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);

        if (logThrowableIgnoreList.contains(ex.getClass().getName())) {
            return;
        }

        logError(request.getPath(), "{}:{} |{}\n\n{}",
                ex.getClass().getSimpleName(),
                request.getPath(),
                ManagementFactory.getRuntimeMXBean().getName(),
                request.getDetailInfo(),
                ex);
    }

    public static void logError(final String mailTitleInfo, final String info, final Object... args) {
        Object objEx = args[args.length - 1];
        String exInfo = "";
        if (objEx instanceof Throwable) {
            exInfo = ((Throwable) objEx).getClass().getSimpleName();
        }
        //TODO：决定是否要发送邮件
        log.error(info, args);
    }


}
