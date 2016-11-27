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

package cn.wantedonline.puppy.httpserver.cmd;

import cn.wantedonline.puppy.Bootstrap;
import cn.wantedonline.puppy.httpserver.annotation.CmdDescr;
import cn.wantedonline.puppy.httpserver.common.BaseCmd;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.util.Log;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 *     远程关闭服务器
 *     TODO:内部IP校验
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 2016/11/27.
 */
@Service
@CmdDescr("非对外接口:关闭服务器")
public class ShutDownCmd implements BaseCmd {
    private static Logger log = Log.getLogger(ShutDownCmd.class);

    @Autowired
    private Bootstrap bootstrap;
    public static final int shutdownWaitTime = 500;

    public Object process(HttpRequest request, HttpResponse response) throws Exception {
        final String ip = request.getPrimitiveRemoteIp();
        log.error("{} try to shutdown server", ip);
        Runnable run = new Runnable() {

            @Override
            public void run() {
                log.error("{} SHUTDOWN HTTP SERVER....", ip);
                try {
                    Thread.sleep(shutdownWaitTime);
                } catch (InterruptedException e) {
                }
                long before = System.currentTimeMillis();
                try {
                    bootstrap.stopServer();
                } finally {
                    long span = System.currentTimeMillis() - before;
                    String msg = "SHUTDOWN HTTP SERVER DONE...USING " + span + "MS";
                    System.err.println(msg);
                    log.error("{}->{}", ip, msg);
                    System.exit(347);// 这里定义一个特殊的退出状态码,表示正常退出
                }
            }
        };
        Thread t = new Thread(run);
        t.start();
        return System.currentTimeMillis() + shutdownWaitTime + "";
    }

}
