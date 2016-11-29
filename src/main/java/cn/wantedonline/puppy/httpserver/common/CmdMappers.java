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

import cn.wantedonline.puppy.httpserver.annotation.CmdDescr;
import cn.wantedonline.puppy.httpserver.annotation.CmdMapper;
import cn.wantedonline.puppy.httpserver.annotation.CmdOverride;
import cn.wantedonline.puppy.httpserver.annotation.CmdPath;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.spring.SpringBootstrap;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.AssertUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.ResourceBundleUtil;
import cn.wantedonline.puppy.util.StringTools;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

/**
 * <pre>
 *     Cmd映射容器
 * </pre>
 *
 * @author wangcheng
 * @since V0.1.0 on 16/11/17.
 */
@Component
public class CmdMappers {
    private Logger log = Log.getLogger(CmdMappers.class);

    private Map<String, CmdMeta> annotation_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> auto_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> config_cmd_map = Collections.emptyMap();

    private Map<String, CmdMeta> cmdAllMap = new LinkedHashMap<>();
    private Map<CmdMeta, List<String>> reverseCmdAllSortedMap;

    @Autowired
    private HttpServerConfig config;

    @Config
    private String cmdmapper_config_filename = "cmdmapper";
    private ResourceBundle cmdMapperBundle = null;
    private final Map<CmdMeta, CmdMeta> cmdMetaUnite = new HashMap<>();
    private List<BaseCmd> cmds;

    private Set<Class<? extends BaseCmd>> disableCmdClass = new HashSet<>(0);
    private Set<Method> disableCmdMethod = new HashSet<>(0);
    private PathMap fuzzyMap;

    private static String sanitizePath(String path) {
        int len = path.length();
        if (len > 1 && path.lastIndexOf('/') == len - 1) {
            path = path.substring(0,len-1);
        }
        return path;
    }

    private String _lower(String name) {
        return name.substring(0,1).toLowerCase() + name.substring(1);
    }

    private BaseCmd getCmd(String name) {
        Object obj = BeanUtil.getTypedBean(name);
        if (obj instanceof BaseCmd) {
            Class<?> clazz = obj.getClass();
            if (AssertUtil.isNotNull(clazz.getAnnotation(Deprecated.class)) || disableCmdClass.contains(clazz)) {
                return null;
            }
            return (BaseCmd) obj;
        }
        return null;
    }

    private void _buildReverseCmdAllSortedMap(Map<CmdMeta, List<String>> tmp, Map<String, CmdMeta> ori) {
        for (Map.Entry<String, CmdMeta> e : ori.entrySet()) {
            CmdMeta meta = e.getValue();
            String url = e.getKey();
            List<String> list = tmp.get(meta);
            if (AssertUtil.isNull(list)) {
                list = new ArrayList<>(1);
                tmp.put(meta, list);
            }
            list.add(url);
        }
    }

    public Map<CmdMeta, List<String>> getReverseCmdAllSortedMap() {
        if (AssertUtil.isNull(reverseCmdAllSortedMap)) {
            Map<CmdMeta, List<String>> tmp = new LinkedHashMap<>();
            _buildReverseCmdAllSortedMap(tmp, auto_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, annotation_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, config_cmd_map);
            reverseCmdAllSortedMap = tmp;
        }
        return reverseCmdAllSortedMap;
    }

    public ResourceBundle getCmdMapperBundle() {
        if (AssertUtil.isNull(cmdMapperBundle)) {
            cmdMapperBundle = ResourceBundleUtil.reload(cmdmapper_config_filename);
            if (AssertUtil.isNull(cmdMapperBundle)) {
                cmdMapperBundle = new ResourceBundle() {
                    @Override
                    protected Object handleGetObject(String key) {
                        return null;
                    }

                    @Override
                    public Enumeration<String> getKeys() {
                        return new Enumeration<String>() {
                            @Override
                            public boolean hasMoreElements() {
                                return false;
                            }

                            @Override
                            public String nextElement() {
                                return null;
                            }
                        };
                    }
                };
            }
        }
        return cmdMapperBundle;
    }

    public CmdMeta getCmdMeta(String path) {
        path = sanitizePath(path);
        CmdMeta meta = cmdAllMap.get(path);
        if (AssertUtil.isNull(meta) || AssertUtil.isNotNull(fuzzyMap)) {
            return (CmdMeta) fuzzyMap.match(path);
        }
        return meta;
    }

