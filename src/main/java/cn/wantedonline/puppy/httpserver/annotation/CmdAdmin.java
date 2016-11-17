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

package cn.wantedonline.puppy.httpserver.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *     修饰管理接口，用于说明接口管理者
 *     和{@code @CmdAuthor}的区别是本注解一般修饰管理或统计接口，而{@code @CmdAuthor}修饰普通接口
 * </pre>
 *
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/11/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdAdmin {

    public enum CmdAdminType {
        /**
         * 后台统计接口
         */
        STAT,
        /**
         * 后台操作接口
         */
        OPER,
    }

    CmdAdminType type() default CmdAdminType.STAT;
}
