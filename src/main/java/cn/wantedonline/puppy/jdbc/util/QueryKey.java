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

import java.util.Arrays;

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public class QueryKey {

    private Class<?> clazz;
    private String[] args;
    private boolean isInclude;

    public QueryKey(Class<?> clazz, String[] args, boolean isInclude) {
        this.clazz = clazz;
        this.args = args;
        this.isInclude = isInclude;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public String[] getArgs() {
        return args;
    }

    public boolean isInclude() {
        return isInclude;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(args);
        result = prime * result + ((clazz == null) ? 0 : clazz.hashCode());
        result = prime * result + (isInclude ? 1231 : 1237);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        QueryKey other = (QueryKey) obj;
        if (!Arrays.equals(args, other.args)) {
            return false;
        }
        if (clazz == null) {
            if (other.clazz != null) {
                return false;
            }
        } else if (!clazz.equals(other.clazz)) {
            return false;
        }
        if (isInclude != other.isInclude) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "QueryKey[" + clazz.getSimpleName() + (isInclude ? ".include." : ".exclude.") + Arrays.toString(args) + "]";
    }
}
