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

import java.lang.reflect.Field;

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public abstract class AbstractFieldsIterator {

    private Object data;

    public AbstractFieldsIterator(Object data) {
        this.data = data;
    }

    public void run() {
        Field[] fields = data.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (ignore(field)) {
                continue;
            }
            this.process(field);
        }
        this.afterIteratorDone();
    }

    public Object getData() {
        return data;
    }

    public abstract void process(Field field);

    public abstract boolean ignore(Field field);

    public abstract void afterIteratorDone();
}
