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
package cn.wantedonline.puppy.httpserver.component;

import ch.qos.logback.access.AccessConstants;
import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.access.spi.ServerAdapter;
import cn.wantedonline.puppy.util.AssertUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

// Contributors:  Joern Huxhorn (see also bug #110)

/**
 * <pre>
 *     改造Logback提供的AccessEvent，适配Puppy的request和response
 *     http://logback.qos.ch/access.html
 * </pre>
 *
 * The Access module's internal representation of logging events. When the
 * logging component instance is called in the container to log then a
 * <code>AccessEvent</code> instance is created. This instance is passed
 * around to the different logback components.
 *
 * @author Ceki G&uuml;lc&uuml;
 * @author S&eacute;bastien Pennec
 */
public class AccessEvent implements Serializable, IAccessEvent {

    private static final long serialVersionUID = 866718993618836343L;

    private static final String EMPTY = "";

    private transient final HttpRequest httpRequest;
    private transient final HttpResponse httpResponse;

    String queryString;
    String requestURI;
    String requestURL;
    String remoteHost;
    String remoteUser;
    String remoteAddr;
    String threadName;
    String protocol;
    String method;
    String serverName;
    String requestContent;
    String responseContent;
    String sessionID;
    long elapsedTime;

    Map<String, String> requestHeaderMap;
    Map<String, String[]> requestParameterMap;
    Map<String, String> responseHeaderMap;
    Map<String, Object> attributeMap;

    long contentLength = SENTINEL;
    int statusCode = SENTINEL;
    int localPort = SENTINEL;

    /**
     * The number of milliseconds elapsed from 1/1/1970 until logging event was
     * created.
     */
    private long timeStamp = 0;

    public AccessEvent(HttpRequest httpRequest, HttpResponse httpResponse) {
        this.httpRequest = httpRequest;
        this.httpResponse = httpResponse;
        this.timeStamp = System.currentTimeMillis();
        this.elapsedTime = httpRequest.getCreateTime();
    }

    /**
     * <strong>因为Puppy的HttpRequest没有继承HttpServletRequest接口，因此总是返回null</strong>
     * 可以调用getMyRequest获得Puppy的HttpRequest
     * Returns the underlying HttpServletRequest. After serialization the returned
     * value will be null.
     *
     * @return
     */
    @Override
    public HttpServletRequest getRequest() {
        return null;
    }

    public HttpRequest getMyRequest() {
        return httpRequest;
    }

    /**
     * <strong>因为Puppy的HttpResponse没有继承HttServletResponse接口,总是返回null</strong>
     * 可以调用getMyResponse获得puppy的HttpResponse
     * Returns the underlying HttpServletResponse. After serialization the returned
     * value will be null.
     *
     * @return
     */
    @Override
    public HttpServletResponse getResponse() {
        return null;
    }

    public HttpResponse getMyResponse() {
        return httpResponse;
    }

