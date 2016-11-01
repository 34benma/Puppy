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

package cn.wantedonline.puppy.httpserver.util.codec;

import cn.wantedonline.puppy.httpserver.util.CharsetTools;
import cn.wantedonline.puppy.util.StringTools;

import java.io.CharArrayWriter;
import java.nio.charset.Charset;
import java.util.BitSet;


public class URICodec {

    private static final int caseDiff = ('a' - 'A');

    private static final Charset defaultCharset = CharsetTools.UTF_8;
    private static final BitSet dontNeedEncoding;

    static {
        dontNeedEncoding = new BitSet(256);
        int i;
        for (i = 'a'; i <= 'z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = 'A'; i <= 'Z'; i++) {
            dontNeedEncoding.set(i);
        }
        for (i = '0'; i <= '9'; i++) {
            dontNeedEncoding.set(i);
        }
        dontNeedEncoding.set('-');
        dontNeedEncoding.set('_');
        dontNeedEncoding.set('.');
        dontNeedEncoding.set('!');
        dontNeedEncoding.set('~');
        dontNeedEncoding.set('*');
        dontNeedEncoding.set('\'');
        dontNeedEncoding.set('(');
        dontNeedEncoding.set(')');
    }

    public static String decode(String s) {
        return decode(s, defaultCharset);
    }

    /**
     * 对URL进行解码，抄自java.net.URLDecoder，其不同之处在于+不会转为空格而是处理为%2B，与前端js的decodeURIComponent效果保持一致
     */
    public static String decode(String s, Charset charset) {
        boolean needToChange = false;
        int numChars = s.length();
        StringBuilder sb = new StringBuilder(numChars > 500 ? numChars / 2 : numChars);
        int i = 0;
        char c;
        byte[] bytes = null;
        while (i < numChars) {
            c = s.charAt(i);
            switch (c) {
            case '%':
                /*
                 * Starting with this instance of %, process all consecutive substrings of the form %xy. Each substring %xy will yield a byte. Convert all consecutive bytes obtained this way to
                 * whatever character(s) they represent in the provided encoding.
                 */
                try {
                    // (numChars-i)/3 is an upper bound for the number
                    // of remaining bytes
                    if (bytes == null) {
                        bytes = new byte[(numChars - i) / 3];
                    }
                    int pos = 0;

                    while (((i + 2) < numChars) && (c == '%')) {
                        bytes[pos++] = (byte) Integer.parseInt(s.substring(i + 1, i + 3), 16);
                        i += 3;
                        if (i < numChars) {
                            c = s.charAt(i);
                        }
                    }
                    // A trailing, incomplete byte encoding such as
                    // "%x" will cause an exception to be thrown
                    if ((i < numChars) && (c == '%')) {
                        throw new IllegalArgumentException("decodeURIComponent: Incomplete trailing escape (%) pattern");
                    }
                    sb.append(new String(bytes, 0, pos, charset));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("decodeURIComponent: Illegal hex characters in escape (%) pattern - " + e.getMessage());
                }
                needToChange = true;
                break;
            default:
                sb.append(c);
                i++;
                break;
            }
        }
        return (needToChange ? sb.toString() : s);
    }

    /**
     * 智能解码，可以在不知道编码的情况下进行分析，有一定的误报率，请谨慎使用
     */
    public static String smartDecode(String str) {
        String dec = decode(str, CharsetTools.UTF_8);
        if (StringTools.hasInvalidCharacter(dec)) {
            return decode(str, CharsetTools.GB18030);
        }
        return dec;
    }

    public static String encode(String s) {
        return encode(s, defaultCharset);
    }

    /**
     * 对URL进行编码，抄自java.net.URLEncoder，其不同之处在于空格不会转为+而是转为%20，与前端js的encodeURIComponent效果保持一致
     */
    public static String encode(String s, Charset charset) {
        boolean needToChange = false;
        StringBuilder out = new StringBuilder(s.length());
        CharArrayWriter charArrayWriter = new CharArrayWriter();
        for (int i = 0; i < s.length();) {
            int c = s.charAt(i);
            if (dontNeedEncoding.get(c)) {
                out.append((char) c);
                i++;
            } else {
                do {
                    charArrayWriter.write(c);
                    if (c >= 0xD800 && c <= 0xDBFF) {
                        if ((i + 1) < s.length()) {
                            int d = s.charAt(i + 1);
                            if (d >= 0xDC00 && d <= 0xDFFF) {
                                charArrayWriter.write(d);
                                i++;
                            }
                        }
                    }
                    i++;
                } while (i < s.length() && !dontNeedEncoding.get((c = s.charAt(i))));
                charArrayWriter.flush();
                String str = new String(charArrayWriter.toCharArray());
                byte[] ba = str.getBytes(charset);
                for (int j = 0; j < ba.length; j++) {
                    out.append('%');
                    char ch = Character.forDigit((ba[j] >> 4) & 0xF, 16);
                    // converting to use uppercase letter as part of
                    // the hex value if ch is a letter.
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                    ch = Character.forDigit(ba[j] & 0xF, 16);
                    if (Character.isLetter(ch)) {
                        ch -= caseDiff;
                    }
                    out.append(ch);
                }
                charArrayWriter.reset();
                needToChange = true;
            }
        }
        return (needToChange ? out.toString() : s);
    }

    private URICodec() {
    }
}
