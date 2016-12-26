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

package cn.wantedonline.puppy.jdbc.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 用于辅助展开列表型参数，拼接SQL的辅助工具
 *
 * @since V0.5.0
 * @author thunder
 */
public class ListExpandUtil {

    private static List<String> formats = new ArrayList<String>();
    private static String _maxArgsLenFormat = "";

    /**
     * 生成指定数量的?符号，用,分隔（带缓存）
     * 
     * @param num 生成的数量
     */
    private static String getFormat(int argsLen) {
        if (argsLen == 0) {// 当参数个数为0时IN语句生成的SQL是IN()会运行出错，抛出错误来以提醒程序猿处理空集情况
            return "";
        }
        int index = argsLen - 1;
        if (formats.size() >= argsLen) {
            return formats.get(index);
        }
        synchronized (formats) {
            if (formats.size() < argsLen) {
                StringBuilder tmp = new StringBuilder(_maxArgsLenFormat);
                for (int i = formats.size(); i < argsLen; i++) {
                    formats.add(tmp.append("?").toString());
                    tmp.append(", ");
                }
                _maxArgsLenFormat = tmp.toString();
            }
            return formats.get(index);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, int[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, long[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, char[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, boolean[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, byte[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, float[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, double[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, short[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, Object[] list) {
        sql.append(getFormat(list.length));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, Collection<T> list) {
        sql.append(getFormat(list.size()));
        for (Object obj : list) {
            args.add(obj);
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     * @param adapter 等待添加的list有可能本身就是一个复杂类型，此适配器可用于将里面需要转化的值get出来
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, T[] list, ValueGetter<T> adapter) {
        sql.append(getFormat(list.length));
        for (T e : list) {
            args.add(adapter.getValue(e));
        }
    }

    /**
     * 展开列表型参数
     * 
     * @param sql 等待拼接的SQL语句
     * @param args 展开后的参数要加入到的集合
     * @param list 待展开的参数列表
     * @param adapter 等待添加的list有可能本身就是一个复杂类型，此适配器可用于将里面需要转化的值get出来
     */
    public static <T> void expand(StringBuilder sql, Collection<Object> args, Collection<T> list, ValueGetter<T> adapter) {
        sql.append(getFormat(list.size()));
        for (T e : list) {
            args.add(adapter.getValue(e));
        }
    }
}
