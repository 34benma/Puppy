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
import cn.wantedonline.puppy.util.*;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import io.netty.handler.codec.http.multipart.*;
import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.Charset;
import java.util.*;

/**
 * <pre>
 *     封装的HttpRequest请求对象，封装了Cookie
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/19.
 */
public class HttpRequest extends DefaultFullHttpRequest {
    private Logger log = Log.getLogger(HttpRequest.class);

    private static final String COOKIE = HttpHeaders.Names.COOKIE;
    private static final String PARAMETER = "Parameter";

    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);

    private long createTime = System.currentTimeMillis();

    private ServerCookieDecoder cookieDecoder = ServerCookieDecoder.STRICT;
    private QueryStringDecoder queryStringDecoder;

    private HttpPostRequestDecoder httpPostRequestDecoder;

    private boolean httpPostRequestDecoderInit;
    private SocketAddress localAddress;
    private SocketAddress remoteAddress;
    private String localIp;
    private String remoteIp;
    private String primitiveRemoteIp;

    private Map<String, List<String>> parametersByGet;
    private Map<String, List<String>> parametersByPost;
    private Map<String, Cookie> cookiesMap;

    private Charset charset4ContentDecoder = CharsetTools.UTF_8;
    private Charset charset4QueryStringDecoder = CharsetTools.UTF_8;

    public HttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        super(httpVersion, method, uri, Unpooled.EMPTY_BUFFER);
    }

    public HttpRequest(HttpVersion httpVersion, HttpMethod method, String uri, boolean validateHeaders) {
        super(httpVersion, method, uri, Unpooled.EMPTY_BUFFER,validateHeaders);
    }

    public void clean() {
        //TODO:清理资源,比如长连接或者创建了文件资源等
    }

    public long getCreateTime() {
        return createTime;
    }

    public Charset getCharset4QueryStringDecoder() {
        return charset4QueryStringDecoder;
    }

    public HttpPostRequestDecoder getHttpPostRequestDecoder() {
        if (!httpPostRequestDecoderInit) {
            HttpMethod method = getMethod();
            if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
                try {
                    httpPostRequestDecoder = new HttpPostRequestDecoder(factory, this, charset4ContentDecoder);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {
                    log.error("request postDataDecode error:{}", this, e);
                } catch (HttpPostRequestDecoder.IncompatibleDataDecoderException e) {}
            }
            httpPostRequestDecoderInit = true;
        }
        return httpPostRequestDecoder;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public void setLocalAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public SocketAddress getRemoteAddress() {return remoteAddress; }

    public String getLocalIp() {
        if (StringTools.isEmpty(localIp)) {
            try {
                localIp = InetAddressUtil.getIP((InetSocketAddress)localAddress);
            } catch (Exception e) {
                log.error("",e);
            }
        }
        return localIp;
    }

    public String getHeader(String headName) {
        return headers().get(headName);
    }

    public Map<String, List<String>> getHeaders() {
        Map<String, List<String>> headers = new HashMap<>(headers().names().size());
        for (String headerName : headers().names()) {
            headers.put(headerName, headers().getAll(headerName));
        }
        return headers;
    }

    public String getRemoteHost() {
        return ((InetSocketAddress)remoteAddress).getHostName();
    }

    public String getPrimitiveRemoteIp() {
        if (StringTools.isEmpty(primitiveRemoteIp)) {
            try {
                primitiveRemoteIp = InetAddressUtil.getIP((InetSocketAddress)remoteAddress);
            } catch (Exception e) {
                log.error("", e);
                primitiveRemoteIp = "";
            }
        }
        return primitiveRemoteIp;
    }

    public int getLocalPort() {
        return ((InetSocketAddress)localAddress).getPort();
    }

    public int getRemotePort() {
        return ((InetSocketAddress)remoteAddress).getPort();
    }

    public String getPath() {
        return getQueryStringDecoder().path();
    }

    public QueryStringDecoder getQueryStringDecoder() {
        if (AssertUtil.isNull(queryStringDecoder)) {
            queryStringDecoder = new QueryStringDecoder(getUri(), charset4QueryStringDecoder);
        }
        return queryStringDecoder;
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

    private long getLong(String key, String v, String type) {
        if (StringTools.isEmpty(v)) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Long.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Long");
        }
    }

    private Map<String, List<String>> initParametersByPost(Map<String, List<String>> params, HttpPostRequestDecoder httpPostRequestDecoder) {
        if (AssertUtil.isNull(httpPostRequestDecoder)) {
            return params;
        }

        try {
            List<InterfaceHttpData> datas = httpPostRequestDecoder.getBodyHttpDatas();
            if (AssertUtil.isNotEmptyCollection(datas)) {
                for (InterfaceHttpData data : datas) {
                    if (data instanceof Attribute) {
                        Attribute attribute = (Attribute) data;
                        try {
                            String key = attribute.getName();
                            String value = attribute.getValue();

                            List<String> ori = params.get(key);
                            if (AssertUtil.isEmptyCollection(ori)) {
                                ori = new ArrayList<>(1);
                                params.put(key, ori);
                            }
                            ori.add(value);
                        } catch (IOException e) {
                            log.error("cant init attribute,req:{},attribute:{}", new Object[] {
                                    this,
                                    attribute,
                                    e
                            });
                        }
                    }
                }
            }
        } catch (HttpPostRequestDecoder.NotEnoughDataDecoderException e) {
            log.error("req:{}", this, e);
        }
        return params;
    }

    private Map<String, List<String>> getParametersbyPost() {
        if (AssertUtil.isEmptyMap(parametersByPost)) {
            HttpPostRequestDecoder httpPostRequestDecoder = getHttpPostRequestDecoder();
            if (AssertUtil.isNotNull(httpPostRequestDecoder)) {
                parametersByPost = initParametersByPost(new HashMap<String, List<String>>(0), httpPostRequestDecoder);
            } else {
                parametersByPost = Collections.emptyMap();
            }
        }
        return parametersByPost;
    }

    public String getParameter(String key) {
        if (StringTools.isEmpty(key)) {
            throw new IllegalArgumentException("key is empty:[" + key + "]");
        }
        //先从get集合获取
        List<String> v = getParametersByGet().get(key);
        if (AssertUtil.isNotEmptyCollection(v)) {
            return v.get(0);
        }
        return getParametersbyPost(key);
    }

    public String getParameter(String key, String defaultValue) {
        String v = getParameter(key);
        if (StringTools.isEmpty(v)) {
            return defaultValue;
        }
        return v;
    }

    public boolean getParameterBoolean(String key) {
        return getBoolean(key, getParameter(key), PARAMETER);
    }

    public boolean getParameterBoolean(String key, boolean defaultValue) {
        return ValueUtil.getBoolean(getParameter(key), defaultValue);
    }

    public float getParameterFloat(String key) {
        return getFloat(key, getParameter(key), PARAMETER);
    }

    public float getParameterFloat(String key, float defalutValue) {
        return ValueUtil.getFloat(getParameter(key), defalutValue);
    }

    public double getParameterDouble(String key) {
        return getDouble(key, getParameter(key), PARAMETER);
    }

    public double getParameterDouble(String key, double defaultValue) {
        return ValueUtil.getDouble(getParameter(key), defaultValue);
    }

    public int getParameterInteger(String key) {
        return getInteger(key, getParameter(key), PARAMETER);
    }

    public int getParameterInteger(String key, int defaultValue) {
        return ValueUtil.getInteger(getParameter(key), defaultValue);
    }

    public long getParameterLong(String key) {
        return getLong(key, getParameter(key), PARAMETER);
    }

    public long getParameterLong(String key, long defaultValue) {
        return ValueUtil.getLong(getParameter(key), defaultValue);
    }

    public Map<String, List<String>> getParameters() {
        Map<String, List<String>> rtnMap = getParametersByGet();
        if (AssertUtil.isNotEmptyMap(rtnMap)) {
            return getParametersbyPost();
        }
    }

    /**
     * 和{@link getParameter(String key)}的区别在于本方法返回的是一个数组
     * @param key
     * @return
     */
    public String[] getParameterValues(String key) {
        List<String> result = getParametersByGet().get(key);
        if (AssertUtil.isNotEmptyCollection(result)) {
            return (String[])result.toArray();
        }

        result = getParametersbyPost().get(key);
        if (AssertUtil.isNotEmptyCollection(result)) {
            return (String[])result.toArray();
        }
        return null;
    }

    private String getParametersbyPost(String key) {
        List<String> v = getParametersbyPost().get(key);
        if (AssertUtil.isNotEmptyCollection(v)) {
            return v.get(0);
        }
        return null;
    }

    private Map<String, List<String>> getParametersByGet() {
        if (AssertUtil.isNull(parametersByGet)) {
            try {
                Map<String, List<String>> params = getQueryStringDecoder().parameters();
                parametersByGet = params;
            } catch (Exception e) {
                log.error("queryString decode fail,req:{},{}:{}", new Object[] {
                        this,
                        e.getClass(),
                        e.getMessage()
                });
                parametersByGet = Collections.EMPTY_MAP;
            }
        }
        return parametersByGet;
    }

    private Map<String, Cookie> getCookies() {
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

    public int getCookieValueInteger(String cookieName) {
        return getInteger(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public int getCookieValueInteger(String cookieName, int defaultValue) {
        return ValueUtil.getInteger(getCookieValue(cookieName), defaultValue);
    }

    public long getCookieValueLong(String cookieName) {
        return getLong(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public long getCookieValueLong(String cookieName, int defaultValue) {
        return ValueUtil.getLong(getCookieValue(cookieName), defaultValue);
    }

}
