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

import java.lang.annotation.Annotation;
import java.util.*;

import cn.wantedonline.puppy.httpserver.annotation.*;
import cn.wantedonline.puppy.httpserver.common.BaseCmd;
import cn.wantedonline.puppy.httpserver.common.CmdMappers;
import cn.wantedonline.puppy.httpserver.common.CmdSessionType;
import cn.wantedonline.puppy.httpserver.common.ContentType;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.StringTools;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 在线接口文档
 * 
 * @author ZengDong
 * @since 2010-5-23 上午12:15:48
 */
@Service
public class DocCmd implements BaseCmd {

    // private static final Logger log = Log.getLogger();
    @Autowired
    private CmdMappers cmdMappers;

    private final String head = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\"><html xmlns=\"http://www.w3.org/1999/xhtml\"><head>";
    private final String js = "<script type=\"text/javascript\">function showul(element){var ul = get_nextsibling(element);if(ul.style.display==\"block\")ul.style.display=\"none\";else{ul.style.display=\"block\";}}\nfunction get_nextsibling(n){var x=n.nextSibling;while (x.nodeType!=1){x=x.nextSibling;}return x;}\nfunction showdesc(elementId){var desc = document.getElementById(elementId);var descs = getElementsByClass(\"desc\",\"div\");for(var i=0;i<descs.length;i++){if(descs[i]!=desc && descs[i].style.display==\"block\")descs[i].style.display = \"none\";}if(desc.style.display==\"none\"){desc.style.display=\"block\";desc.scrollIntoView();}}" + "\nfunction getElementsByClass(searchClass,tag) {var classElements = new Array();if ( tag == null )tag = '*';var els = document.getElementsByTagName(tag);var elsLen = els.length;var pattern = new RegExp(\"(^|\\s)\"+searchClass+\"(\\s|$)\");for (i = 0, j = 0; i < elsLen; i++) {if ( pattern.test(els[i].className) ) {classElements[j] = els[i];j++;}}return classElements;}</script>";
    private final String css = "<style type=\"text/css\">\n" + "body,dl,dt,dd,ul,ol,li,h1,h2,h3,h4,h5,h6,pre,form,fieldset,input,textarea,p,th,td{padding:0;margin:0;} "
            + "table{border-collapse:collapse;border-spacing:0;} "
            + "html,body{width:100%;overflow:auto;}"
            + ""
            + "body{\n"
            + "    margin:0px;\n"
            + "    font: normal 12px/1.6em simsun;"
            + "    }\n"
            + "#header{\n"
            + "    border-bottom:1px #CCCCCC dashed;\n"
            + "    color:#999;\n"
            + "    text-align:center;\n"
            + "    margin-bottom:10px;\n"
            + "    }\n"
            + "#wrapper{\n"
            + "    width:100%;\n"
            + "    }\n"
            + "#sidebar{\n"
            + "    float: left;\n"
            + "    width: 100%;\n"
            + "    overflow-x:hidden;"
            + "    overflow-y:auto;"
            + "    }\n"
            + "h2{\n"
            + "    cursor: pointer;\n"
            + "    font-size: 12px;\n"
            + "    font-weight:400;\n"
            + "    padding: 4px;\n"
            + "    text-align: center;\n"
            + "    margin:0px;\n"
            + "}\n"
            + "h2:hover {\n"
            + "    background: none repeat scroll 0 0 #4F81BD;\n"
            + "    color: white;\n"
            + "}\n"
            + "#sidebar ul{\n"
            + "    margin: 0;\n"
            + "    background:none repeat scroll 0 0 white;\n"
            + "    display:none;\n"
            + "    }\n"
            + "li{\n"
            + "    list-style:none;\n"
            + "    }\n"
            + "li h2{\n"
            + "    padding-left:20px;\n"
            + "    text-align:left;\n"
            + "    background-color:white;\n"
            + "    }\n"
            + ".desc{\n"
            + "    float:right;\n"
            + "    width:100%;\n"
            + "    margin-bottom: 50px;\n"
            + "    word-wrap:break-word;"
            + "    word-break:normal;"
            + "    }\n"
            + "#content h3 {\n"
            + "    background: none repeat scroll 0 0 #95B3D7;\n"
            + "    color: #FFFFFF;\n"
            + "    margin: 5px auto;\n"
            + "    padding: 5px;\n"
            + "    font-size:12px;\n"
            + "    font-weight:400;\n"
            + "    overflow:hidden;\n"
            + "}\n"
            + "#content h2 {\n"
            + "    background: none repeat scroll 0 0 #4F81BD;\n"
            + "    color: #FFFFFF;\n"
            + "    font-size: 14px;\n"
            + "    font-weight: bold;\n"
            + "    margin: 0px auto;\n"
            + "    padding: 5px;\n"
            + "    text-align:left;\n"
            + "}\n"
            + "#content h4 {\n"
            + "    background: none repeat scroll 0 0 #DBE5F1;\n"
            + "    margin: 5px auto 5px 5px;\n"
            + "    padding: 5px;\n"
            + "    font-weight:400;\n"
            + "}\n"
            + "table{\n"
            + "    width:100%;\n"
            + "    margin:0px;\n"
            + "    padding:0px;\n"
            + "    }\n"
            + "td{\n"
            + "    width:20%;\n"
            + "    margin-left:5px;\n"
            + "    }\n"
            + "#content span{\n"
            + "    display:block;\n"
            + "    margin-left:30px;\n"
            + "    }\n"
            + "#content h3 table td{font-size:12px;color: #FFFFFF;}"
            + "#content h4 table td{font-size:12px;}"
            + "a{text-decoration:none;color:#000;}"
            + "#content .desc a h2:hover{}"
            + "#content .desc a:link{color:#000}"
            + "</style>\n";

