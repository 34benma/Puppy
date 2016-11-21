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

import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * <pre>
 *     域名-IP工具类
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/21.
 */
public class InetAddressUtil {

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

    private InetAddressUtil(){}
}
