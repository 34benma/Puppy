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

package cn.wantedonline.puppy.httpserver.util.command;

/**
 * 命令工具，主要为使用SSH登录远程机器
 *
 */
public class CommandUtil {

    /**
     * 使用ssh命令远程调用remote机器命令
     * 
     * @param remoteHost 远程计算机名或者IP
     * @param ori_cmd 原始命令
     * @return
     */
    public static CommandService ssh(String remoteHost, String ori_cmd) {
        String cmd = getSshCmd(remoteHost, ori_cmd);
        return new CommandSsh(cmd);
    }

    /**
     * 根据远程计算机和原始命令来获得ssh命令
     * 
     * @param remoteHost 远程计算机
     * @param ori_cmd 原始命令
     * @return ssh命令
     */
    public static String getSshCmd(String remoteHost, String ori_cmd) {
        return new StringBuilder(5 + ori_cmd.length()).append("ssh ").append(remoteHost).append(" '").append(ori_cmd).append("'").toString();
    }
}
