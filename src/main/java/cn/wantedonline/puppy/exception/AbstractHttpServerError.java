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

import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author wangcheng
 * @since V0.1.0 on 2016/11/19.
 */
public class AbstractHttpServerError extends Error {
    protected HttpResponseStatus status;

    public HttpResponseStatus getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return this.getMessage();
    }
}