    private String getCmdName(BaseCmd cmd) {
        String cmdSuffix = config.getCmdSuffix();
        String cmdName = _lower(cmd.getClass().getSimpleName());

        int idx = cmdName.indexOf('$');
        if (idx > 0) {
            cmdName = cmdName.substring(0, idx);
        }

        if (cmdName.endsWith(cmdSuffix)) {
            cmdName = cmdName.substring(0, cmdName.length() - cmdSuffix.length());
        }
        return cmdName;
    }

    private List<BaseCmd> getCmds() {
        if (AssertUtil.isNull(cmds)) {
            cmds = new ArrayList<>();
            for (String name : SpringBootstrap.getContext().getBeanDefinitionNames()) {
                BaseCmd cmd = getCmd(name);
                if (AssertUtil.isNotNull(cmd)) {
                    cmds.add(cmd);
                }
            }
        }
        return cmds;
    }

    private void putUrl(Map<String, CmdMeta> tmp, String url, CmdMeta meta) {
        if (StringTools.isEmpty(url)) {
            return;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return;
        }
        if (url.contains("*")) {// 模糊匹配
            if (AssertUtil.isNull(fuzzyMap)) {
                fuzzyMap = new PathMap();
            }
            fuzzyMap.put(url, meta);
            return;
        }
        url = sanitizePath(url);
        tmp.put(url, meta);
    }

    public Map<String, CmdMeta> initCmdMapperDefinedMap() {
        Map<String, CmdMeta> tmp = new LinkedHashMap<>();
        for (CmdMeta meta : cmdMetaUnite.values()) {
            CmdMapper m = meta.getMethod().getAnnotation(CmdMapper.class);
            if (AssertUtil.isNotNull(m)) {
                for (String url : m.value()) {
                    putUrl(tmp, url, meta);
                }
            }
        }
        annotation_cmd_map = tmp;
        log.error("ANNOTATION_MAP:\t\t{}", annotation_cmd_map);
        cmdAllMap.putAll(tmp);
        return tmp;
    }

    public Map<String, CmdMeta> initAutoMap() {
        Map<String, CmdMeta> tmp_auto = new LinkedHashMap<String, CmdMeta>();

        String cmdDefaultMethod = config.getCmdDefaultMethod();
        for (BaseCmd cmd : getCmds()) {
            Class<?> clazz = cmd.getClass();
            ArrayList<String> cmdNameList = new ArrayList<String>(1);
            CmdPath cmdPathForClazz = clazz.getAnnotation(CmdPath.class);
            if (clazz.getAnnotation(CmdOverride.class) == null) {
                cmdNameList.add(getCmdName(cmd));
            }
            if (AssertUtil.isNotNull(cmdPathForClazz)) {
                for (String alias : cmdPathForClazz.value()) {
                    cmdNameList.add(alias);
                }
            }

            for (String cmdName : cmdNameList) {
                Method[] mehods = clazz.getDeclaredMethods();
                for (Method method : mehods) {
                    if (!isCmdMethod(cmd, method)) {
                        continue;
                    }
                    CmdMeta meta = null;
                    String methodName = method.getName();

                    ArrayList<String> urlList = new ArrayList<String>(1);
                    if (AssertUtil.isNull(method.getAnnotation(CmdOverride.class))) {
                        if (methodName.equals(cmdDefaultMethod)) {
                            urlList.add(MessageFormat.format("/{0}", cmdName));
                        } else {
                            urlList.add(MessageFormat.format("/{0}/{1}", cmdName, methodName));
                        }
                    }
                    CmdPath cmdPathForMethod = method.getAnnotation(CmdPath.class);
                    if (cmdPathForMethod != null) {
                        for (String alias : cmdPathForMethod.value()) {
                            urlList.add(MessageFormat.format("/{0}/{1}", cmdName, alias));
                        }
                    }

                    meta = newCmdMeta(cmd, method);
                    for (String url : urlList) {
                        tmp_auto.put(url, meta);
                    }
                }
            }
        }
        auto_cmd_map = tmp_auto;
        log.error("AUTO_MAP:\t\t{}", auto_cmd_map);
        cmdAllMap.putAll(tmp_auto);

        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>(tmp_auto.size());
        tmp.putAll(tmp_auto);
        return tmp;
    }

