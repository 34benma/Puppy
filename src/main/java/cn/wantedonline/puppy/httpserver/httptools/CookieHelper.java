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

package cn.wantedonline.puppy.httpserver.httptools;

import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.annotation.Config;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.DefaultCookie;

/**
 *  <pre>
 *      Cookie辅助类
 *  </pre>
 *
 * @author wangcheng
 * @Since V0.6.2 on 2017/01/05
 */
public final class CookieHelper {
    @Config("localhost")
    private static String domain = "wantedonline.cn";
    private CookieHelper() {}

    /**
     * 添加一个Cookie,过期时间为永久
     * @param key
     * @param value
     * @param response
     */
    public static void addCookie(String key, String value, HttpResponse response) {
        addCookie(key, value, -1, response);
    }

    public static void addCookie(String key, String value, int maxAge, HttpResponse response) {
        Cookie cookie = new DefaultCookie(key, value);
        cookie.setDomain(domain);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void removeCookie(String key, String value, HttpResponse response) {
        addCookie(key, value, 0, response);
    }
}
