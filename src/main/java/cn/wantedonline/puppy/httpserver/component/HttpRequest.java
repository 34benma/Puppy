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
import cn.wantedonline.puppy.httpserver.util.CharsetTools;
import cn.wantedonline.puppy.httpserver.util.CollectionUtil;
import cn.wantedonline.puppy.httpserver.util.IPGetterHelper;
import cn.wantedonline.puppy.httpserver.util.MapUtil;
import cn.wantedonline.puppy.util.*;
import cn.wantedonline.puppy.util.HttpUtil;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.SwappedByteBuf;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.multipart.*;

import org.slf4j.Logger;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;


/**
 * Created by wangcheng on 2016/10/30.
 */
public class HttpRequest extends DefaultFullHttpRequest {
    private static final String COOKIE = "COOKIE";
    private static final HttpDataFactory factory = new DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE);
    private static final Logger logger = Log.getLogger();
    private static final String PARAMETER = "PARAMETER";

    static {
        DiskFileUpload.deleteOnExitTemporaryFile = false;
        DiskFileUpload.baseDirectory = null;
        DiskAttribute.deleteOnExitTemporaryFile = false;
        DiskAttribute.baseDirectory = null;
    }

    public void clean() {
        if (httpPostRequestDecoder != null) {
            httpPostRequestDecoder.cleanFiles();
            httpPostRequestDecoder = null;// 短连接情况下，避免都cleanFiles两次
        }
    }

    private Map<String, List<String>> initParametersByPost(Map<String, List<String>> params, HttpPostRequestDecoder httpPostRequestDecoder) {
        if (httpPostRequestDecoder == null) {
            return params;
        }

        try {
            List<InterfaceHttpData> datas = httpPostRequestDecoder.getBodyHttpDatas();
            if (datas != null) {
                for (InterfaceHttpData data : datas) {
                    if (data instanceof Attribute) {
                        Attribute attribute = (Attribute) data;
                        try {
                            String key = attribute.getName();
                            String value = attribute.getValue();

                            List<String> ori = params.get(key);
                            if (ori == null) {
                                ori = new ArrayList<String>(1);
                                params.put(key, ori);
                            }
                            ori.add(value);
                        } catch (IOException e) {
                            logger.error("cant init attribute,req:{},attribute:{}", new Object[] {
                                    this,
                                    attribute,
                                    e
                            });
                        }
                    }
                }
            }
        } catch (HttpPostRequestDecoder.NotEnoughDataDecoderException e) {
            logger.error("req:{}", this, e);
        }
        return params;
    }

    private Charset charset4ContentDecoder = CharsetTools.UTF_8;
    private Charset charset4QueryStringDecoder = CharsetTools.UTF_8;
    private Map<String, Cookie> cookies;
    private long createTime = System.currentTimeMillis();
    private HttpPostRequestDecoder httpPostRequestDecoder;

    private boolean httpPostRequestDecoderInit;
    private SocketAddress localAddress;
    private String localIP;
    private Map<String, List<String>> parameters;
    private Map<String, List<String>> parametersByPost;
    private String primitiveRemoteIP;
    private QueryStringDecoder queryStringDecoder;
    private SocketAddress remoteAddress;
    private String remoteIP;

    public HttpRequest(HttpVersion httpVersion, HttpMethod method, String uri) {
        super(httpVersion, method, uri);
    }

    private boolean getBoolean(String key, String v, String type) {
        if (null == v) {
            throw new IllegalParameterError(key, this, type);
        }
        if (v.equals("true") || v.equalsIgnoreCase("y")) {
            return true;
        }
        return false;
    }

    public Charset getCharset4QueryStringDecoder() {
        return charset4QueryStringDecoder;
    }

    public Cookie getCookie(String name) {
        return getCookies().get(name);
    }

    public Map<String, Cookie> getCookies() {
        if (null == cookies) {
            cookies = new HashMap<>();
            List<String> cookieList = headers().getAll(HttpHeaders.Names.COOKIE);
            for (String cookieString : cookieList) {
                if (StringTools.isEmpty(cookieString)) {
                    continue;
                }
                CookieDecoder.decode(cookieString, cookies);
            }
        }
        return cookies;
    }

    public String getCookieValue(String cookieName) {
        if (StringTools.isEmpty(cookieName)) {
            throw new IllegalArgumentException("cookieName isEmpty:[" + cookieName + "]");
        }
        Cookie cookie = getCookie(cookieName);
        return cookie == null ? null : cookie.value();
    }

    public String getCookieValue(String cookieName, String defaultValue) {
        String value = getCookieValue(cookieName);
        return value == null ? defaultValue : value;
    }

    public boolean getCookieValueBoolean(String cookieName) {
        return getBoolean(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public boolean getCookieValueBoolean(String cookieName, boolean defaultValue) {
        return ValueUtil.getBoolean(getCookieValue(cookieName), defaultValue);
    }

    public String getCookieValueCompelled(String cookieNmae) {
        String v = getCookieValue(cookieNmae);
        if (null == v) {
            throw new IllegalParameterError(cookieNmae, this, COOKIE);
        }
        return v;
    }

    public double getCookieValueDouble(String cookieName) {
        return getDouble(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public double getCookieValueDouble(String cookieName, int defaultValue) {
        return ValueUtil.getDouble(getCookieValue(cookieName), defaultValue);
    }

    public float getCookieValueFloat(String cookieName) {
        return getFloat(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public float getCookieValueFloat(String cookieName, int defaultValue) {
        return ValueUtil.getFloat(getCookieValue(cookieName), defaultValue);
    }

    public long getCookieValueInteger(String cookieName) {
        return getInteger(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public int getCookieValueInteger(String cookieName, int defaultValue) {
        return ValueUtil.getInteger(getCookieValue(cookieName), defaultValue);
    }

    public long getCookieValueLong(String cookieName) {
        return getLong(cookieName, getCookieValue(cookieName), COOKIE);
    }

    public long getCookieValueLong(String cookieName, long defaultValue) {
        return ValueUtil.getLong(getCookieValue(cookieName), defaultValue);
    }

    public long getCreateTime() {
        return createTime;
    }

    private double getDouble(String key, String v, String type) {
        if (v == null) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Double.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Double");
        }
    }

    private float getFloat(String key, String v, String type) {
        if (v == null) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Float.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Float");
        }
    }

    private int getInteger(String key, String v, String type) {
        if (v == null) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Integer.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Integer");
        }
    }

    private long getLong(String key, String v, String type) {
        if (v == null) {
            throw new IllegalParameterError(key, this, type);
        }
        try {
            return Long.valueOf(v);
        } catch (Exception e) {
            throw new IllegalParameterError(key, this, type, " must be Long");
        }
    }

    public HttpPostRequestDecoder getHttpPostRequestDecoder() {
        if (!httpPostRequestDecoderInit) {
            HttpMethod method = getMethod();
            if (method.equals(HttpMethod.POST) || method.equals(HttpMethod.PUT)) {
                try {
                    httpPostRequestDecoder = new HttpPostRequestDecoder(factory, this, charset4ContentDecoder);
                } catch (HttpPostRequestDecoder.ErrorDataDecoderException e) {// 这里如果解析失败,比较严重,要特别关注
                    logger.error("request postDataDecode error:{}", this, e);
                } catch (HttpPostRequestDecoder.IncompatibleDataDecoderException e) {
                }
            }
            httpPostRequestDecoderInit = true;
        }
        return httpPostRequestDecoder;
    }

    public SocketAddress getLocalAddress() {
        return localAddress;
    }

    public String getLocalIP() {
        if (localIP == null) try {
            localIP = HttpUtil.getIP((InetSocketAddress) getLocalAddress());
        } catch (Exception e) {
            logger.error("", e);
            localIP = "";
        }
        return localIP;
    }

    public int getLocalPort() {
        return ((InetSocketAddress) localAddress).getPort();
    }

    public String getParameter(String key) {
        if (StringTools.isEmpty(key)) {
            throw new IllegalArgumentException("key isEmpty:[" + key + "]");
        }
        List<String> v = getParameters().get(key);
        if (v != null) {
            return v.get(0);
        }
        return getParameterByPost(key);
    }

    public String getParameter(String key, String defaultValue) {
        String v = getParameter(key);
        if (v == null) {
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

    public String getParameterByPost(String key) {
        List<String> v = getParametersByPost().get(key);
        if (v != null) {
            return v.get(0);
        }
        return null;
    }

    public String getParameterCompelled(String key) {
        String v = getParameter(key);
        if (v == null) {
            throw new IllegalParameterError(key, this, PARAMETER);
        }
        return v;
    }

    public double getParameterDouble(String key) {
        return getDouble(key, getParameter(key), PARAMETER);
    }

    public double getParameterDouble(String key, double defaultValue) {
        return ValueUtil.getDouble(getParameter(key), defaultValue);
    }

    public float getParameterFloat(String key) {
        return getFloat(key, getParameter(key), PARAMETER);
    }

    public float getParameterFloat(String key, float defaultValue) {
        return ValueUtil.getFloat(getParameter(key), defaultValue);
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
        if (parameters == null) {
            try {
                Map<String, List<String>> params = getQueryStringDecoder().parameters();
                parameters = params;
            } catch (Exception e) {
                logger.error("queryString decode fail,req:{},{}:{}", new Object[] {
                        this,
                        e.getClass(),
                        e.getMessage()
                });
                parameters = Collections.emptyMap();
            }
        }
        return parameters;
    }

    /**
     * 动态添加参数，用以适应特殊情况
     *
     * @param kv 参数key和value，提交的参数个数必须是偶数个
     */
    public void addParameters(Object... keyvalue) {
        MapUtil.checkKeyValueLength(keyvalue);
        Map<String, List<String>> params = getParameters();
        if (Collections.EMPTY_MAP == params) {
            params = parameters = new HashMap<String, List<String>>();
        }
        for (int i = 0; i < keyvalue.length; i++) {
            String k = keyvalue[i++].toString();
            List<String> v = params.get(k);
            if (v == null) {
                Object newV = keyvalue[i];
                List newVList = newV instanceof List ? (List) newV : CollectionUtil.buildList(newV.toString());
                params.put(k, newVList);
            } else {
                v.add(keyvalue[i].toString());
            }
        }
    }

    public Map<String, List<String>> getParametersByPost() {
        if (parametersByPost == null) {
            HttpPostRequestDecoder httpPostRequestDecoder = getHttpPostRequestDecoder();
            if (httpPostRequestDecoder != null) {
                parametersByPost = initParametersByPost(new HashMap<String, List<String>>(0), httpPostRequestDecoder);
            } else {
                parametersByPost = Collections.emptyMap();
            }
        }
        return parametersByPost;
    }

    public String[] getParameterValues(String key) {
        List<String> result = getParameters().get(key);
        if (EmptyChecker.isNotEmpty(result)) {
            return (String[]) result.toArray();
        }

        result = getParametersByPost().get(key);
        if (EmptyChecker.isNotEmpty(result)) {
            return (String[]) result.toArray();
        }
        return null;
    }

    public String getPath() {
        return getQueryStringDecoder().path();
    }

    public String getPrimitiveRemoteIP() {
        if (primitiveRemoteIP == null) {
            try {
                primitiveRemoteIP = HttpUtil.getIP((InetSocketAddress) remoteAddress);
            } catch (Exception e) {
                logger.error("", e);
                primitiveRemoteIP = "";
            }
        }
        return primitiveRemoteIP;
    }

    public QueryStringDecoder getQueryStringDecoder() {
        if (queryStringDecoder == null) {
            queryStringDecoder = new QueryStringDecoder(getUri(), charset4QueryStringDecoder);
        }
        return queryStringDecoder;
    }

    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    public String getRemoteHost() {
        return ((InetSocketAddress) remoteAddress).getHostName();
    }

    public String getRemoteIP() {
        if (remoteIP == null) {
            remoteIP = IPGetterHelper.getIP(this);
        }
        return remoteIP;
    }

    public int getRemotePort() {
        return ((InetSocketAddress) remoteAddress).getPort();
    }

    //TODO: netty4 不再有HttpChunk，需要寻找解决办法，目前puppy中不需要使用该方法
//    public void offerChunk(HttpChunk chunk) throws Exception {
//        getHttpPostRequestDecoder().offer(chunk);
//    }

    public void setCharset4QueryStringDecoder(Charset charset4QueryStringDecoder) {
        this.charset4QueryStringDecoder = charset4QueryStringDecoder;
    }

    public void setLocalAddress(SocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    public void setRemoteAddress(SocketAddress remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    @Override
    public String toString() {
        return Integer.toHexString(hashCode()) + this.getRemoteAddress() + "/" + this.getMethod() + " " + this.getUri();
    }

    private String url;

    public String getUrl() {
        if (url == null) {
            String host = headers().get(HttpHeaders.Names.HOST);
            String port = getLocalPort() == 80 ? "" : ":" + getLocalPort();
            url = "http://" + (StringTools.isEmpty(host) ? getLocalIP() + port : host) + getUri();
        }
        return url;
    }

    public StringBuilder getSimpleInfo() {
        Map<String, List<String>> params = getParameters();
        Map<String, List<String>> post_params = getParametersByPost();
        int keyMaxLen = "HTTP/1.1".length();
        for (String name : headers().names()) {
            keyMaxLen = Math.max(keyMaxLen, name.length());
        }
        for (String key : params.keySet()) {
            keyMaxLen = Math.max(keyMaxLen, key.length());
        }
        for (String key : post_params.keySet()) {
            keyMaxLen = Math.max(keyMaxLen, key.length());
        }
        String fmt = "%" + (keyMaxLen + 1) + "s  %s\n";

        StringBuilder r = new StringBuilder();
        r.append(String.format(fmt, Integer.toHexString(hashCode()), getRemoteAddress()));
        r.append(String.format(fmt, getMethod(), getUrl()));

        String content = getContentString();
        if (StringTools.isNotEmpty(content)) {
            r.append("CONTENT:\n" + content + "\n");
        }

        if (!params.isEmpty()) {
            r.append("PARAM:\n");
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    r.append(String.format(fmt, key, val));
                }
            }
        }
        if (!post_params.isEmpty()) {
            r.append("POST_PARAM:\n");
            for (Entry<String, List<String>> p : post_params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    r.append(String.format(fmt, key, val));
                }
            }
        }

        Collection<Cookie> cookies = getCookies().values();
        if (EmptyChecker.isNotEmpty(cookies)) {
            r.append("COOKIES  :\n");
            for (Cookie c : cookies) {
                r.append(String.format(fmt, c.name(), c.value()));
            }
        }
        return r;
    }

    public StringBuilder getDetailInfo() {
        Map<String, List<String>> params = getParameters();
        Map<String, List<String>> post_params = getParametersByPost();
        int keyMaxLen = "HTTP/1.1".length();
        for (String name : headers().names()) {
            keyMaxLen = Math.max(keyMaxLen, name.length());
        }
        for (String key : params.keySet()) {
            keyMaxLen = Math.max(keyMaxLen, key.length());
        }
        for (String key : post_params.keySet()) {
            keyMaxLen = Math.max(keyMaxLen, key.length());
        }
        String fmt = "%" + (keyMaxLen + 1) + "s  %s\n";

        StringBuilder r = new StringBuilder("REQUEST:\n");
        r.append(String.format(fmt, getMethod(), getUrl()));
        r.append(String.format(fmt, getProtocolVersion().text(), getRemoteAddress() + "->" + getLocalAddress()));
        if (!headers().names().isEmpty()) {
            r.append("HEADER:\n");
            for (String name : headers().names()) {
                for (String value : headers().getAll(name)) {
                    r.append(String.format(fmt, name, value));
                }
            }
        }
        String content = getContentString();
        if (StringTools.isNotEmpty(content)) {
            r.append("CONTENT:\n" + content + "\n");
        }

        if (!params.isEmpty()) {
            r.append("PARAM:\n");
            for (Entry<String, List<String>> p : params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    r.append(String.format(fmt, key, val));
                }
            }
        }
        if (!post_params.isEmpty()) {
            r.append("POST_PARAM:\n");
            for (Entry<String, List<String>> p : post_params.entrySet()) {
                String key = p.getKey();
                List<String> vals = p.getValue();
                for (String val : vals) {
                    r.append(String.format(fmt, key, val));
                }
            }
        }
        return r;
    }

    public String getContentString(Charset charset) {
        ByteBuf content = content();
        return new String(content.array(), charset);
    }

    public String getContentString() {
        ByteBuf content = content();
        if (content.hasArray()) {
            return new String(content.array(), charset4ContentDecoder);
        }
        return "";
    }

    public ByteBuf getPostContent() {
        return getPostContent(ByteOrder.BIG_ENDIAN);
    }

    public ByteBuf getPostContent(ByteOrder byteOrder) {
        ByteBuf content = content();
        if (content.isReadable()) {
            byte[] contentArray = new byte[content.readableBytes()];
            content.getBytes(content.readerIndex(), contentArray);
            return new SwappedByteBuf(content).order(byteOrder).readBytes(contentArray);
        }
        return new EmptyByteBuf(ByteBufAllocator.DEFAULT);
    }

    public Charset getCharset4ContentDecoder() {
        return charset4ContentDecoder;
    }

    public void setCharset4ContentDecoder(Charset charset4ContentDecoder) {
        this.charset4ContentDecoder = charset4ContentDecoder;
    }

    public String getHeader(String name, String defaultValue) {
        String value = headers().get(name);
        if (null == value) {
            return defaultValue;
        }
        return value;
    }
}