    public Map<String, CmdMeta> initConfigMap() {
        ResourceBundle bundle = getCmdMapperBundle();
        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>();
        // 3.加载cmdmapper配置映射
        if (bundle != null) {
            for (String url : bundle.keySet()) {
                if (!isUrlConfig(url)) {
                    continue;
                }
                String value = bundle.getString(url).trim();
                CmdMeta cm = buildCmdMeta(value);
                if (cm != null) {
                    putUrl(tmp, url, cm);
                }
            }
        }
        config_cmd_map = tmp;
        log.error("CONFIG_MAP:\t\t{}", config_cmd_map);
        cmdAllMap.putAll(tmp);

        return tmp;
    }

    public CmdMeta buildCmdMeta(String configStr) {
        String[] arg = configStr.split("\\.");
        if (arg.length >= 2) {// 具体到方法
            String cmdName = _lower(arg[0].trim());
            String cmdMethodName = arg[1].trim();
            try {
                BaseCmd cmd = BeanUtil.getTypedBean(cmdName);
                if (cmd == null) {
                    return null;
                }
                Method method = cmd.getClass().getMethod(cmdMethodName, HttpRequest.class, HttpResponse.class);
                return newCmdMeta(cmd, method);
            } catch (Exception e) {
                log.error("cann't find cmd:{},method:{}", new Object[] {
                        cmdName,
                        cmdMethodName,
                        e
                });
            }
        } else {
            String cmdName = _lower(configStr);
            try {
                BaseCmd cmd = BeanUtil.getTypedBean(cmdName);
                if (cmd == null) {
                    return null;
                }
                Method method = cmd.getClass().getMethod(config.getCmdDefaultMethod(), HttpRequest.class, HttpResponse.class);
                return newCmdMeta(cmd, method);
            } catch (Exception e) {
                log.error("cann't find cmd:{}", new Object[] {
                        cmdName,
                        e
                });
            }
        }
        return null;
    }

    private boolean isCmdMethod(BaseCmd cmd, Method method) {
        if (method.getAnnotation(Deprecated.class) != null || disableCmdMethod.contains(method)) {
            return false;
        }
        if (!Modifier.isPublic(method.getModifiers())) {
            return false;
        }
        Class<?>[] pts = method.getParameterTypes();
        if (pts.length != 2) {
            return false;
        }
        if (!pts[0].isAssignableFrom(HttpRequest.class) || !pts[1].isAssignableFrom(HttpResponse.class)) {
            return false;
        }
        return true;
    }

    private boolean isUrlConfig(String cmdMapperBundleKey) {
        return cmdMapperBundleKey.startsWith("*") || cmdMapperBundleKey.startsWith("/");
    }

    /**
     * <pre>
     * 2011-01-25 为了解决统计cmd各自的请求量问题,内存中需要统一CmdMeta
     * 即：如果cmd,method一样,内存中只能用相同的引用
     */
    private CmdMeta newCmdMeta(BaseCmd cmd, Method method) {
        CmdMeta tmp = new CmdMeta(cmd, method);
        CmdMeta ori = cmdMetaUnite.get(tmp);
        if (ori == null) {
            cmdMetaUnite.put(tmp, tmp);
            return tmp;
        }
        return ori;
    }

    public void printFuzzyMap() {
        if (fuzzyMap != null) {
            log.error("FUZZY_MAP:\t\t{}", fuzzyMap);
        }
    }

    public static class CmdMeta {
        private BaseCmd cmd;
        private Method method;
        private String baseName;
        private String name;

        public CmdMeta(BaseCmd cmd, Method method) {
            this.cmd = cmd;
            this.method = method;
            this.baseName = cmd.getClass().getSimpleName() + ".*";
            this.name = cmd.getClass().getSimpleName() + "." + method.getName();
        }

        public BaseCmd getCmd() {
            return cmd;
        }

        public Method getMethod() {
            return method;
        }

        public String getBaseName() {
            return baseName;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CmdMeta cmdMeta = (CmdMeta) o;

            if (!cmd.equals(cmdMeta.cmd)) return false;
            if (!method.equals(cmdMeta.method)) return false;
            if (!baseName.equals(cmdMeta.baseName)) return false;
            return name.equals(cmdMeta.name);

        }

        @Override
        public int hashCode() {
            int result = cmd.hashCode();
            result = 31 * result + method.hashCode();
            result = 31 * result + baseName.hashCode();
            result = 31 * result + name.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return name;
        }

        public String getCmdDescription() {
            CmdDescr c = cmd.getClass().getAnnotation(CmdDescr.class);
            if (AssertUtil.isNotNull(c)) {
                return c.value();
            }
            return "";
        }
    }
}
