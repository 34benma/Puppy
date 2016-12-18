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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * <pre>
 *     负责构造Map对象时key-value对的检查和填充
 * </pre>
 *
 * @author ZengDong
 * @author wangcheng
 * @since V 0.5.0 on 2016/12/18
 */
public class MapUtil {

    /**
     * 检查key-value对是否对应
     * 
     * @param keyvalue 若干个key-value对
     */
    public static void checkKeyValueLength(Object... keyvalue) {
        if (keyvalue.length % 2 != 0) {
            throw new IllegalArgumentException("keyvalue.length is invalid:" + keyvalue.length);
        }
    }

    /**
     * 将若干个key-value对放入指定的map对象
     * 
     * @param <K> key的类型
     * @param <V> value的类型
     * @param map 要放入的Map对象
     * @param keyvalue 若干个key-value对
     * @return map
     */
    public static <K, V> Map<K, V> buildMap(Map<K, V> map, Object... keyvalue) {
        checkKeyValueLength(keyvalue);
        for (int i = 0; i < keyvalue.length; i++) {
            map.put((K) keyvalue[i++], (V) keyvalue[i]);
        }
        return map;
    }

    private MapUtil() {}
}
