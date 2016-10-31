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
package cn.wantedonline.puppy.httpserver.util.ch.qos.logback.access.pattern;

import java.text.MessageFormat;
import java.util.List;

import ch.qos.logback.access.pattern.AccessConverter;
import ch.qos.logback.access.spi.IAccessEvent;
import cn.wantedonline.puppy.util.ValueUtil;

/**
 * 打印部分响应内部
 * 
 * @author ZengDong
 * @since 2010-10-12 下午01:54:02
 */
public class SimplifyResponseConverter extends AccessConverter {

    private int num = 1;

    private boolean calcByChar = false;
    private boolean calcByLine = true;
    private boolean printHead = true;
    private boolean printBottom = true;
    private boolean trim = true;

    private String omitReplacerBegin = "...(";
    private String omitReplacerEnd = ")...";

    private String lineFeedReplacer = "||";
    private boolean noLineFeedReplacer = false;

    @SuppressWarnings("rawtypes")
    @Override
    public void start() { // 默认配置：1,LHB
        // 第0个参数表示 要打印的字符个数/行数
        num = ValueUtil.getInteger(getFirstOption(), 1);// 默认1行

        List optionList = getOptionList();
        if (optionList != null) {
            // 第1个参数表示 打印模式：
            // L - 按行算num
            // C - 按字符个数算num
            // H - 打印头部
            // B - 打印尾部
            // K - 不trim原内容
            if (optionList.size() > 1) {
                String mode = ((String) optionList.get(1)).toUpperCase();
                calcByChar = mode.contains("C");
                calcByLine = mode.contains("L");

                boolean printHead1 = mode.contains("H");
                boolean printBottom1 = mode.contains("B");
                if (printHead1 || printBottom1) {
                    this.printBottom = printBottom1;
                    this.printHead = printHead1;
                }
                trim = !mode.contains("K");
            }

            // 第2个参数表示 换行符号的替换符号
            if (optionList.size() > 2) {
                lineFeedReplacer = (String) optionList.get(2);
            }

            // 第3,4个参数表示 中间精简部分代替字符串
            if (optionList.size() > 4) {
                omitReplacerBegin = (String) optionList.get(3); // 开始符号
                omitReplacerEnd = (String) optionList.get(4); // 结束符号
            }
            noLineFeedReplacer = lineFeedReplacer.contains("\n") || lineFeedReplacer.contains("\r");
        }
        addInfo(this.toString());
    }

    private String simplify(String ori) {
        String out = ori;
        if (trim) {
            out = ori.trim(); // 先去空格
        }
        int len = out.length();
        if (calcByChar) {
            // 先判断原长度是否够短,可以全部打印
            if (noLineFeedReplacer) {// 如果不用替换换行符,则简单判断个数,看能否直接返回
                if (printBottom && printHead) {
                    if (len <= num * 2 + 10) {
                        return out;
                    }
                } else {
                    if (len < num + 10) {
                        return out;
                    }
                }
            }

            // 判断内容太长,开始精简
            StringBuilder buf = new StringBuilder();
            boolean cut = false;
            int lastIndex = 0;
            if (printHead) {
                int currentNum = 0;
                boolean lastCharIsLineFeed = false;
                for (lastIndex = 0; lastIndex < len; lastIndex++) {
                    char c = out.charAt(lastIndex);
                    if (c == '\n' || c == '\r') {
                        if (!lastCharIsLineFeed) {
                            buf.append(lineFeedReplacer);
                        }
                        lastCharIsLineFeed = true;
                        continue;
                    }
                    lastCharIsLineFeed = false;
                    buf.append(c);
                    if (++currentNum >= num) {
                        cut = true;
                        break;
                    }
                }
            }

            int lastInsertIndex = buf.length();

            if (printBottom) {
                cut = false;
                int currentNum = 0;
                boolean lastCharIsLineFeed = false;
                for (int i = len - 1; i > lastIndex; i--) {
                    char c = out.charAt(i);
                    if (c == '\n' || c == '\r') {
                        if (!lastCharIsLineFeed) {
                            buf.insert(lastInsertIndex, lineFeedReplacer);
                        }
                        lastCharIsLineFeed = true;
                        continue;
                    }
                    lastCharIsLineFeed = false;
                    buf.insert(lastInsertIndex, c);
                    if (++currentNum >= num) {
                        cut = true;
                        break;
                    }
                }
            }

            if (cut) {
                buf.insert(lastInsertIndex, omitReplacerEnd);
                buf.insert(lastInsertIndex, len);
                buf.insert(lastInsertIndex, omitReplacerBegin);
            }
            return buf.toString();
        }
        StringBuilder buf = new StringBuilder();
        boolean cut = false;
        // 先打印头几行
        int lastIndex = 0;
        if (printHead) {
            int currentNum = 0;
            boolean lastCharIsLineFeed = false;
            for (lastIndex = 0; lastIndex < len; lastIndex++) {
                char c = out.charAt(lastIndex);
                if (c == '\n' || c == '\r') {
                    if (!lastCharIsLineFeed) {
                        buf.append(lineFeedReplacer);
                        if (++currentNum >= num) {
                            cut = true;
                            break;
                        }
                    }
                    lastCharIsLineFeed = true;
                    continue;
                }
                lastCharIsLineFeed = false;
                buf.append(c);
            }
        }

        int lastInsertIndex = buf.length();
        if (printBottom) {
            cut = false;
            int currentNum = 0;
            boolean lastCharIsLineFeed = false;
            for (int i = len - 1; i > lastIndex; i--) {
                char c = out.charAt(i);
                if (c == '\n' || c == '\r') {
                    if (!lastCharIsLineFeed) {
                        buf.insert(lastInsertIndex, lineFeedReplacer);
                        if (++currentNum >= num) {
                            cut = true; // 发现已经凑够了 bottom的行数,表明有精简
                            break;
                        }
                    }
                    lastCharIsLineFeed = true;
                    continue;
                }
                lastCharIsLineFeed = false;
                buf.insert(lastInsertIndex, c);
            }
        }

        if (cut) {
            buf.insert(lastInsertIndex, omitReplacerEnd);
            buf.insert(lastInsertIndex, len);
            buf.insert(lastInsertIndex, omitReplacerBegin);
        }
        return buf.toString();
    }

    public static void main(String[] args) {
        SimplifyResponseConverter sc = new SimplifyResponseConverter();
        sc.calcByChar = true;
        // sc.printBottom = false;
        sc.printHead = false;
        sc.num = 20;
        sc.noLineFeedReplacer = true;
        // sc.trim = false;
        System.out.println(sc);

        String sample = "123456\r\nabcdefghi\r\n\rASDFGHJKL:\n\r";
        StringBuilder sam = new StringBuilder();
        for (int i = 0; i < 20; i++) {
            sam.append(sample);
        }
        System.out.println(sc.simplify(sam.toString()));
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "SimplifyResponseConverter [num={0}, calcByChar={1}, calcByLine={2}, printHead={3}, printBottom={4}, notTrim={5}, omitReplacerBegin={6}, omitReplacerEnd={7}, lineFeedReplacer={8}]",
                num, calcByChar, calcByLine, printHead, printBottom, trim, omitReplacerBegin, omitReplacerEnd, lineFeedReplacer);
    }

    @Override
    public String convert(IAccessEvent ae) {
        return simplify(ae.getResponseContent());
    }
}
