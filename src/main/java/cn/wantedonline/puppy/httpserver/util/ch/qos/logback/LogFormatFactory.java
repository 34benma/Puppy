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

package cn.wantedonline.puppy.httpserver.util.ch.qos.logback;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by louiswang on 16/11/1.
 */
public class LogFormatFactory {
    private static final Map<String, LogFormatFactory> LFF_CACHE_MAP = new ConcurrentHashMap<>(1);

    public static LogFormatFactory getInstance(String split) {
        LogFormatFactory lff = LFF_CACHE_MAP.get(split);
        if (null == lff) {
            LogFormatFactory new_lff = new LogFormatFactory(split);
            LogFormatFactory new_lff1 = LFF_CACHE_MAP.put(split, new_lff);
            lff = new_lff1 == null ? new_lff : new_lff1;
        }
        return lff;
    }

    private String _maxArgsLenFormat = "";
    private List<String> formats;
    private String split;

    private LogFormatFactory(String split) {
        this.split = split;
        this.formats = new ArrayList<String>();
    }

    public String getFormat(int argsLen) {
        int index = argsLen - 1;
        if (formats.size() >= argsLen) {
            return formats.get(index);
        }
        synchronized (this) {
            if (formats.size() < argsLen) {
                StringBuilder tmp = new StringBuilder(_maxArgsLenFormat);
                for (int i = formats.size(); i < argsLen; i++) {
                    formats.add(tmp.append("{}").toString());
                    tmp.append(split);
                }
                _maxArgsLenFormat = tmp.toString();
            }
            return formats.get(index);
        }
    }

    public String getFormat(Object[] args) {
        if (null == args) {
            return "";
        }
        return getFormat(args.length);
    }
}
