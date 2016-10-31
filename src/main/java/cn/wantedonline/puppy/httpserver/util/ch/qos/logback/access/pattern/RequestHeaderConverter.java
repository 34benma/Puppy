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
package cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.pattern;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.util.OptionHelper;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.spi.AccessEvent;

public class RequestHeaderConverter extends AccessConverter {

    String key;

    @Override
    public void start() {
        key = getFirstOption();
        if (OptionHelper.isEmpty(key)) {
            addWarn("Missing key for the requested header. Defaulting to all keys.");
            key = null;
        }
        super.start();
    }

    @Override
    public String convert(IAccessEvent accessEvent) {
        if (!isStarted()) {
            return "INACTIVE_HEADER_CONV";
        }
        if (key != null) {
            return accessEvent.getRequestHeader(key);
        }
        HttpRequest req = ((AccessEvent) accessEvent).getHttpRequest();
        return req.headers().toString();
    }
}
