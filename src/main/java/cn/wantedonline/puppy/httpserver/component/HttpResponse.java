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

import cn.wantedonline.puppy.httpserver.util.CharsetTools;
import cn.wantedonline.puppy.httpserver.util.HttpServerConfig;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class HttpResponse extends DefaultFullHttpResponse {
    public enum ContentType {
        html,json,plain,xml,lua
    }

    private Charset contentCharset = CharsetTools.UTF_8;
    private int contentLength = -1;
    private boolean contentSetted = false;
    private List<Cookie> cookies = new ArrayList<>(1);
    private long createTime = System.currentTimeMillis();
    private ContentType innerContentType = HttpServerConfig.getRespInnerContentType();
    private int keepAliveTimeout = HttpServerConfig.getKeepAliveTimeout();
    private boolean keepAliveTimeoutSetted = false;
    private HttpResponseStatus status;
    private ContextAttachment attach;
    private String contentString;

    public HttpResponse(ContextAttachment attach) {
        super(HttpVersion.HTTP_1_1);
        this.status = HttpResponseStatus.OK;
        this.attach = attach;
    }

    public HttpResponse() {// 初始化一些默认值
        super(HttpVersion.HTTP_1_1);
        this.status = HttpResponseStatus.OK;
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public Charset getContentCharset() {
        return contentCharset;
    }

    public String getContentString() {// 一般用于 内部查看
        if (contentString == null) {
            ByteBuf cb = this.content();
            if (cb == null) {
                return null;
            }
            return cb.toString(contentCharset);
        }
        return contentString;
    }

    @Override
    @SuppressWarnings("deprecation")
    public long getContentLength() {
        return contentLength;
    }

    public List<Cookie> getCookies() {
        return cookies;
    }

    public long getCreateTime() {
        return createTime;
    }

    public ContentType getInnerContentType() {
        return innerContentType;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    @Override
    public HttpResponseStatus getStatus() {
        return this.status;
    }

    public boolean isContentSetted() {
        return contentSetted;
    }

    /**
     * 302重定向
     *
     * @param localtionUrl 重定向的URL
     */
    public void redirect(String localtionUrl) {
        setStatus(HttpResponseStatus.FOUND);
        setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
    }

    /**
     * 重定向
     *
     * @param localtionUrl 重定向的URL
     */
    public void redirect(String localtionUrl, HttpResponseStatus status) {
        setStatus(status);
        setHeaderIfEmpty(HttpHeaders.Names.LOCATION, localtionUrl);
    }

    /** 业务代码中自定义的记录到访问日志里面的返回内容，如果为null就会默认从contentString中去取 */
    private String accessLogContent;
    /** 记录是否是二进制内容，二进制内容不显示在access日志中，否则会造成乱码影响日志检索 */
    private boolean binaryContent;

    public boolean isBinaryContent() {
        return binaryContent;
    }

    public String getAccessLogContent() {
        return accessLogContent;
    }

    /**
     * 自定义的记录到访问日志里面的返回内容
     */
    public void setAccessLogContent(String accessLogContent) {
        this.accessLogContent = accessLogContent;
    }

    @Override
    public void setContent(ByteBuf content) {
        super.setContent(content);
        if (content != null) {
            contentLength = content.readableBytes();
        }
        contentSetted = true;
        if (null == contentString) {
            binaryContent = true;
        }
    }

    public void setContentCharset(Charset contentCharset) {
        this.contentCharset = contentCharset;
    }

    public void setContentString(String contentStr) {
        this.contentString = contentStr; // 如果开启 deflate功能时，HttpContentCompressor 会在编码时setContent(channelBuffer) 为了让accesslog正常显示，这里提前存下 contentStr
        ByteBuf content = ByteBuf.copiedBuffer(contentStr, contentCharset);
        super.setContent(content);
        if (content != null) {
            contentLength = content.readableBytes();
        }
        contentSetted = true;
    }

    /**
     * 注意不要在这里设置keepAlive相关参数,要设置请使用setKeepAliveTimeout
     */
    public boolean setHeaderIfEmpty(String name, String value) {
        if (headers.get(name) == null) {
            setHeader(name, value);
            return true;
        }
        return false;
    }

    public void setInnerContentType(ContentType innerContentType) {
        this.innerContentType = innerContentType;
    }

    public void setKeepAliveTimeout(int keepAliveTimeout) {
        if (keepAliveTimeout < 0) {
            throw new IllegalArgumentException("keepAliveTimeout:" + keepAliveTimeout + " cant be nagative");
        }
        this.keepAliveTimeoutSetted = true;
        this.keepAliveTimeout = keepAliveTimeout;
    }

    @Override
    public void setStatus(HttpResponseStatus status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return getProtocolVersion().getText() + ' ' + getStatus().toString();
    }

    @Override
    public void setChunked(boolean chunked) {
        super.setChunked(chunked);
        if (chunked) {// 测试发现如果不带此header,浏览器会一直 等待(转圈圈)
            this.setHeader(HttpHeaders.Names.TRANSFER_ENCODING, HttpHeaders.Values.CHUNKED);
        } else {
            this.removeHeader(HttpHeaders.Names.TRANSFER_ENCODING);
        }
    }

    /**
     * <pre>
     * 把所有配置好的 cookies变成 实际要发送响应时用到的set-cookie: 响应头
     *
     * 此方法仅供内部使用
     */
    public void packagingCookies() {
        List<Cookie> cookies = getCookies();
        if (!cookies.isEmpty()) {
            // Reset the cookies if necessary.
            for (Cookie cookie : cookies) {
                CookieEncoder cookieEncoder = new CookieEncoder(true);
                cookieEncoder.addCookie(cookie);
                addHeader(HttpHeaders.Names.SET_COOKIE, cookieEncoder.encode());
            }
        }
    }

    public boolean isKeepAliveTimeoutSetted() {
        return keepAliveTimeoutSetted;
    }

    public ContextAttachment getAttach() {
        return attach;
    }

}
