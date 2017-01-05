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

import cn.wantedonline.puppy.httpserver.common.ContentType;
import cn.wantedonline.puppy.httpserver.common.HttpServerConfig;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.CharsetTools;
import cn.wantedonline.puppy.util.StringTools;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.ClientCookieEncoder;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieEncoder;
import io.netty.handler.codec.http.cookie.DefaultCookie;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/22.
 */
public class HttpResponse extends DefaultFullHttpResponse {

    private List<Cookie> cookies = new ArrayList<Cookie>(1);
    private long createTime = System.currentTimeMillis();
    private ContentType innerContentType = HttpServerConfig.getRespInnerContentType();
    private ContextAttachment attach;
    private int contentLength = -1;
    private Charset contentCharset = CharsetTools.UTF_8;
    private String contentString;
    private boolean binaryContent; //是否为二进制内容
    private boolean contentSetted = false;

    public HttpResponse() {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public HttpResponse(HttpVersion version, HttpResponseStatus status) {
        super(version, status);
    }

    public HttpResponse(ContextAttachment attach) {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.attach = attach;
    }

    /**
     * 使用DefaultCookie
     * @param cookie
     */
    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public boolean isBinaryContent() {
        return binaryContent;
    }

    public Charset getContentCharset() {
        return contentCharset;
    }

    public void setContentCharset(Charset contentCharset) {
        this.contentCharset = contentCharset;
    }

    public String getContentString() {
        if (StringTools.isEmpty(contentString)) {
            ByteBuf buf = this.content();
            if (AssertUtil.isNull(buf)) {
                return null;
            }
            return buf.toString(contentCharset);
        }
        return contentString;
    }

    public void setContent(ByteBuf contentBuf) {
        content().writeBytes(contentBuf);
        if (AssertUtil.isNotNull(contentBuf)) {
            contentLength = content().readableBytes();
        }
        contentSetted = true;
        if (StringTools.isEmpty(contentString)) {
            binaryContent = true;
        }
    }

    public void setContentString(String contentStr) {
        if (StringTools.isEmpty(contentStr)) {
            return;
        }
        this.contentString = contentStr;
        content().writeBytes(contentStr.getBytes(contentCharset));
        contentLength = content().readableBytes();
        contentSetted = true;
    }

    public int getContentLength() {
        return contentLength;
    }

    public boolean isContentSetted() {
        return contentSetted;
    }

    /**
     * 重定向
     * @param locationUrl
     */
    public void redirect(String locationUrl) {
        redirect(locationUrl, HttpResponseStatus.FOUND);
    }

    /**
     * 自定义返回码的重定向
     * @param locationUrl
     * @param status
     */
    public void redirect(String locationUrl, HttpResponseStatus status) {
        setStatus(status);
        setHeaderIfEmpty(HttpHeaders.Names.LOCATION, locationUrl);
    }

    public boolean setHeaderIfEmpty(String name, String value) {
        if (AssertUtil.isNull(headers().get(name))) {
            headers().set(name, value);
            return true;
        }
        return false;
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

    public void setInnerContentType(ContentType innerContentType) { this.innerContentType = innerContentType; }

    @Override
    public HttpResponseStatus getStatus() {
        return super.getStatus();
    }

    public void packagingCookies() {
        List<Cookie> cookies = getCookies();
        if (AssertUtil.isNotEmptyCollection(cookies)) {
            headers().add(HttpHeaders.Names.SET_COOKIE, ClientCookieEncoder.STRICT.encode(cookies));
        }
    }

}
