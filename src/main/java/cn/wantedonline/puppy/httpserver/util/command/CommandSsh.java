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
 * SSH命令类
 *
 */
public class CommandSsh extends CommandService {

    private boolean first = true;

    /**
     * 构造方法
     * 
     * @param commandString 命令字符串
     */
    public CommandSsh(String commandString) {
        super(commandString);
    }

    /**
     * 构造方法
     * 
     * @param commandString 命令字符串
     * @param useShell 是否使用Shell
     */
    public CommandSsh(String commandString, boolean useShell) {
        super(commandString, useShell);
    }

    /**
     * 构造方法
     * 
     * @param command 命令数组
     */
    public CommandSsh(String[] command) {
        super(command);
    }

    /**
     * 检查是否存在信任关系
     */
    protected void checkIsSshTrust() {
        if (first && processingLine.endsWith("password:")) {
            throw new RuntimeException(processingLine);
        }
        first = false;
    }

    /**
     * 处理一行
     */
    @Override
    protected void processLine() {
        checkIsSshTrust();
    }
}
