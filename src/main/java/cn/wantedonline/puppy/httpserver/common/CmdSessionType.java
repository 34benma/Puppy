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

package cn.wantedonline.puppy.httpserver.common;

/**
 *  <pre>
 *      登录态状态
 *  </pre>
 * @author 迅雷 hujiachao
 * @author wangcheng
 * @since V0.1.0 2016/11/17
 */
public enum CmdSessionType {
    /** 必须要有登录态 */
    COMPELLED,
    /** 不需要登录态 */
    NOT_COMPELLED,
    /** 有登录态和没有登录态时都能正常使用，如果没有登录态会被当作游客处理 */
    DISPENSABLE,
    /** 内部接口,需要IP认证 */
    INTERNAL_WITH_IP_AUTH,
    /** 内部接口,需要签名认证 */
    INTERNAL_WITH_SIGN_AUTH

}
