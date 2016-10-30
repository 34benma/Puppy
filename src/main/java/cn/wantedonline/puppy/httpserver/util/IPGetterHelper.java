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

package cn.wantedonline.puppy.httpserver.util;

import cn.wantedonline.puppy.httpserver.component.HttpRequest;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class IPGetterHelper {

    private static class DefaultIPGetter implements IPGetter {
        @Override
        public String getIP(HttpRequest request) {
            return request.getPrimitiveRemoteIP();
        }
    }

    public static final IPGetter DEFAULT_IPGETTER = new DefaultIPGetter();
    private static IPGetter CURRENT_IPGETTER = DEFAULT_IPGETTER;

    public static final String PROXY_CLIENT_IP = "Proxy-Client-IP";
    public static final String WL_PROXY_CLIENT_IP = "WL-Proxy-Client-IP";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_REAL_IP = "X-Real-IP";

    public static String getIP(HttpRequest request) {
        return CURRENT_IPGETTER.getIP(request);
    }

    public static void setIPGetter(IPGetter getter) {
        if (null != getter) {
            CURRENT_IPGETTER = getter;
        }
    }

    public static String getIP(HttpRequest request, String proxyHeader) {
        String proxyIp = request.headers().get(proxyHeader);
        if (null == proxyIp) {
            return request.getPrimitiveRemoteIP();
        }
        proxyIp = proxyIp.trim();
        if (proxyIp.isEmpty() || !proxyIp.contains(".")) {
            return request.getPrimitiveRemoteIP();
        }
        return proxyIp;
    }

    public static String getIPByXForwarded(HttpRequest request) {
        String proxyIp = request.headers().get(X_FORWARDED_FOR);
        if (null == proxyIp) {
            return request.getPrimitiveRemoteIP();
        }
        proxyIp = proxyIp.trim();
        if (proxyIp.isEmpty() || !proxyIp.contains(".")) {
            return request.getPrimitiveRemoteIP();
        }
        int index = proxyIp.indexOf(",");
        if (index > 0) {
            proxyIp = proxyIp.substring(0, index).trim();
        }
        return proxyIp;
    }

    public static String getIP(HttpRequest request, String... proxyHeaders) {
        for (int i = 0; i < proxyHeaders.length; i++) {
            String proxyIp = request.headers().get(proxyHeaders[i]);
            if (proxyIp == null) {
                continue;
            }
            proxyIp = proxyIp.trim();
            int index = proxyIp.lastIndexOf(',');
            if (index > 0 && index < proxyIp.length()) {
                proxyIp = proxyIp.substring(index+1, proxyIp.length()).trim();
            }
            if (null == proxyIp || proxyIp.isEmpty() || !proxyIp.contains(".")) {
                continue;
            }
            return proxyIp;
        }
        return request.getPrimitiveRemoteIP();
    }
}
