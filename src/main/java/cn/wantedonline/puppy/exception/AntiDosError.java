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

package cn.wantedonline.puppy.exception;


import io.netty.handler.codec.http.HttpResponseStatus;

public class AntiDosError extends AbstractHttpServerError {

    private static final long serialVersionUID = 1L;
    public final static AntiDosError INSTANCE = new AntiDosError();

    private AntiDosError() {
    }

    @Override
    public HttpResponseStatus getStatus() {
        return HttpResponseStatus.FORBIDDEN;
    }
}
