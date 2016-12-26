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

package cn.wantedonline.puppy.jdbc.util;

/**
 * 根据key获取对应的基本型的value，支持默认值和异常处理
 *
 * @since V0.5.0
 * @author thunder
 */
public interface KeyValueGetter {

    /**
     * 获取key对应的value字符串
     */
    public String getString(String key);

    /**
     * 获取key对应的value字符串，当value不存在时返回defaultValue
     */
    public String getString(String key, String defaultValue);

    /**
     * 获取key对应的value并转换为Boolean，当value不存在或转换失败时返回defaultValue
     * 
     * @return 字符串为true/y/1时都返回true
     */
    public Boolean getBool(String key, Boolean defaultValue);

    /**
     * 获取key对应的value并转换为Integer，当value不存在或转换失败时返回defaultValue
     */
    public Integer getInt(String key, Integer defaultValue);

    /**
     * 获取key对应的value并转换为Long，当value不存在或转换失败时返回defaultValue
     */
    public Long getLong(String key, Long defaultValue);

    /**
     * 获取key对应的value并转换为Float，当value不存在或转换失败时返回defaultValue
     */
    public Float getFloat(String key, Float defaultValue);

    /**
     * 获取key对应的value并转换为Double，当value不存在或转换失败时返回defaultValue
     */
    public Double getDouble(String key, Double defaultValue);
}
