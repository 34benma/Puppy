/*
 * Copyright [2016-2026] wangcheng(wantedonline@outlook.com)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package cn.wantedonline.puppy.exception;

import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/19.
 */
public class IllegalParameterError extends AbstractHttpServerError {
    private static final String name = IllegalParameterError.class.getSimpleName() + ":";
    private String message;
    private HttpRequest request;

    public IllegalParameterError(String parameter, HttpRequest request, String type) {
        this.request = request;
        this.message = name + "Need " + type + ":" + parameter;
    }

    public IllegalParameterError(String parameter, HttpRequest request, String type, String extendMessage) {
        this.request = request;
        this.message = name + type + ":'" + parameter + "'" +extendMessage;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.OK;
    }
}
