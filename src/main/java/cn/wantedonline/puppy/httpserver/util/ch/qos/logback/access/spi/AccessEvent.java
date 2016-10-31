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
package cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.spi;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.util.CharsetTools;
import cn.wantedonline.puppy.httpserver.util.IPGetterHelper;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.pattern.RemotePortConverter;
import cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.pattern.SimplifyResponseConverter;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * <pre>
 * logback-0.9.24 -> logback-0.9.28 本类没有变化
 *  
 * 
 * The Access module's internal representation of logging events. When the logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed around to the different logback components.
 * 
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable, IAccessEvent {

    private static final long serialVersionUID = -5502700134912346943L;
    // http://www.apache-korea.org/cactus/api/framework-13/javax/servlet/ServletRequest.html#getServerName()
    public final static String[] NA_STRING_ARRAY = new String[] {
        IAccessEvent.NA
    };
    public final static String EMPTY = "";

    private transient final HttpRequest httpRequest;
    private transient final HttpResponse httpResponse;

    public static void crackTest() {
    }

    static {
        PatternLayout.defaultConverterMap.put("remotePort", RemotePortConverter.class.getName());
        PatternLayout.defaultConverterMap.put("responseContentSimple", SimplifyResponseConverter.class.getName());
    }

    String protocol;
    String method;
    String requestURL;
    String requestContent;
    String responseContent;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was created.
     */
    private long timeStamp = 0;

    public AccessEvent(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.timeStamp = httpRequest.getCreateTime();
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        if (this.timeStamp != 0) {
            throw new IllegalStateException("timeStamp has been already set for this event.");
        }
        this.timeStamp = timeStamp;
    }

    /**
     * 取得的uri是不带?后面的参数的
     */
    @Override
    public String getRequestURI() {
        return httpRequest.getPath();
    }

    /**
     * The first line of the request.
     */
    @Override
    public String getRequestURL() {
        if (requestURL == null) {
            StringBuilder buf = new StringBuilder();
            buf.append(httpRequest.getMethod());
            buf.append(AccessConverter.SPACE_CHAR);
            buf.append(httpRequest.getUri());
            buf.append(AccessConverter.SPACE_CHAR);
            buf.append(httpRequest.getProtocolVersion());
            requestURL = buf.toString();
        }
        return requestURL;
    }

    @Override
    public String getRemoteHost() {
        return httpRequest.getRemoteHost();
    }

    /**
     * <pre>
     * Returns the login of the user making this request, if the user has been authenticated,
     * or null if the user has not been authenticated.
     * Whether the user name is sent with each subsequent request depends on the browser and type of authentication.
     * Same as the value of the CGI variable REMOTE_USER.
     * 
     * 此方法只适用于 servlet带session情况,因此忽略!?
     * </pre>
     * 
     * @return
     */
    @Override
    public String getRemoteUser() {
        return IAccessEvent.NA;
    }

    @Override
    public String getProtocol() {
        if (protocol == null) {
            HttpVersion v = httpRequest.getProtocolVersion();
            if (v != null) {
                String t = v.text();
                if (t == null || t.isEmpty()) {
                    protocol = IAccessEvent.NA;
                } else {
                    protocol = t;
                }
            } else {
                protocol = IAccessEvent.NA;
            }
        }
        return protocol;
    }

    @Override
    public String getMethod() {
        if (method == null) {
            HttpMethod m = httpRequest.getMethod();
            if (m == null) {
                method = IAccessEvent.NA;
            } else {
                method = m.toString();
            }
        }
        return method;
    }

    /**
     * <pre>
     * Returns the host name of the server that received the request.
     * For HTTP servlets, same as the value of the CGI variable SERVER_NAME.
     * 
     * 没什么意义,先不实现
     * </pre>
     * 
     * @return
     */
    @Override
    public String getServerName() {
        return IAccessEvent.NA;
    }

    @Override
    public String getRemoteAddr() {
        String r = IPGetterHelper.getIP(httpRequest);
        if (r.isEmpty()) {
            return IAccessEvent.NA;
        }
        return r;
    }

    @Override
    public String getRequestHeader(String key) {
        // key = key.toLowerCase();
        String result = httpRequest.headers().get(key);
        if (result != null) {
            return result;
        }
        return IAccessEvent.NA;
    }

    /**
     * DBAppender跟FullRequestConverter会用到
     */
    @Override
    public Enumeration<String> getRequestHeaderNames() {
        // return new Vector<String>(httpRequest.getHeaderNames()).elements();
        throw new UnsupportedOperationException("getRequestHeaderNames");
    }

    /**
     * RequestHeaderConverter会用到
     */
    @Override
    public Map<String, String> getRequestHeaderMap() {
        throw new UnsupportedOperationException("getRequestHeaderMap");
    }

    /**
     * Attributes are not serialized
     * 
     * @param key
     */
    @Override
    public String getAttribute(String key) {
        throw new UnsupportedOperationException("getAttribute");
    }

    /**
     * RequestParameterConverter会用到
     */
    @Override
    public String[] getRequestParameter(String key) {
        throw new UnsupportedOperationException("getRequestParameter");
    }

    @Override
    public String getCookie(String key) {
        Cookie c = httpRequest.getCookie(key);
        if (c != null) {
            return c.value();
        }
        return IAccessEvent.NA;
    }

    @Override
    public long getContentLength() {
        return httpResponse.get;
    }

    @Override
    public int getStatusCode() {
        HttpResponseStatus status = httpResponse.getStatus();
        if (status != null) {
            return status.code();
        }
        return SENTINEL;
    }

    @Override
    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }
        if (HttpMethod.POST.equals(httpRequest.getMethod()) && "application/x-www-form-urlencoded".equals(httpRequest.headers().get(HttpHeaders.Names.CONTENT_TYPE))) {
            return new String(httpRequest.content().toString(CharsetTools.UTF_8));
            // return httpRequest.getPostContentDecoder().getPostContent();
        }
        StringBuilder tmp = new StringBuilder();
        Map<String, List<String>> params = httpRequest.getParameters();
        for (Entry<String, List<String>> p : params.entrySet()) {
            String key = p.getKey();
            List<String> vals = p.getValue();
            for (String val : vals) {
                tmp.append("&").append(key).append('=').append(val);
            }
        }
        if (tmp.length() > 0) {
            requestContent = tmp.substring(1);
        } else {
            requestContent = "";
        }
        return requestContent;
    }

    @Override
    public String getResponseContent() {
        if (responseContent != null) {
            return responseContent;
        }
        if (null != httpResponse.getAccessLogContent()) { // 有设置内容，就使用设置的内容，否则就按规矩办
            responseContent = httpResponse.getAccessLogContent();
        } else if (httpResponse.isBinaryContent()) {
            responseContent = "[BINARY CONTENT]";
        } else if (getStatusCode() == HttpResponseStatus.FOUND.getCode()) { // 如果是通过redirect跳转的，此处内容就是跳转后的URL
            responseContent = "[REDIRECT] " + httpResponse.getHeader(HttpHeaders.Names.LOCATION);
        } else {
            responseContent = httpResponse.getContentString();
        }
        return responseContent;
    }

    @Override
    public int getLocalPort() {
        return httpRequest.getLocalPort();
    }

    public int getRemotePort() {
        return httpRequest.getRemotePort();
    }

    @Override
    public String getResponseHeader(String key) {
        String r = httpResponse.headers().get(key);
        if (r == null || r.isEmpty()) {
            return IAccessEvent.NA;
        }
        return r;
    }

    /**
     * 用于FullResponseConverter
     */
    @Override
    public List<String> getResponseHeaderNameList() {
        throw new UnsupportedOperationException("getResponseHeaderNameList");
    }

    @Override
    public void prepareForDeferredProcessing() {

    }

    // 以下两个是自己要用的
    public HttpRequest getHttpRequest() {
        return httpRequest;
    }

    public HttpResponse getHttpResponse() {
        return httpResponse;
    }

    // 以下方法是 0.9.28变成1.0.0时，多出来的方法，不用先
    // TODO:后续可以再整理，如让httpRequest跟servlet规范对接
    @Override
    public Map<String, String[]> getRequestParameterMap() {
        throw new UnsupportedOperationException("getRequestParameterMap");
    }

    @Override
    public ServerAdapter getServerAdapter() {
        throw new UnsupportedOperationException("getServerAdapter");
    }

    @Override
    public Map<String, String> getResponseHeaderMap() {
        throw new UnsupportedOperationException("getResponseHeaderMap");
    }

    @Override
    public HttpServletRequest getRequest() {
        throw new UnsupportedOperationException("getRequest");
    }

    @Override
    public HttpServletResponse getResponse() {
        throw new UnsupportedOperationException("getResponse");
    }

    // @CRACK 2014-2-13 这里是多出的方法，因为我们的日志里面没有使用这个，故直接返回0即可
    public long getElapsedTime() {
        return 0;
    }
}
