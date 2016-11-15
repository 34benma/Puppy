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

package cn.wantedonline.puppy.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.Character.UnicodeBlock;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 字符串工具类
 *
 * 引用并修改 by louiswang
 *
 * @author 迅雷 ZengDong
 * @author wangcheng
 */
public final class StringTools {

    /**
     * 判断字符串是否为空，判断时首尾空格会被去掉。
     * 
     * @param str
     * @return
     */
    public static boolean isBlank(CharSequence str) {
        if (str == null) {
            return true;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            if (c > ' ') {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断字符串是否为空
     * @param str
     * @return
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * 判断字符串是否为空，判断时首尾空格会被去掉。
     * @param str
     * @return
     */
    public static boolean isNotBlank(CharSequence str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否不是空的
     * 
     * @param str
     * @return
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否不是数字字符串
     * @param str
     * @return
     */
    public static boolean isNotNumberStr(CharSequence str) {
        return !isNumberStr(str);
    }

    /**
     * 判断字符串是否为数字字符串
     * @param str
     * @return
     */
    public static boolean isNumberStr(CharSequence str) {
        if (isEmpty(str)) {
            return false;
        }
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除字符串中的换行符（删除\r,\n）
     * @param src
     * @return
     */
    public static String removeNewLines(String src) {
        if (isNotEmpty(src)) {
            return src.replace("\n", "").replace("\r", "");
        }
        return "";
    }

    /**
     * 字符串split后,并trim,去除空字符串
     * @param str
     * @param regex
     * @return
     */
    public static List<String> splitAndTrim(String str, String regex) {
        String[] arr = str.split(regex);
        List<String> list = new ArrayList<String>(arr.length);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                list.add(add);
            }
        }
        return list;
    }

    /**
     * 字符串split后,并trim,去除空字符串,最后放入result此集合中
     * @param str
     * @param regex
     * @param result
     * @return
     */
    public static Collection<String> splitAndTrim(String str, String regex, Collection<String> result) {
        String[] arr = str.split(regex);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                result.add(add);
            }
        }
        return result;
    }

    /**
     * 字符串split后,并trim,去除空字符串,最终放入转成对应componentClazz(只支持基本)类型的集合
     * @param <T>
     * @param str
     * @param regex
     * @param result
     * @param componentClazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Collection<T> splitAndTrim(String str, String regex, Collection<T> result, Class<T> componentClazz) {
        Method method = ValueUtil.getValueOfMethod(componentClazz);
        String[] arr = str.split(regex);
        for (String a : arr) {
            String add = a.trim();
            if (add.length() > 0) {
                try {
                    result.add((T) method.invoke(null, add));
                } catch (Exception e) {
                }
            }
        }
        return result;
    }

    /**
     * 将字符串分割,trim后封装成String数组
     * @param str
     * @param regex
     * @return
     */
    public static String[] splitAndTrimAsArray(String str, String regex) {
        return splitAndTrim(str, regex).toArray(ValueUtil.REF_ARRAY_STRING);
    }

    /**
     * <pre>
     * str
     *   --split()--> Collection<String>
     *   --toArray()--> T[]
     *   
     * 此方法可以处理 原始数据类型,故返回值是 Object,须得到值后再转型
     * </pre>
     * 
     * @param <T>
     * @param str
     * @param regex
     * @param componentClazz
     * @return
     */
    public static <T> Object splitAndTrimAsArray(String str, String regex, Class<T> componentClazz) {
        return ValueUtil.toArray(splitAndTrim(str, regex, new ArrayList<T>(2), componentClazz), componentClazz);
    }

    /**
     * 处理字符串的html转义字符
     */
    public static String escapeHtml(String source) {
        if (isNotEmpty(source)) {
            return source.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace(" ", "&nbsp;").replace("\n", "<br/>").replace("\r", "").replace("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        }
        return "";
    }

    /**
     * 处理字符串的xml转义字符
     */
    public static String escapeXml(String source) {
        if (isNotEmpty(source)) {
            return source.replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("&", "&amp;").replace("'", "&apos;");
        }
        return "";
    }

    /**
     * 对传入的字符串进行截断，如果长度超过length就截取前length个字符的子串，如果小于length就原样返回
     */
    public static String truncate(String source, int length) {
        if (isEmpty(source) || length <= 0) {
            return source;
        }
        return source.length() > length ? source.substring(0, length) : source;
    }

    private static final String[] STRING_ESCAPE_LIST;
    static {
        STRING_ESCAPE_LIST = new String[93]; // ascii最大的需要转义的就是\(93)
        STRING_ESCAPE_LIST['\\'] = "\\\\";
        STRING_ESCAPE_LIST['\"'] = "\\\"";
        STRING_ESCAPE_LIST['\''] = "\\\'";
        STRING_ESCAPE_LIST['\r'] = "\\r";
        STRING_ESCAPE_LIST['\n'] = "\\n";
        STRING_ESCAPE_LIST['\f'] = "\\f";
        STRING_ESCAPE_LIST['\t'] = "\\t";
        STRING_ESCAPE_LIST['\b'] = "\\b";
    }

    /**
     * 转义为""中可用的字符串
     */
    public static String escapeString(String source) {
        if (isNotEmpty(source)) {
            StringBuilder sb = new StringBuilder(source.length() + 16);
            for (int i = 0; i < source.length(); i++) {
                char ch = source.charAt(i);
                if (ch < STRING_ESCAPE_LIST.length) {
                    String append = STRING_ESCAPE_LIST[ch];
                    sb.append(null != append ? append : ch);
                } else {
                    sb.append(ch);
                }
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * <pre>
     * str
     *   --split()--> Collection<String>
     *   --toArray()--> T[]
     *   
     * 此方法使用引用refArray的方式,不能处理 原始数据类型,但返回值支持泛型
     * </pre>
     * 
     * @param <T>
     * @param str
     * @param regex
     * @param refArray
     * @return
     */
    @SuppressWarnings({
        "unchecked",
        "rawtypes",
        "cast"
    })
    public static <T> T[] splitAndTrimAsArray(String str, String regex, T[] refArray) {
        return ValueUtil.toArray((Collection<T>) splitAndTrim(str, regex, new ArrayList(2), refArray.getClass().getComponentType()), refArray);
    }

    /**
     * 清空StringBuilder内容
     * @param sb
     * @return
     */
    public static StringBuilder clearStringBuilder(StringBuilder sb) {
        sb.delete(0, sb.length());
        return sb;
    }

    public static StringBuilder subStringBuilder(StringBuilder sb, int start, int end) {
        int count = sb.length();
        if (end < count) {
            sb.delete(end, count);
        }
        if (start > 0) {
            sb.delete(0, start);
        }
        return sb;
    }

    public static StringBuilder subStringBuilder(StringBuilder sb, int start) {
        if (start > 0) {
            sb.delete(0, start);
        }
        return sb;
    }

    private StringTools() {
    }

    public static String trim(String str) {// 抄自String.trim 加上对全角空格的处理
        if (isEmpty(str)) {
            return "";
        }
        int len = str.length();
        int count = str.length();
        int st = 0;

        while ((st < len) && ((str.charAt(st) <= ' ') || str.charAt(st) == '　')) {
            st++;
        }
        while ((st < len) && ((str.charAt(len - 1) <= ' ') || str.charAt(len - 1) == '　')) {
            len--;
        }
        return ((st > 0) || (len < count)) ? str.substring(st, len) : str;
    }

    private static final String[] SQL_LIKE_PATTERN;
    static {
        SQL_LIKE_PATTERN = new String[96];
        SQL_LIKE_PATTERN['%'] = "\\%";
        SQL_LIKE_PATTERN['_'] = "\\_";
    }

    /**
     * ESCAPE MySql的LIKE语句部分，防注入
     */
    public static String escapeSqlLikePattern(String keyword) {
        if (isEmpty(keyword)) {
            return "";
        }
        StringBuilder sb = new StringBuilder(keyword.length());
        for (int i = 0; i < keyword.length(); i++) {
            char ch = keyword.charAt(i);
            if (ch < SQL_LIKE_PATTERN.length) {
                String append = SQL_LIKE_PATTERN[ch];
                sb.append(null != append ? append : ch);
            } else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * 允许用户提交的字符集列表（包括繁简中文、日文、韩文，另外取用了QQ拼音输入法的符号输入器所有能输入的符号，这些都能在Win7上正常显示）
     */
    private static final Set<String> visibleCharacters;
    static {
        visibleCharacters = new HashSet<String>();
        visibleCharacters.add(UnicodeBlock.BASIC_LATIN.toString()); // 基本ASCII字符，包括控制字符、英文字母、数字、运算符等
        visibleCharacters.add(UnicodeBlock.GENERAL_PUNCTUATION.toString()); // 基本标点符号
        visibleCharacters.add(UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION.toString()); // CJK符号
        visibleCharacters.add(UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS.toString()); // 半角全角符号
        visibleCharacters.add(UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS.toString()); // CJK字符（中文）
        visibleCharacters.add(UnicodeBlock.HANGUL_SYLLABLES.toString()); // 韩语
        visibleCharacters.add(UnicodeBlock.HIRAGANA.toString()); // 日文平假名
        visibleCharacters.add(UnicodeBlock.KATAKANA.toString()); // 日文片假名
        visibleCharacters.add(UnicodeBlock.DINGBATS.toString());
        visibleCharacters.add(UnicodeBlock.SMALL_FORM_VARIANTS.toString());
        visibleCharacters.add(UnicodeBlock.CJK_COMPATIBILITY_FORMS.toString());
        visibleCharacters.add(UnicodeBlock.ENCLOSED_CJK_LETTERS_AND_MONTHS.toString());
        visibleCharacters.add(UnicodeBlock.NUMBER_FORMS.toString());
        visibleCharacters.add(UnicodeBlock.ENCLOSED_ALPHANUMERICS.toString());
        visibleCharacters.add(UnicodeBlock.MATHEMATICAL_OPERATORS.toString());
        visibleCharacters.add(UnicodeBlock.LATIN_1_SUPPLEMENT.toString());
        visibleCharacters.add(UnicodeBlock.GREEK.toString());
        visibleCharacters.add(UnicodeBlock.CJK_COMPATIBILITY.toString());
        visibleCharacters.add(UnicodeBlock.SPACING_MODIFIER_LETTERS.toString());
        visibleCharacters.add(UnicodeBlock.MISCELLANEOUS_TECHNICAL.toString());
        visibleCharacters.add(UnicodeBlock.SUPERSCRIPTS_AND_SUBSCRIPTS.toString());
        visibleCharacters.add(UnicodeBlock.LETTERLIKE_SYMBOLS.toString());
        visibleCharacters.add(UnicodeBlock.GEOMETRIC_SHAPES.toString());
        visibleCharacters.add(UnicodeBlock.CYRILLIC.toString());
        visibleCharacters.add(UnicodeBlock.LATIN_EXTENDED_A.toString());
        visibleCharacters.add(UnicodeBlock.LATIN_EXTENDED_B.toString());
        visibleCharacters.add(UnicodeBlock.BOPOMOFO.toString());
        visibleCharacters.add(UnicodeBlock.HANGUL_COMPATIBILITY_JAMO.toString());
        visibleCharacters.add(UnicodeBlock.IPA_EXTENSIONS.toString());
        visibleCharacters.add(UnicodeBlock.BOX_DRAWING.toString());
        visibleCharacters.add(UnicodeBlock.MISCELLANEOUS_SYMBOLS.toString());
        visibleCharacters.add(UnicodeBlock.BLOCK_ELEMENTS.toString());
        visibleCharacters.add(UnicodeBlock.ARABIC.toString());
        visibleCharacters.add(UnicodeBlock.THAI.toString());
        visibleCharacters.add(UnicodeBlock.ARROWS.toString());
    }

    /**
     * 分析一下给定的字符串是否包含平台不能识别的字符
     */
    public static boolean hasInvalidCharacter(String str) {
        for (int i = 0; i < str.length(); i++) {
            char ch = str.charAt(i);
            // 检查字符集，此功能针对其他平台提交的特殊字符，防止出现无法显示的字符
            UnicodeBlock ub = UnicodeBlock.of(ch);
            if (null == ub || !StringTools.visibleCharacters.contains(ub.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * <pre>
     * 自动选择对应的charset来对字节码解码
     * 
     * 只支持GBK和UTF8
     * 
     * 原理:com.xunlei.subtitle.util.EncodingUtil.detectAndTransferEncoding(InputStream)
     * 
     * <pre>
     * 没有读取到BOM，就只能根据内容来判断了
     * 判断的原理就是看出现的内容的范围有没符合UTF-8规范
     * UTF-8是一种不定长的编码方式，类似Huffman编码，一个字符，可能被编码为1-4个字节
     * 
     * Unicode编码(16进制)　 UTF-8字节流(二进制)                   UTF-8编码后的范围(16进制)
     * 000000 - 00007F　     0xxxxxxx                              00-7F
     * 000080 - 0007FF　     110xxxxx 10xxxxxx                     C0-DF 80-BF
     * 000800 - 00FFFF　     1110xxxx 10xxxxxx 10xxxxxx            E0-EF 80-BF 80-BF
     * 010000 - 10FFFF　     11110xxx 10xxxxxx 10xxxxxx 10xxxxxx   F0-F7 80-BF 80-BF 80-BF
     * 
     * ascii字符都在第一个范围内，所以没有BOM头的只包含ascii字符的文件，其UTF-8编码形式和GBK编码形式内容是完全一样的
     * 
     * eg:“汉”字的Unicode编码是0x6C49
     * 0x6C49在0x0800-0xFFFF之间，应当使用3字节模板了
     * 1110xxxx 10xxxxxx 10xxxxxx
     * 0x6C49写成二进制是0110 1100 0100 1001，用这个依次代替模板中的x
     * 得到11100110 10110001 10001001，即E6 B1 89，即为UTF-8编码形式
     * 
     * 中日韩文(CJK)单字一般被编成3个字节，如果内容中有符合编码规范的字节，就能基本认定是UTF-8了
     * 当然不是100%靠谱，这里为了能快速判定，只要出现一次符合规范的，就认定是UTF-8
     * 
     * 各字符集编码范围
     * 
     * GB2312-80
     *   Ascii Chars:               00-7F
     *   Simplified Chinese Chars:  A1-F7 + A1-FE
     *   Note: A9 + A4-EF ==> Tabs in Chinese Chars
     * 
     * Big5
     *   Ascii Chars:               00-7F
     *   Traditional Chinses Chars: A1-F9 + 40-7E
     *                              A1-F9 + A1-FE
     * 
     * GBK
     *   Ascii Chars:               00-7F
     *   Chinses Chars:             81-FE + 40-7E
     *                              81-FE + 80-FE
     * 
     * EUC-KR
     *                              A1-FE + A1-FE
     * 
     * Unicode CJK范围
     * CJK基本　　　[004E00-009FFF]    20992码位　实际20940字
     * CJK扩展A　　 [003400-004DBF]    6592码位 　实际6582字
     * CJK扩展B　　 [020000-02A6DF]    42720码位　实际42711字
     * CJK扩展C　　 [02A700-02B73F]    4159码位　 实际4149字
     * CJK扩展D　　 [02B740-02B81F]    224码位　　实际222字
     * CJK兼容扩展  [02F800-02FA1F]    544码位　　实际542字
     * CJK部首扩展  [002E80-002EFF]    128码位　　实际115字
     * CJK康熙部首  [002F00-002FDF]    224码位　　实际214字
     * CJK笔画　　  [0031C0-0031EF]    48码位　　 实际36字
     * CJK兼容      [00F900-00FAFF]    512个码位  实际477字
     * PUA（GBK）   [00E815-00E86F]    90个码位   实际80字
     * PUA部件扩展  [00E400-00E5FF]    511个码位  实际452字
     * PUA缺字增补  [00E600-00E6BF]    191个码位  实际185字
     * 
     * 如果只考虑CJK的话，就不会出现4个字节的UTF-8字符，即不会有>=F0的字节，而80-FF是GBK/BIG5的范围
     * 所以可以认为，只要>=F0的字节出现，就很有可能是GBK/BIG5字符
     * </pre>
     * 
     * @param bytes
     * @return
     */
    public static String smartDecode(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
        String charset = "GB18030"; // 默认是GB18030
        int read = 0;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((read = bis.read()) != -1) {
            baos.write(read);
            if (read >= 0xF0) {
                break;
            }
            if (0x80 <= read && read <= 0xBF) { // 按UTF-8的编码规则，不会在开头就出现这个范围的字节，如果出现那就一定不是UTF-8
                break;
            }
            if (0xC0 <= read && read <= 0xDF) { // UTF-8编码为2个字节的情况，由于两个字节是GBK的编码范围，并不足以确认是UTF-8，需要进一步判断
                read = bis.read();
                if (read == -1) {
                    break;
                }
                baos.write(read);
                if (0x80 <= read && read <= 0xBF) {
                    continue;
                }
                break;
            } else if (0xE0 <= read && read <= 0xEF) { // UTF-8编码为3个字节的情况，不能确保100%准确,只要有中文的肯定会有三字节的..
                read = bis.read();
                if (read == -1) {
                    break;
                }
                baos.write(read);
                if (0x80 <= read && read <= 0xBF) {
                    read = bis.read();
                    if (read == -1) {
                        break;
                    }
                    baos.write(read);
                    if (0x80 <= read && read <= 0xBF) {
                        charset = "UTF-8";
                        break;
                    }
                    break;
                }
                break;
            }
        }
        // 继续把内容读完以备转码
        while ((read = bis.read()) != -1) {
            baos.write(read);
        }
        byte[] data = baos.toByteArray();
        CloseableHelper.closeSilently(baos);
        try {
            return new String(data, charset);
        } catch (UnsupportedEncodingException e) {
            return "";
        }
    }

    /**
     * 判断一段byte对应文本的编码，按可能性判断，有误判可能
     */
    public static String detectCharset(byte[] data) {
        String output = "";
        try {
            // 判断UTF-8
            output = new String(data, "UTF-8");
            if (!hasInvalidCharacter(output)) {
                return "UTF-8";
            }
            // 判断BIG5
            output = new String(data, "BIG5");
            if (!hasInvalidCharacter(output)) {
                return "BIG5";
            }
            // 判断韩文
            output = new String(data, "EUC-KR");
            if (!hasInvalidCharacter(output)) {
                return "EUC-KR";
            }
        } catch (UnsupportedEncodingException ex) {
        }
        // GB18030最后兜底
        return "GB18030";
    }
}
