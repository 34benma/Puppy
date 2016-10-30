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

import java.io.Serializable;
import java.util.Comparator;

/**
 * Created by wangcheng on 2016/10/30.
 */
public final class CaseIgnoringComparator implements Comparator<String>, Serializable {
    private static final long serialVersionUID = -1060886965538461961L;

    public static final CaseIgnoringComparator INSTANCE = new CaseIgnoringComparator();

    private CaseIgnoringComparator() {
    }

    public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
    }

    @SuppressWarnings("MethodMayBeStatic")
    private Object readResolve() {
        return INSTANCE;
    }
}
