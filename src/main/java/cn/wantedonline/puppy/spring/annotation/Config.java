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
 * <pre>
 *     通过配置文件在系统启动时初始化字段值
 *     使用场景：某个Spring容器中的Bean的某个字段A在被@Config注解之后，可以通过从配置文件给该字段设置值
 *     Puppy启动的时候，在Spring初始化完成之后会自动将配置文件中设置的属性值给赋值到该字段
 *     支持的数据类型：
 *                  8种基本数据类型和String类型
 *                  集合类型，支持Map,Set,List和Array数据结构
 *                  如果是集合类型，集合中存储的元素也必须是8种基本类型或String类型
 *                  总之，不支持自定义的POJO对象
 *     如果是集合类型，默认元素之间使用英文状态下的逗号(,)分隔，如果是Key-Value的字典类型，Key-Value之间使用英文状态下的冒号分隔(:)
 *
 *     使用示例：
 *     {@code
 *     @Service
 *     public class A {
 *        @code @Config(resetable=true)
 *         private int B = 2;
 *     }
 *     }
 *     属性配置文件中有这样一项配置：B=10
 *     当Puppy启动之后自动会将B字段赋值为10
 *     {@code
 *     @Service
 *     public class A {
 *        @code @Config(resetable=true)
 *         private List<Integer> list;
 *     }
 *      }
 *     属性配置文件中有这样一项配置: list=1,2,3,4; 如果需要自定义分隔符，需要设置注解属性split,默认是英文状态下的逗号
 *     当puppy启动完成之后list中的元素为1，2，3，4
 *     {@code
 *     @Service
 *     public class A {
 *        @code @Config(resetable=true)
 *         private Map<Integer,String> map;
 *     }
 *     }
 *     属性配置文件中有这样一项配置：map=1:a,2:b,3:c; 默认情况下key:value之间使用英文状态下的冒号分隔，键值对之间使用英文状态下的逗号分隔
 *     当puppy启动完成之后map中的键值对为1:a,2:b,3:c
 *
 *     <strong>
 *         注意：只支持8种基本数据类型和String类型，如果是集合，Map或数组，存储的元素也必须是8种基本数据类型或String
 *         如果字段被{@code static}修饰，则该类必须为{@code final} 因为如果不是{@code final}类，则该类的子类的该属性自然也会加上该注解
 *         系统初始化的时候回初始化两次
 *     </strong>
 *
 * </pre>
 * Created By 迅雷 ZengDong
 * 引用并doc by louiswang
 *
 * @author ZengDong
 * @author louiswang
 * @since V0.1.0 on 2016/10/28
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
