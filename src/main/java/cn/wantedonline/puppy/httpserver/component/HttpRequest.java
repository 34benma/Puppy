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

import cn.wantedonline.puppy.exception.IllegalParameterError;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.StringTools;
import cn.wantedonline.puppy.util.ValueUtil;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <pre>
 *     封装的HttpRequest请求对象，封装了Cookie
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/19.
 */
public class HttpRequest extends DefaultFullHttpRequest {
    private static final String COOKIE = HttpHeaders.Names.COOKIE;
    private ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;

    private Map<String, Cookie> cookiesMap;

    public HttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        super(httpVersion, method, uri);
    }

    public Map<String, Cookie> getCookies() {
        if (AssertUtil.isNull(cookiesMap)) {
            cookiesMap = new HashMap<>();
            //https://tools.ietf.org/html/rfc6265#section-5.4 Cookie Header只能有一个
            String cookieStrs = headers().get(HttpHeaders.Names.COOKIE);
            if (StringTools.isNotEmpty(cookieStrs)) {
                Set<Cookie> cookies = cookieDecoder.decode(cookieStrs);
                if (AssertUtil.isNotEmptyCollection(cookies)) {
                    for (Cookie cookie : cookies) {
                        cookiesMap.put(cookie.name(), cookie);
                    }
                    return cookiesMap;
                }
            }
        }
        return Collections.EMPTY_MAP;
    }

    public Cookie getCookie(String name) {
        return getCookies().get(name);
    }

    public String getCookieValue(String cookieName) {
        Cookie cookie = getCookie(cookieName);
        return AssertUtil.isNull(cookie) ? "" : cookie.value();
    }

    public String getCookieValue(String cookieName, String defalutValue) {
        if (StringTools.isEmpty(cookieName)) {
            throw new IllegalArgumentException("cookieName isEmpty:[" + cookieName + "]");
        }
        String value = getCookieValue(cookieName);
        return StringTools.isEmpty(value) ? defalutValue : value;
    }

    private boolean getBoolean(String key, String v, String type) {
        if (StringTools.isEmpty(v)) {
            throw new IllegalParameterError(key, this, type);
        }
        return "true".equalsIgnoreCase(v) || "y".equalsIgnoreCase(v) || "1".equalsIgnoreCase(v);
    }

    private double getDouble(String key, String v, String type) {
        if (StringTools.isEmpty(v)) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Double.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Double");
        }
    }

    private float getFloat(String key, String v, String type) {
        if (StringTools.isEmpty(v)) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Float.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Float");
        }
    }

    private int getInteger(String key, String v, String type) {
        if (StringTools.isEmpty(v)) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Integer.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Integer");
        }
    }

    public boolean getCookieValueBoolean(String cookieName) {
        return getBoolean(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public boolean getCookieValueBoolean(String cookieName, boolean defalutValue) {
        return ValueUtil.getBoolean(getCookieValue(cookieName), defalutValue);
    }

    public double getCookieValueDouble(String cookieName) {
        return getDouble(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public double getCookieValueDouble(String cookieName, double defaultValue) {
        return ValueUtil.getDouble(getCookieValue(cookieName), defaultValue);
    }

    public float getCookieValueFloat(String cookieName) {
        return getFloat(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public float getCookieValueFloat(String cookieName,  float defalutValue) {
        return ValueUtil.getFloat(getCookieValue(cookieName), defalutValue);
    }


}
