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
import cn.wantedonline.puppy.httpserver.annotation.CmdAuthor;
import cn.wantedonline.puppy.httpserver.annotation.CmdReturn;
import cn.wantedonline.puppy.httpserver.common.BaseCmd;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.httptools.JsonUtil;
import cn.wantedonline.puppy.httpserver.httptools.RtnConstants;
import cn.wantedonline.puppy.jdbc.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * Created by louiswang on 16/12/26.
 */
@Service
public class TestCmd implements BaseCmd {

    @Resource(name="crawlerDataSource")
    private JdbcTemplate myJdbcTemplate;

    @Cmd("测试Cmd功能")
    @CmdReturn({
            ""
    })
    @CmdAuthor("wangcheng")
    public Object testMyCmd(HttpRequest request, HttpResponse response) throws Exception {
        String sql = "SELECT count(1) FROM shequ_crawler.article where status = 6";
        int count = myJdbcTemplate.queryForInt(sql);
        return JsonUtil.getRtnAndDataJsonObject(RtnConstants.OK, count);
    }
}
