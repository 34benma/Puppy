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

import cn.wantedonline.puppy.httpserver.annotation.Cmd;
import cn.wantedonline.puppy.httpserver.annotation.CmdDescr;
import cn.wantedonline.puppy.httpserver.common.BaseCmd;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.annotation.Config;
import org.springframework.stereotype.Service;

/**
 * Created by louiswang on 17/1/23.
 */
@Service
@CmdDescr("下载文件")
public class DownloadCmd implements BaseCmd {
    @Config(resetable = true)
    private String downloadPath = "/download";

    @Cmd("下载文件")
    public Object download(HttpRequest request, HttpResponse response) throws Exception {
        return "OK";
    }
}
