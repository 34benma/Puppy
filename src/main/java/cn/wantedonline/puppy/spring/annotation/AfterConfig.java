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

package cn.wantedonline.puppy.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * <pre>
 *      在某个配置生效后调用使用了{@code @AfterConfig}修饰的方法
 *      使用场景：某个Spring容器中的Bean的某个字段使用了{@code @Config}修饰
 *      如果需要在系统启动后自动调用某个方法来感知
 * </pre>
 *
 * Created By 迅雷 ZengDong
 * 引用并doc By louiswang
 *
 * @author ZengDong
 * @author louiswang
 * @since V0.1.0 on 2016/10/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterConfig {
}
