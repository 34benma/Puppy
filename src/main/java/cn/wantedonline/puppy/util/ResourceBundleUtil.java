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

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import org.slf4j.Logger;

/**
 * <pre>
 *     资源包工具类
 * </pre>
 * 
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/11/17
 */
public class ResourceBundleUtil {

    /**
     * 资源包实例默认是缓存的，为了防止资源包缓存，编写ResourceBundle.Control的子类，重写getTimeToLive方法
     */
    private static class NoCacheResourceBundleControl extends ResourceBundle.Control {

        /**
         * 设置资源包为不缓存
         */
        @Override
        public long getTimeToLive(String baseName, Locale locale) {
            return ResourceBundle.Control.TTL_DONT_CACHE;
        }
    }

    private static final NoCacheResourceBundleControl noCacheResourceBundleControl = new NoCacheResourceBundleControl();

    /**
     * 重新加载资源包
     * 
     * @param filterName 资源包路径
     * @return
     */
    public static ResourceBundle reload(String filterName) {
        try {
            return ResourceBundle.getBundle(filterName, Locale.ENGLISH, noCacheResourceBundleControl);
        } catch (MissingResourceException e) {
//            log.error(e.getMessage());
            return null;
        }
    }
}
