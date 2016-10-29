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

package cn.wantedonline.puppy.util;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by wangcheng on 2016/10/29.
 * 提供获得IP地址的方法
 */
public class HttpUtil {
    private static Set<String> localIpSet;
    private static Set<String> localIPWith127001Set;
    private static String localSampleIP;

    public static String getIP(InetSocketAddress inetSocketAddress) {
        if (null != inetSocketAddress) {
            InetAddress address = inetSocketAddress.getAddress();
            if (null != address) {
                return address.getHostAddress();
            }
        }
        return "";
    }

    /**
     * 获得包含127.0.0.1在内的全部本地IP
     * @return
     */
    public static Set<String> getLocalIPWith127001() {
        if (null == localIPWith127001Set) {
            Set<String> localIPSetTmp = new LinkedHashSet<String>(3);
            try {
                Enumeration<?> e1 = NetworkInterface.getNetworkInterfaces();
                while (e1.hasMoreElements()) {
                    NetworkInterface ni = (NetworkInterface) e1.nextElement();
                    Enumeration<?> e2 = ni.getInetAddresses();
                    while (e2.hasMoreElements()) {
                        InetAddress ia = (InetAddress) e2.nextElement();
                        if (ia instanceof Inet6Address) {
                            continue;
                        }
                        String ip = ia.getHostAddress();
                        localIPSetTmp.add(ip);
                    }
                }
            } catch (SocketException e) {
                e.printStackTrace();
            }
            localIPWith127001Set = localIPSetTmp;
        }
        return localIPWith127001Set;
    }

    /**
     * 获得除127.0.0.1之外的IP
     * @return
     */
    public static Set<String> getLocalIP() {
        if (null == localIpSet) {
            Set<String> localIpSetTmp = new LinkedHashSet<>(3);
            localIpSetTmp.addAll(getLocalIPWith127001());
            localIpSetTmp.remove("127.0.0.1");
            localIpSet = localIpSetTmp;
        }
        return localIpSet;
    }

    public static String getLocalSampleIP() {
        if (null == localSampleIP) {
            Set<String> set = getLocalIP();
            localSampleIP = EmptyChecker.isEmpty(set) ? "N/A" : set.iterator().next();
        }
        return localSampleIP;
    }

    public static Set<String> getIPByDomainName(String domainName) {
        Set<String> domainIPSet = new LinkedHashSet<>(2);
        try {
            InetAddress[] inets = InetAddress.getAllByName(domainName);
            for (InetAddress inetAddress : inets) {
                domainIPSet.add(inetAddress.getHostAddress());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return domainIPSet;
    }

}
