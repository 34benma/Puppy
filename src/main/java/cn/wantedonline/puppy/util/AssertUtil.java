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

import java.util.Collection;
import java.util.Map;

/**
 * <pre>
 *      参数校验工具，校验null，empty等
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/26
 */
public class AssertUtil {
    private AssertUtil() {}

    public static boolean isNotNull(Object obj) {
        return null != obj;
    }

    public static boolean isNull(Object obj) {
        return null == obj;
    }

    public static boolean isNotEmptyCollection(Collection<?> collec) {
        return !isEmptyCollection(collec);
    }

    public static boolean isEmptyCollection(Collection<?> collec) {
        return null == collec || collec.size() == 0;
    }

    public static boolean isNotEmptyMap(Map<?, ?> map) {
        return !isEmptyMap(map);
    }

    public static boolean isEmptyMap(Map<?, ?> map) {
        return null == map || map.size() == 0;
    }

    public static boolean isEmptyArray(Object[] objects) {return null == objects || objects.length == 0;}

    public static boolean isNotEmptyArray(Object[] objects) {return !isEmptyArray(objects);}

}
