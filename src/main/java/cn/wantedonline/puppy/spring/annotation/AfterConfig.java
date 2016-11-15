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
 *  AfterBoostrap 表示 在程序启动时，在Spring整体BeanContext完全实例化完后，再来遍历调用一次
 * 而AfterConfig   表示 在程序启动时，以及在运行时动态改变配置时，调用
 * 也就是@AfterBoostrap  不会在动态改变配置时调用，而@AfterConfig调用情况更多点
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterConfig {
}
