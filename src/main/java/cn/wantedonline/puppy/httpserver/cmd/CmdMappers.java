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

import cn.wantedonline.puppy.httpserver.annotation.CmdDescr;
import cn.wantedonline.puppy.httpserver.annotation.CmdMapper;
import cn.wantedonline.puppy.httpserver.component.ContextAttachment;
import cn.wantedonline.puppy.httpserver.component.HttpRequest;
import cn.wantedonline.puppy.httpserver.component.HttpResponse;
import cn.wantedonline.puppy.httpserver.component.TimeoutInterrupter;
import cn.wantedonline.puppy.httpserver.util.HttpServerConfig;
import cn.wantedonline.puppy.httpserver.util.TimeSpanStat;
import cn.wantedonline.puppy.spring.BeanUtil;
import cn.wantedonline.puppy.spring.SpringBootstrap;
import cn.wantedonline.puppy.spring.annotation.Config;
import cn.wantedonline.puppy.util.EmptyChecker;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.ValueUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by wangcheng on 2016/10/30.
 */
@Component
public class CmdMappers {
    @Autowired
    private TimeoutInterrupter timeoutInterrupter;

    public static class StageTimeSpanStat extends TimeSpanStat {

        public StageTimeSpanStat(String name) {
            super(name, 1000, false, null);
            this.initFormat(40, 1);
        }

        @Override
        protected void warn(long end, long begin, Object arg) {
        }
    }

    public static class CmdMeta {

        private BaseCmd cmd;
        private Method method;
        /** cmd所在的类名简称，专用于通配符配置超时，配置整个类下面的所有cmd的配置 */
        private String baseName;
        private String name;
        /** 当前方法被调用的次数 */
        protected StageTimeSpanStat stat;

        /**
         * 每次业务操作时间,单位秒, <0 表示此命令直接Disable,0表示不超时,>0 指具体超时秒数
         */
        private int timeout;

        public CmdMeta(BaseCmd cmd, Method method) {
            this.cmd = cmd;
            this.method = method;
            this.baseName = cmd.getClass().getSimpleName() + ".*";
            this.name = cmd.getClass().getSimpleName() + "." + method.getName();
            stat = new StageTimeSpanStat(name);
        }

        public void access(ContextAttachment attach) {
            stat.record(attach.getEncode(), attach.getProcess(), attach);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CmdMeta other = (CmdMeta) obj;
            if (cmd == null) {
                if (other.cmd != null) {
                    return false;
                }
            } else if (!cmd.equals(other.cmd)) {
                return false;
            }
            if (method == null) {
                if (other.method != null) {
                    return false;
                }
            } else if (!method.equals(other.method)) {
                return false;
            }
            return true;
        }

        public BaseCmd getCmd() {
            return cmd;
        }

        public Method getMethod() {
            return method;
        }

        public long getNum() {
            return stat.getAllNum();
        }

        public int getTimeout() {
            return timeout;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((cmd == null) ? 0 : cmd.hashCode());
            result = prime * result + ((method == null) ? 0 : method.hashCode());
            return result;
        }

        public boolean isDisable() {
            return timeout < 0;
        }

        /**
         * 重置计数器
         */
        public void resetCounter() {
            stat = new StageTimeSpanStat(name);
        }

        @Override
        public String toString() {
            return name;
        }

        /**
         * 获取CMD相关的描述信息，用于生成在线DOC
         */
        public String getCmdDescription() {
            CmdDescr c = cmd.getClass().getAnnotation(CmdDescr.class);
            if (null != c) {
                return c.value();
            }
            return "";
        }

        public StageTimeSpanStat getStat() {
            return stat;
        }

        public void setStat(StageTimeSpanStat stat) {
            this.stat = stat;
        }

    }

    private static final String GLOBAL_TIMEOUT = "GLOBAL_TIMEOUT";
    private static final Logger log = Log.getLogger();

    /**
     * 去除请求url的路径中最后一个"/"号
     */
    private static String sanitizePath(String path) {
        int len = path.length();
        if (len > 1 && path.lastIndexOf('/') == len - 1) {
            path = path.substring(0, len - 1);
        }
        return path;
    }

    private Map<String, CmdMeta> annotation_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> auto_cmd_map = Collections.emptyMap();
    private Map<String, CmdMeta> config_cmd_map = Collections.emptyMap();

    private Map<String, CmdMeta> cmdAllMap = new LinkedHashMap<String, CmdMeta>();
    private Map<CmdMeta, List<String>> reverseCmdAllSortedMap;

