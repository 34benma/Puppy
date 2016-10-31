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

package cn.wantedonline.puppy.httpserver.component;

import ch.qos.logback.access.spi.AccessEvent;
import ch.qos.logback.core.BasicStatusManager;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.spi.*;
import ch.qos.logback.core.status.InfoStatus;
import ch.qos.logback.core.status.StatusManager;
import ch.qos.logback.core.status.WarnStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by louiswang on 16/10/31.
 */
public class AccessLogger implements Context, AppenderAttachable<AccessEvent>, FilterAttachable<AccessEvent> {

    public static final String DEFAULT_CONFIG_FILE = "logback-access.xml";

    private long birthTime = System.currentTimeMillis();
    Object configurationLock = new Object();

    private String name;
    StatusManager sm = new BasicStatusManager();
    Map<String, String> propertyMap = new HashMap<>();
    Map<String, Object> objectMap = new HashMap<>();
    private FilterAttachableImpl<AccessEvent> fai = new FilterAttachableImpl<>();

    AppenderAttachableImpl<AccessEvent> aai = new AppenderAttachableImpl<>();
    String fileName;
    boolean quiet;
    boolean started;
    boolean alreadySetLogbackStatusManageer = false;

    private boolean logEnable = true;
    private boolean logSuccess = false;
    private InnerLog logimpl;
    private static final InnerLog NOP_LOG = new InnerLog();
    private final InnerLog defaultLog = new DefaultLog();

    public AccessLogger() {
        start();
        putObject(CoreConstants.EVALUATOR_MAP, new HashMap<Object, Object>());
    }

    public AccessLogger(String fileName) {
        try {
            this.fileName = getClass().getClassLoader().getResource(fileName).getFile();
            getStatusManager().add(new InfoStatus("loading accessContext file[" + fileName + "]", this));
        } catch (Throwable e) {
            getStatusManager().add(new WarnStatus("[" + fileName + "] does not exist", this));
            throw new RuntimeException();
        }
        start();
        putObject(CoreConstants.EVALUATOR_MAP, new HashMap<Object, Object>());
    }

    public void start() {

    }

    private static class InnerLog {
        public void log(AccessEvent accessEvent) {

        }

        public void log(HttpRequest request, HttpResponse response) {

        }
    }

    private class DefaultLog extends InnerLog {
        @Override
        public void log(AccessEvent accessEvent) {
            if (getFilterChainDecision(accessEvent) == FilterReply.DENY) {
                return;
            }
            aai.appendLoopOnAppenders(accessEvent);
        }

        @Override
        public void log(HttpRequest request, HttpResponse response) {
            AccessEvent accessEvent = new AccessEvent(request, response);

        }
    }

}
