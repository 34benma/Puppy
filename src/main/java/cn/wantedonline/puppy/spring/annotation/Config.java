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

package cn.wantedonline.puppy.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by louiswang on 16/10/28.
 *
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Config {

    String value() default "";

    /**
     * 是否可重设
     */
    boolean resetable() default false;

    /**
     * elements之间分隔符
     */
    String split() default ",";

    /**
     * key-value 分隔符
     */
    String splitKeyValue() default ":";
}
