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

package cn.wantedonline.puppy.httpserver.cmd;


import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.util.concurrent.ConcurrentUtil;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.AntiDos;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.StringTools;

public abstract class BaseStatCmd extends BaseCmd {

    private AntiDos baseStatCmdGlobalAntiDos = null;
    @Config(resetable = true)
    private String antiDos4BaseStatCmdGlobal;

    /**
     * 初始化：设置响应类型是 plain,校验ip,校验是否频繁访问
     */
    protected void init(HttpRequest request, HttpResponse response) {
        response.setInnerContentType(HttpResponse.ContentType.plain);
        IPAuthenticator.auth(this, request);
        if (baseStatCmdGlobalAntiDos != null) {
            baseStatCmdGlobalAntiDos.visitAndCheck(request.getRemoteIP());
        }
    }

    /**
     * 初始化,并返回带有当前时间戳的 StringBuilder
     */
    protected StringBuilder initWithTime(HttpRequest request, HttpResponse response) {
        init(request, response);
        return new StringBuilder().append(DateStringUtil.DEFAULT.now()).append('\n');
    }

    public AntiDos getBaseStatCmdGlobalAntiDos() {
        return baseStatCmdGlobalAntiDos;
    }

    public void setAntiDos4BaseStatCmdGlobal(String antiDos4BaseStatCmdGlobal) {
        this.antiDos4BaseStatCmdGlobal = antiDos4BaseStatCmdGlobal;
        if (StringTools.isEmpty(antiDos4BaseStatCmdGlobal)) {
            baseStatCmdGlobalAntiDos = null;
        } else {
            baseStatCmdGlobalAntiDos = new AntiDos(this.antiDos4BaseStatCmdGlobal).initSweeper(0, ConcurrentUtil.getDaemonExecutor());
        }
    }
}