    private String all_doc;

    public String getAll_doc() {
        if (all_doc == null) {
            all_doc = getDoc();
        }
        return all_doc;
    }

    public String getDoc() {
        // 输出时，按CMD字母顺序排序
        List<Map.Entry<CmdMappers.CmdMeta, List<String>>> list = new ArrayList<Map.Entry<CmdMappers.CmdMeta, List<String>>>(cmdMappers.getReverseCmdAllSortedMap().entrySet());
        Collections.sort(list, new Comparator<Map.Entry<CmdMappers.CmdMeta, List<String>>>() {

            @Override
            public int compare(Map.Entry<CmdMappers.CmdMeta, List<String>> o1, Map.Entry<CmdMappers.CmdMeta, List<String>> o2) {
                return o1.getKey().getCmd().getClass().getSimpleName().compareTo(o2.getKey().getCmd().getClass().getSimpleName());
            }
        });
        Map<String, List<Map.Entry<CmdMappers.CmdMeta, List<String>>>> category_cmds_map = new LinkedHashMap<String, List<Map.Entry<CmdMappers.CmdMeta, List<String>>>>();
        for (Map.Entry<CmdMappers.CmdMeta, List<String>> e : list) {
            CmdMappers.CmdMeta meta = e.getKey();
            boolean hasDoc = false;
            for (Annotation a : meta.getMethod().getAnnotations()) {
                // 如果是CmdAdmin为系统统计的，就不显示DOC
                if (a instanceof CmdAdmin) {
                    hasDoc = false;
                    break;
                } else if (a.toString().startsWith("@" + CmdAdmin.class.getPackage().getName())) { // 说明此cmd有文档注释
                    hasDoc = true;
                }
            }
            // 如果有标记
            if (hasDoc) {
                String key = meta.getCmd().getClass().getSimpleName().replace("Cmd", "") + " " + meta.getCmdDescription();
                List<Map.Entry<CmdMappers.CmdMeta, List<String>>> cmds = category_cmds_map.get(key);
                if (cmds == null) {
                    cmds = new ArrayList<Map.Entry<CmdMappers.CmdMeta, List<String>>>(1);
                    category_cmds_map.put(key, cmds);
                }
                cmds.add(e);
            }
        }
        StringBuilder tmp = new StringBuilder(head);
        tmp.append(css);
        tmp.append(js);
        tmp.append("</head><body>");
        tmp.append("<div id=\"wrapper\"><div id=\"roller\" style=\"height:100%;width:350px;position:fixed;overflow:auto;left:0;background-color:#DBE5F1;\"><div id=\"sidebar\">");
        for (Map.Entry<String, List<Map.Entry<CmdMappers.CmdMeta, List<String>>>> e : category_cmds_map.entrySet()) {
            String category = e.getKey();
            tmp.append("<h2 onclick=\"showul(this)\">").append(category).append("</h2>");
            tmp.append("<ul>");
            List<Map.Entry<CmdMappers.CmdMeta, List<String>>> cmdProperty = e.getValue();
            for (Map.Entry<CmdMappers.CmdMeta, List<String>> cmd : cmdProperty) {
                // CmdMeta meta = cmd.getKey();
                List<String> urls = cmd.getValue();
                for (String url : urls) {
                    tmp.append("<a href=\"#").append(url).append("\">").append("<li><h2>").append(url).append("</h2></a></li>");
                }
            }
            tmp.append("</ul>");
        }
        tmp.append("</div></div>");
        tmp.append("<div id=\"content\" style=\"margin:0 10px 0 360px;\">");
        for (Map.Entry<String, List<Map.Entry<CmdMappers.CmdMeta, List<String>>>> e : category_cmds_map.entrySet()) {
            List<Map.Entry<CmdMappers.CmdMeta, List<String>>> cmdProperty = e.getValue();
            for (Map.Entry<CmdMappers.CmdMeta, List<String>> cmd : cmdProperty) {
                List<String> urls = cmd.getValue();
                for (String url : urls) {
                    CmdMappers.CmdMeta meta = cmd.getKey();
                    Cmd c = meta.getMethod().getAnnotation(Cmd.class);
                    CmdAuthor author = meta.getMethod().getAnnotation(CmdAuthor.class);
                    CmdContentType contentType = meta.getMethod().getAnnotation(CmdContentType.class);
                    CmdParams params = meta.getMethod().getAnnotation(CmdParams.class);
                    CmdReturn ret = meta.getMethod().getAnnotation(CmdReturn.class);
                    CmdSession session = meta.getMethod().getAnnotation(CmdSession.class);
                    tmp.append("<div class=\"desc\" id=\"").append(url).append("\">");
                    if (c != null) {
                        tmp.append("<a href=\"").append(url).append("\" target=\"_blank\"><h2>").append(c.value()).append("\t").append(url).append("</h2>").append("</a>");
                    }
                    tmp.append("<h3><table><tr><td>登录态要求</td><td nowrap>");
                    if (null == session) {
                        tmp.append("未知");
                    } else {
                        if (session.type() == CmdSessionType.COMPELLED) {
                            tmp.append("必须要求有登录态，否则报rtn:11");
                        } else if (session.type() == CmdSessionType.NOT_COMPELLED) {
                            tmp.append("不需要登录态");
                        } else if (session.type() == CmdSessionType.DISPENSABLE) {
                            tmp.append("要求有登录态，没有也不会报错，将被当作游客处理");
                        } else if (session.type() == CmdSessionType.INTERNAL_WITH_IP_AUTH) {
                            tmp.append("内部接口，需要IP认证");
                        } else if (session.type() == CmdSessionType.INTERNAL_WITH_SIGN_AUTH) {
                            tmp.append("内部接口，需要签名认证");
                        }
                    }
                    tmp.append("</td><td></td><td></td><td nowarp>");
                    if (author != null) {
                        printArray(tmp, author.value());
                    }
                    tmp.append("</td></tr></table></h3>");
                    if (c != null) {
                        String[] descs = c.desc();
                        if (AssertUtil.isNotEmptyArray(descs) && (descs.length > 1 || StringTools.isNotEmpty(descs[0]))) {
                            tmp.append("<span>");
                            for (String d : descs) {
                                tmp.append("\n").append(d);
                            }
                            tmp.append("</span>");
                        }
                    }
                    tmp.append("<h3>参数</h3>");

                    if (params != null) {
                        for (CmdParam p : params.value()) {
                            tmp.append("<h4><table><tr><td>").append(p.name()).append("</td><td>").append(p.type().getSimpleName()).append("</td><td>").append(p.compelled() ? "必需" : "")
                                    .append("</td><td>").append(StringTools.isEmpty(p.defaultValue()) ? "" : "默认值:" + p.defaultValue()).append("</td><td>");
                            printArray(tmp, p.scope());
                            tmp.append("</td></tr></table></h4>").append("<span>");
                            String[] descs = p.desc();
                            for (int i = 0; i < descs.length; i++) {
                                tmp.append(StringTools.escapeHtml(descs[i])).append("\n<br/>");
                                // tmp.append(descs[i]).append("\n");
                            }
                            tmp.append("</span>");
                        }
                    }
                    tmp.append("<h3><table><tr><td>回包</td><td>");
                    if (contentType != null) {
                        printArray(tmp, contentType.value());
                    }
                    tmp.append("</td><td></td><td></td><td></td></tr></table></h3><span>");
                    if (ret != null) {
                        String[] descs = ret.value();
                        if (AssertUtil.isNotEmptyArray(descs) && (descs.length > 1 || StringTools.isNotEmpty(descs[0]))) {
                            // tmp.append("<pre>");
                            for (String d : descs) {
                                tmp.append(StringTools.escapeHtml(d)).append("\n<br/>");
                            }
                            // tmp.append("</pre>");
                        }
                    }
                    tmp.append("</span></div>");
                }
            }
        }
        tmp.append("</div>");
        tmp.append("</div>");
        tmp.append("</body></html>");
        return tmp.toString();
    }

    /**
     * 显示当前已经映射的命令
     */
    @CmdAdmin()
    public Object process(HttpRequest request, HttpResponse response) throws Exception {
//        init(request, response);
        response.setInnerContentType(ContentType.html);
        return getAll_doc();
    }

    private void printArray(StringBuilder tmp, Object[] arr) {
        boolean first = true;
        if (AssertUtil.isNotEmptyArray(arr)) {
            for (Object au : arr) {
                String a = StringTools.escapeHtml(au.toString());
                if (!first) {
                    tmp.append(",").append(a);
                } else {
                    tmp.append(a);
                    first = false;
                }

            }
        }
    }

}
