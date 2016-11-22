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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.CookieEncoder;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/22.
 */
public class HttpResponse extends DefaultFullHttpResponse {

    private Charset contentCharset = CharsetTools.UTF_8;
    private int contentLength = -1;
    private List<Cookie> cookies = new ArrayList<Cookie>(1);
    private long createTime = System.currentTimeMillis();
    private ContentType innerContentType = HttpServerConfig.getRespInnerContentType();
    private ContextAttachment attach;
    private String contentString;

    public HttpResponse() {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
    }

    public HttpResponse(ContextAttachment attach) {
        super(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        this.attach = attach;
    }

    public void addCookie(Cookie cookie) {
        this.cookies.add(cookie);
    }

    public Charset getContentCharset() {
        return contentCharset;
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

    public int getContentLength() {
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

    @Override
    public HttpResponseStatus getStatus() {
        return super.getStatus();
    }

}
