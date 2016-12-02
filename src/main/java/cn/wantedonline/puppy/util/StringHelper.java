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

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 *
 * @author 迅雷 zengdong
 * @author wangcheng
 * @since V0.2.0 on 16/12/2.
 */
public class StringHelper {
    /**
     * 打印 堆栈异常
     *
     * @param tmp
     * @param ex
     * @return
     */
    public static StringBuilder printThrowable(StringBuilder tmp, Throwable ex) {
        if (AssertUtil.isNotNull(ex)) {
            StringWriter stringWriter = new StringWriter();
            PrintWriter pw = new PrintWriter(stringWriter);
            ex.printStackTrace(pw);
            tmp.append(stringWriter).append('\n');
        }
        return tmp;
    }

    /**
     * 打印 堆栈异常
     */
    public static StringBuilder printThrowable(Throwable ex) {
        return printThrowable(new StringBuilder(), ex);
    }

    private StringHelper() {
    }
}
