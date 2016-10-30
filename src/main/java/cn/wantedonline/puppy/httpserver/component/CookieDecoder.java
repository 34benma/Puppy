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

package cn.wantedonline.puppy.httpserver.component;

import cn.wantedonline.puppy.httpserver.util.CaseIgnoringComparator;
import cn.wantedonline.puppy.util.Log;
import io.netty.handler.codec.http.Cookie;
import io.netty.handler.codec.http.DefaultCookie;
import org.slf4j.Logger;

import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Map;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class CookieDecoder {
    private static final Logger logger = Log.getLogger();

    public static final CookieDecoder INSTANCE = new CookieDecoder();
    public static final Set<String> RESERVED_NAMES = new TreeSet<>(CaseIgnoringComparator.INSTANCE);
    static {
        RESERVED_NAMES.add("$Domain");
        RESERVED_NAMES.add("$Path");
        RESERVED_NAMES.add("$Comment");
        RESERVED_NAMES.add("$CommentURL");
        RESERVED_NAMES.add("$Discard");
        RESERVED_NAMES.add("$Port");
        RESERVED_NAMES.add("$Max-Age");
        RESERVED_NAMES.add("$Expires");
        RESERVED_NAMES.add("$Version");
        RESERVED_NAMES.add("$Secure");
        RESERVED_NAMES.add("$HTTPOnly");
    }

    public static String stripQuote(String value) {
        if ((value.startsWith("\"")) && value.endsWith("\"")) {
            try {
                return value.substring(1, value.length() - 1);
            } catch (Exception ex) {}
        }
        return value;
    }

    public static Map<String, Cookie> decode(String cookieString, Map<String, Cookie> map) {
        StringTokenizer tok = new StringTokenizer(cookieString, ";", false);
        while (tok.hasMoreElements()) {
            String token = tok.nextToken();
            int i = token.indexOf("=");
            if (i > -1) {
                String name = token.substring(0, i).trim();
                if (RESERVED_NAMES.contains(name)) {
                    continue;
                }
                String value = stripQuote(token.substring(i+1).trim());
                try {
                    map.put(name, new DefaultCookie(name, value));
                } catch (Exception e) {
                    logger.warn("new DefaultCookie fail,name:{},value:{}", new Object[] {
                            name,
                            value
                    });
                }
            } else {}
        }
        return map;
    }

}
