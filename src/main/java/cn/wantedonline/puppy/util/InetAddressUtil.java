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

package cn.wantedonline.puppy.util;

import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * <pre>
 *     域名-IP工具类
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/21.
 */
public class InetAddressUtil {
    private static Set<String> localIPSet;
    private static Set<String> localIPWith127001Set;
    private static String localSampleIP;

    /**
     * 获得inetSocketAddress对应的IP地址
     *
     * @param inetSocketAddress
     * @return
     */
    public static String getIP(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress != null) {
            InetAddress addr = inetSocketAddress.getAddress();
            if (addr != null) {
                return addr.getHostAddress();
            }
        }
        return "";
    }

    public static Set<String> getLocalIPWith127001() {
        if (localIPWith127001Set == null) {
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
     * 获得本地IP（除去127.0.0.1之外的IP）
     */
    public static Set<String> getLocalIP() {
        if (localIPSet == null) {
            Set<String> localIPSetTmp = new LinkedHashSet<String>(3);
            localIPSetTmp.addAll(getLocalIPWith127001());
            localIPSetTmp.remove("127.0.0.1");
            localIPSet = localIPSetTmp;
        }
        return localIPSet;
    }

    /**
     * 获得本地特征IP
     *
     * @return
     */
    public static String getLocalSampleIP() {
        if (localSampleIP == null) {
            Set<String> set = getLocalIP();
            localSampleIP = AssertUtil.isEmptyCollection(set) ? "N/A" : set.iterator().next();
        }
        return localSampleIP;
    }

    /**
     * 通过domainName获得IP地址
     *
     * @param domainName
     * @return
     */
    public static Set<String> getIPByDomainName(String domainName) {
        Set<String> domainIPSet = new LinkedHashSet<String>(2);
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

    private InetAddressUtil(){}
}
