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

import java.util.*;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class CollectionUtil {
    private CollectionUtil() {}

    public static boolean addAll(Collection c, Object... objs) {
        boolean result = false;
        for (Object obj : objs) {
            result |= c.add(obj);
        }
        return result;
    }

    public static <T> List<T> buildList(T... ts) {
        List<T> list = new ArrayList<T>(ts.length);
        addAll(list, ts);
        return list;
    }

    public static <T> Set<T> buildSet(T... ts) {
        Set<T> set = new HashSet<T>(ts.length * 2);
        addAll(set, ts);
        return set;
    }
}