    @Override
    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        if (this.timeStamp != 0) {
            throw new IllegalStateException("timeStamp has been already set for this event.");
        } else {
            this.timeStamp = timeStamp;
        }
    }

    /**
     * @param threadName The threadName to set.
     */
    public void setThreadName(String threadName) {
        this.threadName = threadName;
    }

    @Override
    public String getThreadName() {
        return threadName == null ? NA : threadName;
    }

    @Override
    public String getRequestURI() {
        if (requestURI == null) {
            if (httpRequest != null) {
                requestURI = httpRequest.getRequestURI();
            } else {
                requestURI = NA;
            }
        }
        return requestURI;
    }

    @Override
    public String getQueryString() {
        if (queryString == null) {
            if (httpRequest != null) {
                StringBuilder buf = new StringBuilder();
                final String qStr = httpRequest.getQueryString();
                if (qStr != null) {
                    buf.append(AccessConverter.QUESTION_CHAR);
                    buf.append(qStr);
                }
                queryString = buf.toString();
            } else {
                queryString = NA;
            }
        }
        return queryString;
    }

    /**
     * The first line of the request.
     */
    @Override
    public String getRequestURL() {
        if (requestURL == null) {
            if (httpRequest != null) {
                StringBuilder buf = new StringBuilder();
                buf.append(httpRequest.getMethod());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(httpRequest.getRequestURI());
                buf.append(getQueryString());
                buf.append(AccessConverter.SPACE_CHAR);
                buf.append(httpRequest.getProtocol());
                requestURL = buf.toString();
            } else {
                requestURL = NA;
            }
        }
        return requestURL;
    }

    @Override
    public String getRemoteHost() {
        if (remoteHost == null) {
            if (httpRequest != null) {
                // the underlying implementation of HttpServletRequest will
                // determine if remote lookup will be performed
                remoteHost = httpRequest.getRemoteHost();
            } else {
                remoteHost = NA;
            }
        }
        return remoteHost;
    }

    /**
     * puppy 目前总是返回null
     * @return
     */
    @Override
    public String getRemoteUser() {
        if (remoteUser == null) {
            if (httpRequest != null) {
                remoteUser = httpRequest.getRemoteUser();
            } else {
                remoteUser = NA;
            }
        }
        return remoteUser;
    }

    @Override
    public String getProtocol() {
        if (protocol == null) {
            if (httpRequest != null) {
                protocol = httpRequest.getProtocol();
            } else {
                protocol = NA;
            }
        }
        return protocol;
    }

    @Override
    public String getMethod() {
        if (method == null) {
            if (httpRequest != null) {
                method = httpRequest.getMethodStr();
            } else {
                method = NA;
            }
        }
        return method;
    }

    @Override
    public String getSessionID() {
        if (sessionID == null) {
            if (httpRequest != null) {
                final HttpSession session = httpRequest.getSession();
                if (session != null) {
                    sessionID = session.getId();
                }
            } else {
                sessionID = NA;
            }
        }
        return sessionID;
    }

    @Override
    public String getServerName() {
        if (serverName == null) {
            if (httpRequest != null) {
                serverName = httpRequest.getServerName();
            } else {
                serverName = NA;
            }
        }
        return serverName;
    }

    @Override
    public String getRemoteAddr() {
        if (remoteAddr == null) {
            if (httpRequest != null) {
                remoteAddr = httpRequest.getPrimitiveRemoteIp();
            } else {
                remoteAddr = NA;
            }
        }
        return remoteAddr;
    }

    @Override
    public String getRequestHeader(String key) {
        String result = null;
        key = key.toLowerCase();
        if (requestHeaderMap == null) {
            if (httpRequest != null) {
                buildRequestHeaderMap();
                result = requestHeaderMap.get(key);
            }
        } else {
            result = requestHeaderMap.get(key);
        }

        if (result != null) {
            return result;
        } else {
            return NA;
        }
    }

    @Override
    public Enumeration<String> getRequestHeaderNames() {
        Vector<String> list = new Vector<>();
        // post-serialization
        if (httpRequest == null) {
            list.addAll(getRequestHeaderMap().keySet());
        } else {
            list.addAll(httpRequest.headers().names());
        }
        return list.elements();
    }

    @Override
    public Map<String, String> getRequestHeaderMap() {
        if (requestHeaderMap == null) {
            buildRequestHeaderMap();
        }
        return requestHeaderMap;
    }

    public void buildRequestHeaderMap() {
        // according to RFC 2616 header names are case insensitive
        // latest versions of Tomcat return header names in lower-case
        requestHeaderMap = new TreeMap<String, String>(String.CASE_INSENSITIVE_ORDER);
        Vector<String> list = new Vector<>();
        list.addAll(httpRequest.headers().names());
        Enumeration<String> e = list.elements();
        if (e == null) {
            return;
        }
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            requestHeaderMap.put(key, httpRequest.getHeader(key));
        }
    }

    public void buildRequestParameterMap() {
        requestParameterMap = new HashMap<String, String[]>();
        Enumeration<String> e = httpRequest.getParameterNames();
        if (e == null) {
            return;
        }
        while (e.hasMoreElements()) {
            String key = e.nextElement();
            requestParameterMap.put(key, httpRequest.getParameterValues(key));
        }
    }

    @Override
    public Map<String, String[]> getRequestParameterMap() {
        if (requestParameterMap == null) {
            buildRequestParameterMap();
        }
        return requestParameterMap;
    }

    @Override
    public String getAttribute(String key) {
        Object value = null;
        if (attributeMap != null) {
            // Event was prepared for deferred processing so we have a copy of attribute map and must use that copy
            value = attributeMap.get(key);
        } else if (httpRequest != null) {
            // We have original request so take attribute from it
            value = httpRequest.getParameter(key);
        }

        return value != null ? value.toString() : NA;
    }

    private void copyAttributeMap() {

        if (httpRequest == null) {
            return;
        }

        attributeMap = new HashMap<String, Object>();

        Enumeration<String> names = httpRequest.getParameterNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();

            Object value = httpRequest.getParameter(name);
            if (shouldCopyAttribute(name, value)) {
                attributeMap.put(name, value);
            }
        }
    }

    private boolean shouldCopyAttribute(String name, Object value) {
        if (AccessConstants.LB_INPUT_BUFFER.equals(name) || AccessConstants.LB_OUTPUT_BUFFER.equals(name)) {
            // Do not copy attributes used by logback internally - these are available via other getters anyway
            return false;
        } else if (value == null) {
            // No reasons to copy nulls - Map.get() will return null for missing keys and the list of attribute
            // names is not available through IAccessEvent
            return false;
        } else {
            // Only copy what is serializable
            return value instanceof Serializable;
        }
    }

    @Override
    public String[] getRequestParameter(String key) {
        if (httpRequest != null) {
            String[] value = httpRequest.getParameterValues(key);
            if (value == null) {
                return new String[] { NA };
            } else {
                return value;
            }
        } else {
            return new String[] { NA };
        }
    }

    @Override
    public String getCookie(String key) {

        if (httpRequest != null) {
            return httpRequest.getCookieValue(key);
        }
        return NA;
    }

    @Override
    public long getContentLength() {
        if (contentLength == SENTINEL) {
            if (httpResponse != null) {
                contentLength = httpResponse.getContentLength();
                return contentLength;
            }
        }
        return contentLength;
    }

    public int getStatusCode() {
        if (statusCode == SENTINEL) {
            if (httpResponse != null) {
                statusCode = httpResponse.getStatus().code();
            }
        }
        return statusCode;
    }

    public long getElapsedSeconds() {
        return elapsedTime < 0 ? elapsedTime : elapsedTime / 1000;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public String getRequestContent() {
        if (requestContent != null) {
            return requestContent;
        }
        //TODO:目前不返回实体内容
        requestContent = EMPTY;
        return requestContent;
    }

    public String getResponseContent() {
        if (responseContent != null) {
            return responseContent;
        }
        //TODO:目前不返回实体内容
        responseContent = EMPTY;
        return responseContent;
    }

    public int getLocalPort() {
        if (localPort == SENTINEL) {
            if (httpRequest != null) {
                localPort = httpRequest.getLocalPort();
            }

        }
        return localPort;
    }

    @Override
    public ServerAdapter getServerAdapter() {
        return null;
    }

    public String getResponseHeader(String key) {
        buildResponseHeaderMap();
        return responseHeaderMap.get(key);
    }

    void buildResponseHeaderMap() {
        if (responseHeaderMap == null) {
            Set<String> names = httpResponse.headers().names();
            if (AssertUtil.isNotEmptyCollection(names)) {
                Map<String, String> tmpMap = new HashMap<>(names.size());
                for (String name : names) {
                    tmpMap.put(name, httpResponse.headers().get(name));
                }
                responseHeaderMap = tmpMap;
            }
        }
    }

    public Map<String, String> getResponseHeaderMap() {
        buildResponseHeaderMap();
        return responseHeaderMap;
    }

    public List<String> getResponseHeaderNameList() {
        buildResponseHeaderMap();
        return new ArrayList<String>(responseHeaderMap.keySet());
    }

    public void prepareForDeferredProcessing() {
        getRequestHeaderMap();
        getRequestParameterMap();
        getResponseHeaderMap();
        getLocalPort();
        getMethod();
        getProtocol();
        getRemoteAddr();
        getRemoteHost();
        getRemoteUser();
        getRequestURI();
        getRequestURL();
        getServerName();
        getTimeStamp();
        getElapsedTime();

        getStatusCode();
        getContentLength();
        getRequestContent();
        getResponseContent();

        copyAttributeMap();
    }
}
