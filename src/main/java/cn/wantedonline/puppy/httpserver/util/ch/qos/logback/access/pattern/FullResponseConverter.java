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

import java.util.Map.Entry;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.CoreConstants;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.spi.AccessEvent;

public class FullResponseConverter extends AccessConverter {

    @Override
    public String convert(IAccessEvent ae) {
        StringBuilder buf = new StringBuilder();

        HttpResponse resp = ((AccessEvent) ae).getHttpResponse();
        buf.append(resp.getProtocolVersion().text());
        buf.append(' ');
        buf.append(resp.getStatus());
        buf.append(CoreConstants.LINE_SEPARATOR);

        for (Entry<String, String> e : resp.headers()) {
            buf.append(e.getKey());
            buf.append(": ");
            buf.append(e.getValue());
            buf.append(CoreConstants.LINE_SEPARATOR);
        }
        buf.append(CoreConstants.LINE_SEPARATOR);
        buf.append(ae.getResponseContent());
        buf.append(CoreConstants.LINE_SEPARATOR);
        return buf.toString();
    }
}
