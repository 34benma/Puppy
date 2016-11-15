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
 *     在系统启动后自动调用的方法
 *     使用场景：在Puppy启动后，需要自动调用Spring容器中某个类的某个方法，就可以使用该注解注解方法
 *
 *     使用示例：
 *     {@code
 *      @Service
 *      public class A {
 *          @code @AfterBootstrap
 *          public void MethodA() {
 *              //do methodA
 *          }
 *      }
 *     }
 *
 *     这样，当puppy启动后，自动调用MethodA这个方法
 *
 * <strong>
 *     注意：该方法必须是无参方法，可以有返回值
 *     如果方法被{@code static}修饰，则该类必须为{@code final} 因为如果不是{@code final}类，则该类的子类的自然也继承了这个方法
 *     Puppy初始化的时候回调用这个方法两次
 * </strong>
 * </pre>
 *
 * Created By 迅雷 ZengDong
 * 引用并doc by louiswang
 *
 * @author ZengDong
 * @author louiswang
 * @since V0.1.0 on 2016/10/28
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface AfterBootstrap {
}
