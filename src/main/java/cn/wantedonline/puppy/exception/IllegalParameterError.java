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
 * Created by wangcheng on 2016/10/30.
 */
public class IllegalParameterError extends AbstractHttpServerError {
    private static final long serialVersionUID = -1L;
    private static final String name = IllegalParameterError.class.getSimpleName() + ":";
    private HttpRequest request;
    private String message;

    public IllegalParameterError(String parameter, HttpRequest request, String type) {
        this.request = request;
        this.message = name + "NEED " + type + ":" + parameter;
    }

    public IllegalParameterError(String parameter, HttpRequest request, String type, String extendMessage) {
        this.request = request;
        this.message = name + type + ":'" + parameter + "'" + extendMessage;
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
