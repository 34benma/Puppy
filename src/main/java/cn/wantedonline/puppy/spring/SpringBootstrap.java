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

package cn.wantedonline.puppy.spring;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * <pre>
 *     Spring启动类
 *     如果需要获取系统ApplicationContext则调用{@code SpringBootstrap.getContext()}
 *     如果Spring还没有完成初始化，则ApplicationContext为null
 *
 * <strong>
 *     注意：业务层不要调用load方法来加载Spring容器，Puppy启动的时候会调用load方法加载好
 *     Spring容器，完成Spring容器的初始化工作
 * </strong>
 * </pre>
 *
 * @author louiswang
 * @since V0.1.0 on 2016/10/28
 */
public class SpringBootstrap {
    private static ApplicationContext CONTEXT;

    private SpringBootstrap() {};

    public static ApplicationContext getContext() {
        if (null == CONTEXT) {
            throw new NullPointerException("SpringBootstrap.CONTEXT is null, you should call SpringBootstrap.load() method first");
        }
        return CONTEXT;
    }

    public static ApplicationContext load(String... springConfigLocations) {
        if (CONTEXT != null) {
            throw new IllegalAccessError("ApplicationContext has instanced");
        }
        CONTEXT = new ClassPathXmlApplicationContext(springConfigLocations);
        ConfigAnnotationBeanPostProcessor pp = BeanUtil.getTypedBean(CONTEXT, "configAnnotationBeanPostProcessor");
        pp.postProcessAfterBootstrap(CONTEXT);
        return CONTEXT;
    }

}
