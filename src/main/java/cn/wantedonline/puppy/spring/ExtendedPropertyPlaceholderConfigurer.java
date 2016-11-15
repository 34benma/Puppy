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

package cn.wantedonline.puppy.spring;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;

import java.io.IOException;
import java.util.Properties;

/**
 * <pre>
 *     拓展spring的属性配置文件加载器
 * </pre>
 * @author wangcheng
 * @since V0.1.0 on 2016/10/27
 */
public class ExtendedPropertyPlaceholderConfigurer extends PropertyPlaceholderConfigurer {
    private Properties props;

    @Override
    protected void processProperties(ConfigurableListableBeanFactory beanFactoryToProcess, Properties props) throws BeansException {
        super.processProperties(beanFactoryToProcess, props);
        this.props = props;
    }

    public Properties reload() throws IOException {
        props = this.mergeProperties();
        return props;
    }

    /**
     * 获取系统属性配置文件中指定key的valueßß
     * @param key
     * @return
     */
    public Object getProperty(String key) {
        return props.get(key);
    }
}
