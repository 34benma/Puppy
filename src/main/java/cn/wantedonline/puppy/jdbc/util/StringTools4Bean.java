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

package cn.wantedonline.puppy.jdbc.util;

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public class StringTools4Bean {

    public static String listingObject(Object data) {
        if (data == null) {
            return "";
        }
        if (JdbcUtils.isSimpleClass(data.getClass())) {
            return data.getClass().getSimpleName() + "[" + data + "]";
        }
        return listingString(data, true);
    }

    private static String listingString(Object data, boolean snapped) {
        StringBuilder sb = new StringBuilder(100);
        sb.append(data.getClass().getSimpleName()).append("[");
        try {
            boolean flag = false;
            boolean isstring = true;
            Object obj = null;
            String str = "";
            for (java.lang.reflect.Method m : data.getClass().getDeclaredMethods()) {
                if ((m.getName().startsWith("get") || m.getName().startsWith("is")) && m.getParameterTypes().length == 0) {
                    int l = m.getName().startsWith("get") ? 3 : 2;
                    obj = m.invoke(data);
                    if (snapped && obj == null) {
                        continue;
                    }
                    isstring = obj instanceof String;
                    if (!isstring && snapped) {
                        if (obj instanceof Number && ((Number) obj).intValue() == 0) {
                            continue;
                        }
                        if (obj instanceof Boolean && ((Boolean) obj) == false) {
                            continue;
                        }
                    }
                    str = isstring ? ("\"" + obj + "\"") : String.valueOf(obj);
                    if (flag) {
                        sb.append(", ");
                    }
                    sb.append(m.getName().substring(l).toLowerCase()).append("=").append(str);
                    flag = true;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        sb.append("]");
        return sb.toString();
    }
}
