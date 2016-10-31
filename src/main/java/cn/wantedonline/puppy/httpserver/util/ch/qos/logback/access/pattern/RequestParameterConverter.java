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

import java.util.List;
import java.util.Map;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.util.OptionHelper;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.spi.AccessEvent;
import cn.wantedonline.puppy.util.EmptyChecker;
import cn.wantedonline.puppy.util.StringTools;

public class RequestParameterConverter extends AccessConverter {

    private String key;
    private boolean printAll;

    @Override
    public void start() {
        key = getFirstOption();
        if (OptionHelper.isEmpty(key)) {
            printAll = true;
            addInfo("Missing key for the request parameter,print all parameters");
        }
        super.start();
    }

    private static final String[] STRING_ESCAPE_LIST;
    static {
        STRING_ESCAPE_LIST = new String[93]; // ascii最大的需要转义的就是\(93)
        STRING_ESCAPE_LIST['\r'] = "\\r";
        STRING_ESCAPE_LIST['\n'] = "\\n";
        STRING_ESCAPE_LIST['\f'] = "\\f";
        STRING_ESCAPE_LIST['\t'] = "\\t";
        STRING_ESCAPE_LIST['\b'] = "\\b";
        STRING_ESCAPE_LIST['\\'] = "\\\\";
    }

    /**
     * 将控制字符等转义出来，使得日志能在一行之内通过grep抓取
     */
    private static String convert(String source) {
        if (StringTools.isNotEmpty(source)) {
            StringBuilder sb = new StringBuilder(source.length() + 16);
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (ch < STRING_ESCAPE_LIST.length) {
                    String append = STRING_ESCAPE_LIST[ch];
                    sb.append(null != append ? append : ch);
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return "";
    }

    @Override
    public String convert(IAccessEvent accessEvent) {
        if (!isStarted()) {
            return "INACTIVE_REQUEST_PARAM_CONV";
        }
        HttpRequest req = ((AccessEvent) accessEvent).getHttpRequest();
        if (printAll) {
            Map<String, List<String>> v = req.getParametersByPost();
            if (EmptyChecker.isEmpty(v)) {
                return convert(req.getParameters().toString() + ",{" + req.getContentString() + "}");
            }
            return convert(req.getParameters().toString() + "," + v.toString());
        }
        List<String> v = req.getParameters().get(key);
        if (v.size() == 1) {
            return v.get(0);
        }
        return convert(v.toString());
    }
}
