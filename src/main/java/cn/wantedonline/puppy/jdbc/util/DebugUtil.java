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
 * 用于在调试SQL的时候合并生成完整的SQL语句
 *
 * @since V0.5.0
 * @author thunder
 */
public class DebugUtil {

    /**
     * 合并带?的语句
     */
    public static String merge(String sql, Object... args) {
        if (args == null) {
            return sql;
        }
        String[] sqls = (sql + " ").split("\\?");
        if (args.length == sqls.length - 1) {
            StringBuilder exsql = new StringBuilder();
            int i;
            for (i = 0; i < args.length; i++) {
                exsql.append(sqls[i]);
                if (args[i] instanceof String) {
                    String src = args[i].toString().replace("'", "\\'").replace("\r", "").replace("\n", "").trim();
                    String append = src;
                    if (src.length() > 120) {
                        append = src.substring(0, 50) + "...(" + args[i].toString().length() + ")..." + src.substring(src.length() - 50, src.length());
                    }
                    exsql.append("'").append(append).append("'");
                } else {
                    exsql.append(args[i]);
                }
            }
            exsql.append(sqls[i]);
            exsql.deleteCharAt(exsql.length() - 1);
            return exsql.toString();
        }
        return "";
    }
}
