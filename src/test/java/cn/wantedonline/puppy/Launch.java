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
package cn.wantedonline.puppy;

import cn.wantedonline.puppy.Bootstrap;
import cn.wantedonline.puppy.spring.BeanUtil;
import org.springframework.context.ApplicationContext;

/**
 * 测试puppy启动
 * @author wangcheng
 * @since V0.1.0 on 2016/11/16
 */
public class Launch {
    public static void main(String[] args) throws InterruptedException {
        ApplicationContext context = Bootstrap.main(null, null, null,"classpath:applicationContext.xml");
        Bootstrap bootstrap = BeanUtil.getTypedBean("bootstrap");
        System.out.println(bootstrap.getServerStartTime());
    }
}