    @Config
    private String cmdmapper_config_filename = "cmdmapper";
    private ResourceBundle cmdMapperBundle = null;
    private final Map<CmdMeta, CmdMeta> cmdMetaUnite = new HashMap<CmdMeta, CmdMeta>();
    private List<BaseCmd> cmds;// 所有spring中配置好且符合cmd类规范的对象
    @Autowired
    private HttpServerConfig config;

    private Set<Class<? extends BaseCmd>> disableCmdClass = new HashSet<Class<? extends BaseCmd>>(0);
    private Set<Method> disableCmdMethod = new HashSet<Method>(0);
    private PathMap fuzzyMap;
    private int globalTimeout = Integer.MIN_VALUE;

    private String _lower(String name) {
        return name.substring(0, 1).toLowerCase() + name.substring(1);
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

    private BaseCmd getCmd(String name) {
        Object obj = BeanUtil.getTypedBean(name);
        if (obj instanceof BaseCmd) {
            Class<?> clazz = obj.getClass();
            if (clazz.getAnnotation(Deprecated.class) != null || disableCmdClass.contains(clazz)) {
                return null;
            }
            return (BaseCmd) obj;
        }
        return null;
    }

    public Map<CmdMeta, List<String>> getReverseCmdAllSortedMap() {
        if (reverseCmdAllSortedMap == null) {
            Map<CmdMeta, List<String>> tmp = new LinkedHashMap<CmdMeta, List<String>>();
            _buildReverseCmdAllSortedMap(tmp, auto_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, annotation_cmd_map);
            _buildReverseCmdAllSortedMap(tmp, config_cmd_map);
            reverseCmdAllSortedMap = tmp;
        }
        return reverseCmdAllSortedMap;
    }

    private void _buildReverseCmdAllSortedMap(Map<CmdMeta, List<String>> tmp, Map<String, CmdMeta> ori) {
        for (Map.Entry<String, CmdMeta> e : ori.entrySet()) {
            CmdMeta meta = e.getValue();
            String url = e.getKey();
            List<String> list = tmp.get(meta);
            if (list == null) {
                list = new ArrayList<String>(1);
                tmp.put(meta, list);
            }
            list.add(url);
        }
    }

    public ResourceBundle getCmdMapperBundle() {
        if (cmdMapperBundle == null) {
            cmdMapperBundle = ResourceBundleUtil.reload(cmdmapper_config_filename);
            if (cmdMapperBundle == null) {
                cmdMapperBundle = new ResourceBundle() {

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

                    @Override
                    protected Object handleGetObject(String key) {
                        return null;
                    }
                };
            }
        }
        return cmdMapperBundle;
    }

    public CmdMeta getCmdMeta(String path) throws Exception {
        path = sanitizePath(path);
        CmdMeta meta = cmdAllMap.get(path);
        if (meta == null && fuzzyMap != null) {
            return (CmdMeta) fuzzyMap.match(path);
        }
        return meta;
    }

    private String getCmdName(BaseCmd cmd) {
        String cmdSuffix = config.getCmdSuffix();
        String cmdName = _lower(cmd.getClass().getSimpleName());

        // 这里解决 因为cglib造成其类名被替换的情况
        // 2011-03-18 原来没有此bug的原因是因为原来是不先通过getCmds() 获得其BaseCmd再继续获得其className,而是直接通过 Bootstrap.CONTEXT.getBeanDefinitionNames()来找到的
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
        if (cmds == null) {
            cmds = new ArrayList<BaseCmd>();
            for (String name : SpringBootstrap.getContext().getBeanDefinitionNames()) {
                BaseCmd cmd = getCmd(name);
                if (cmd != null) {
                    cmds.add(cmd);
                }
            }
        }
        return cmds;
    }

    /**
     * 全局命令超时设置,单位秒
     */
    public int getGlobalTimeout() {
        if (globalTimeout == Integer.MIN_VALUE) {
            int noTimeout = 0;
            try {
                String v = getCmdMapperBundle().getString(GLOBAL_TIMEOUT);
                globalTimeout = ValueUtil.getInteger(v, noTimeout);// 默认为0,也就是不超时
            } catch (Exception e) {
                globalTimeout = noTimeout;
            }
        }
        return globalTimeout;
    }

    /**
     * 获得一个命令对应的超时，单位秒。优先级规则是 单个cmd配置 > 类下所有cmd配置 > 全局配置
     */
    public int getCmdMetaTimeout(CmdMeta cmd) {
        int defaultTimeout = getGlobalTimeout();
        try {
            try {
                String v1 = getCmdMapperBundle().getString(cmd.baseName);
                defaultTimeout = ValueUtil.getInteger(v1, defaultTimeout);
            } catch (Exception e) {
            }
            String v2 = getCmdMapperBundle().getString(cmd.toString());
            return ValueUtil.getInteger(v2, defaultTimeout);
        } catch (Exception e) {
            return defaultTimeout;
        }
    }

    public Map<String, CmdMeta> initCmdMapperDefinedMap() {
        Map<String, CmdMeta> tmp = new LinkedHashMap<String, CmdMeta>();
        for (CmdMeta meta : cmdMetaUnite.values()) {
            CmdMapper m = meta.getMethod().getAnnotation(CmdMapper.class);
            if (m != null) {
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
            if (cmdPathForClazz != null) {
                for (String alias : cmdPathForClazz.value()) {
                    cmdNameList.add(alias);
                }
            }

            for (String cmdName : cmdNameList) {
                // Method[] mehods = cmdMiscMethodExtenable ? clazz.getMethods() : clazz.getDeclaredMethods();
                Method[] mehods = clazz.getDeclaredMethods();
                for (Method method : mehods) {
                    if (!isCmdMethod(cmd, method)) {
                        continue;
                    }
                    CmdMeta meta = null;
                    String methodName = method.getName();

                    // if (method.getDeclaringClass().equals(cmd.getClass())) {
                    // if (methodName.equals(cmdDefaultMethod)) {
                    // meta = newCmdMeta(cmd, method);
                    //
                    // tmp_auto.put("/" + cmdName, meta);
                    // if (cmdDefaultMethodVisiable)
                    // tmp_expand.put(methodUrl, meta);
                    // } else {
                    ArrayList<String> urlList = new ArrayList<String>(1);
                    if (method.getAnnotation(CmdOverride.class) == null) {
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

    private void putUrl(Map<String, CmdMeta> tmp, String url, CmdMeta meta) {
        if (url == null) {
            return;
        }
        url = url.trim();
        if (url.isEmpty()) {
            return;
        }
        if (url.contains("*")) {// 模糊匹配
            if (fuzzyMap == null) {
                fuzzyMap = new PathMap();
            }
            fuzzyMap.put(url, meta);
            return;
        }
        // if (!url.startsWith("/")) {
        // url = "/" + url;
        // // log.info("ignore url:{} for CmdMappers", url);
        // // return;
        // }
        url = sanitizePath(url);
        tmp.put(url, meta);
    }

    /**
     * <pre>
     * 重置所有命令的计数器
     * 注意,这里没有进行并发处理
     * 也就是在重置过程中,HttpServer还能允许新请求进来
     * 影响不大,所以不对此问题进行处理
     */
    public void resetAllCounter() {
        for (CmdMeta meta : cmdMetaUnite.keySet()) {
            meta.resetCounter();
        }
    }

    @Config(resetable = true)
    private boolean interruptClosedChannel = true;// 2011-12-19 现在为了支持attachCmd，需要任何时候都打开

    public StringBuilder resetCmdConfig() {
        cmdMapperBundle = null;// 清空原来的cmdMapperBundle,这样可以重新读配置文件
        globalTimeout = Integer.MIN_VALUE;// 重置
        boolean damonScannerEnable = interruptClosedChannel;

        StringBuilder sb = new StringBuilder();
        StringBuilder sb1 = new StringBuilder();
        for (CmdMeta meta : cmdMetaUnite.keySet()) {
            meta.timeout = getCmdMetaTimeout(meta);
            if (meta.isDisable()) {
                sb.append(meta.name).append(", ");
            } else if (meta.getTimeout() > 0) {// 只要有个命令有设置超时,后台扫描器就要打开
                damonScannerEnable = true;
                sb1.append(meta.name).append("=").append(meta.getTimeout()).append(", ");
            }
        }
        StringBuilder r = new StringBuilder();

        if (EmptyChecker.isNotEmpty(sb)) {
            String msg = sb.substring(0, sb.length() - 2);
            log.error("DISABLE_SET:\t\t{{}}", msg);
            r.append("DISABLE_SET:\t\t{").append(msg).append("}\n");
        }
        if (EmptyChecker.isNotEmpty(sb1)) {
            String msg = sb1.substring(0, sb1.length() - 2);
            log.error("TIMEOUT_SET:\t\t{{}}", msg);
            r.append("TIMEOUT_SET:\t\t{").append(msg).append("}\n");
        }
        r.append("GlobalTimeout:\t\t").append(getGlobalTimeout());
        if (getGlobalTimeout() <= 0) {
            r.append("\t").append("警告，设置为0会导致不能清理，有内存泄漏风险");
        }
        r.append("\n");

        timeoutInterrupter.setThreadInterrupterEnable(damonScannerEnable);
        boolean enable = timeoutInterrupter.isEnable();
        r.append(enable ? "TimeoutInterrupter Enable" : "TimeoutInterrupter Disable");
        return r;
    }

}
