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
 *
 * </pre>
 *
 * @author 迅雷 ZengDong
 * @author wangcheng
 * @since V0.1.0 on 2016/11/17
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CmdMonitor {

    public static int DEFAULT_TIMEOUT = -88888888;

    /**
     * <pre>
     * 一个监控url对应一个param,如
     * nodeType=1&test=1
     */
    String param();

    /**
     * 响应码 默认为200
     */
    int status() default 200; // -1 表示不判断

    /**
     * 响应大小必须大于多少字节
     */
    int lengthMin() default 0;

    /**
     * <pre>
     * 响应内容中必须包括,多个字符串 是"且"关系
     * 
     * 如果是重定向的响应码，这里表示的是 locations的内容
     */
    String[] contains() default "";

    /**
     * 仅对json回包有效
     */
    int rtn() default -1;// -1 表示不判断

    /**
     * 连接超时,默认用armero默认配置(一般是5000ms)
     */
    int connTimeout() default DEFAULT_TIMEOUT;

    /**
     * socketTimeout超时,一般是5000ms)
     */
    int soTimeout() default DEFAULT_TIMEOUT;

    /**
     * <pre>
     * 用于隔离开测试环境跟正式环境的监控用例,这里用 ip或host 包含方法来判断
     *
     * 因此如果是想分隔开两个用例，可以写两个CmdMonitor
     * 正式环境用enableOn="twin"
     * 测评环境用enableOn="test"
     * 
     * host不好区分时，其次方法是用ip(内部用 数字跟.号 来判断是否为ip格式片断)，如enableOn="192.168"
     * 用ip没有host那么直观
     * 
     * 当然没分别的就不用这么麻烦了,不设置就行
     */
    String enableOn() default "";
}
