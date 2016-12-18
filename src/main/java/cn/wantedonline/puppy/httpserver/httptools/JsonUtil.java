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

package cn.wantedonline.puppy.httpserver.httptools;

import cn.wantedonline.puppy.util.MapUtil;
import com.alibaba.fastjson.JSON;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * <pre>
 *     JSON工具类，基于fastJSON
 * </pre>
 *
 * @author wangcheng
 * @since V0.5.0 on 2016/12/18.
 */
public class JsonUtil {

    /**
     * 返回对象的JSON表示
     * @param data
     * @return
     */
    public static String getDataJSONObect(Object data) {
        return JSON.toJSONString(data);
    }

    /**
     * 按正确次序传入 key,value,生成map
     *
     * @param keyvalue
     * @return Map对象
     */
    public static Map<String, Object> buildMap(Object... keyvalue) {
        return MapUtil.buildMap(new LinkedHashMap<String, Object>(keyvalue.length / 2), keyvalue);
    }

    public static String getOnlyOKJSON() {
        return "{\"" + RtnConstants.rtn + "\":" + RtnConstants.OK + "}";
    }

    /**
     * 获得指定的rtn的JSON
     */
    public static String getOnlyRtnJson(int rtn) {
        return "{\"" + RtnConstants.rtn + "\":" + rtn + "}";
    }

    /**
     * 获得指定的rtn和指定的data的JSON
     */
    public static String getRtnAndDataJsonObject(int rtn, Object data) {
        Map<String, Object> object = new LinkedHashMap<String, Object>(2);
        object.put(RtnConstants.rtn, rtn);
        object.put(RtnConstants.data, data);
        return JSON.toJSONString(object);
    }

    /**
     * 获得指定rtn、rtnMsg和data的JSON
     *
     * @param rtn 返回码
     * @param rtnMsg 给前端展示提示框的文本信息
     * @param data 数据
     * @return JSON表示
     */
    public static String getRtnAndDataJsonObject(int rtn, String rtnMsg, Object data) {
        Map<String, Object> object = new LinkedHashMap<String, Object>(3);
        object.put(RtnConstants.rtn, rtn);
        object.put(RtnConstants.rtnMsg, rtnMsg);
        object.put(RtnConstants.data, data);
        return JSON.toJSONString(object);
    }

    private JsonUtil() {}
}
