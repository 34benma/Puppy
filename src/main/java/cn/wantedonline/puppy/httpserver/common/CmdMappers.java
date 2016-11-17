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

package cn.wantedonline.puppy.httpserver.common;

import cn.wantedonline.puppy.httpserver.annotation.CmdDescr;
import cn.wantedonline.puppy.util.AssertUtil;

import java.lang.reflect.Method;

/**
 * <pre>
 *     Cmd映射容器
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/17.
 */
public class CmdMappers {
    public static class CmdMeta {
        private BaseCmd cmd;
        private Method method;
        private String baseName;
        private String name;

        public CmdMeta(BaseCmd cmd, Method method) {
            this.cmd = cmd;
            this.method = method;
            this.baseName = cmd.getClass().getSimpleName() + ".*";
            this.name = cmd.getClass().getSimpleName() + "." + method.getName();
        }

        public BaseCmd getCmd() {
            return cmd;
        }

        public Method getMethod() {
            return method;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CmdMeta cmdMeta = (CmdMeta) o;

            if (!cmd.equals(cmdMeta.cmd)) return false;
            if (!method.equals(cmdMeta.method)) return false;
            if (!baseName.equals(cmdMeta.baseName)) return false;
            return name.equals(cmdMeta.name);

        }

        @Override
        public int hashCode() {
            int result = cmd.hashCode();
            result = 31 * result + method.hashCode();
            result = 31 * result + baseName.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getCmdDescription() {
            CmdDescr c = cmd.getClass().getAnnotation(CmdDescr.class);
            if (AssertUtil.isNotNull(c)) {
                return c.value();
            }
            return "";
        }
    }
}
