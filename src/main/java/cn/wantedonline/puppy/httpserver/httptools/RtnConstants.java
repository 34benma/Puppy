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

/**
 * <pre>
 *     返回码定义类
 * </pre>
 *
 * @author wangcheng
 * @since V0.5.0 on 2016/12/18.
 */
public interface RtnConstants {
    String rtn = "rtnCode";
    String data = "data";
    String rtnMsg = "rtnMsg";

    /**
     * 一切正常
     */
    int OK = 0;

    /**
     * 参数获取失败，比如类型转换失败等
     */
    int PARAM_ILLEGAL = 1001;
    /**
     * 验证码校验失败
     */
    int VCODE_INVALID = 1002;

    /**
     * 登录态校验失败
     */
    int SESSIONID_INVALID = 1003;

    /**
     * 登录态被踢
     */
    int SESSIONID_KICKOUT = 1004;

    /**
     * 参数校验失败
     */
    int PARAM_INVALID = 1005;

    /**
     * 操作被禁止
     */
    int OPERATION_FORBIDDEN = 2001;

    /**
     * 查询不到指定数据
     */
    int NO_RECORDS_EXISTS = 3001;

    /**
     * 服务器内部错误
     */
    int INTERNAL_SERVER_ERROR = 5001;
}
