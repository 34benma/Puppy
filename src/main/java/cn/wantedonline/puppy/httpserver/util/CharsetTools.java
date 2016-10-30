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

package cn.wantedonline.puppy.httpserver.util;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

/**
 * Created by wangcheng on 2016/10/30.
 */
public class CharsetTools {
    public static final Charset GB2312 = Charset.forName("GB2312");
    public static final Charset GBK = Charset.forName("GBK");
    public static final Charset GB18030 = Charset.forName("GB18030");
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset US_ASCII = Charset.forName("US-ASCII");
    public static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Charset UTF_16 = Charset.forName("UTF-16");
    public static final Charset UTF_16BE = Charset.forName("UTF-16BE");
    public static final Charset UTF_16LE = Charset.forName("UTF-16LE");

    public static String newString(byte[] bytes, String charsetName) {
        if (null == bytes) {
            return null;
        }
        try {
            return new String(bytes, charsetName);
        } catch (UnsupportedEncodingException e) {
            throw newIllegalStateException(charsetName, e);
        }
    }

    public static byte[] getBytes(String string, String charsetName) {
        if (null == string) {
            return null;
        }
        try {
            return string.getBytes(charsetName);
        } catch (UnsupportedEncodingException e){
            throw newIllegalStateException(charsetName, e);
        }
    }

    private static IllegalStateException newIllegalStateException(String charsetName, UnsupportedEncodingException e) {
        return new IllegalStateException(charsetName + ":" + e);
    }

    private CharsetTools() {}
}
