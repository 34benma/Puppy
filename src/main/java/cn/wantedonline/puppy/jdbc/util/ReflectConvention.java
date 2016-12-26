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

import java.lang.reflect.Field;
import java.util.Date;

import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.StringTools;
import org.slf4j.Logger;

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public class ReflectConvention {

    private static final Logger log = Log.getLogger();

    /**
     * 构造sql中的where条件语句中对应的字段,现只支持and条件【 and filedname = 'demo' and filedname2 ='go',】 ,或update语句中的 set 语句【 filedname = 'demo', filedname2 ='go',】 *
     * 
     * @param data 要操作的vo
     * @param field 字段
     * @param tmp 要append的StringBuilder对象
     * @param condition 判断关系，如:=,>=,like,regexp等，为空时默认表示等于号
     * @param valueTemplate 查询值的模板,可为空
     * @param useQuestionmark 用于prepareStatement中是使用? 来表示此值，延迟赋值
     * @param useAnd 字段之间是否使用and
     * @return 如果参数useQuestionmark为假时，StringBuilder对象中添加形如 " and filedname = 'demo' " 的字符串. 如果参数useQuestionmark为真时，StringBuilder对象中添加形如 " and filedname = ? " 的字符串. 返回此字段值
     */
    private static Object _appendSBByField(Object data, Field field, StringBuilder tmp, String condition, String valueTemplate, boolean useQuestionmark, boolean useAnd) {
        String cond = condition;
        if (null == cond) {
            cond = "=";
        }
        try {
            // 获得对应值
            Object fieldValueObj = null;
            try {
                fieldValueObj = cn.wantedonline.puppy.util.ReflectConvention.getValue(data, field);
            } catch (Exception e) {
                fieldValueObj = field.get(data);
            }
            // update操作中，当字段值为空时，忽略此字段
            // query操作中，当字段为空或字符为""时，忽略此字段
            // 另query中，当为seqid字段时，因其不可能为0，
            // 所以当seqid<=0时，说明查询操作肯定是不希望包括seqid的，故忽略
            // 从另一角度也就是说，使用此方法的开发者须在调用前
            // 就判断seqid是否<=0,其小于等于0时，按理是不能查询到任何数据的
            if (null == fieldValueObj || (useAnd && fieldValueObj.toString().trim().equals(""))) {
                return null;
            }
            boolean isSeqId = "seqid".equalsIgnoreCase(field.getName());
            if (useAnd) {
                if (isSeqId && Long.valueOf(fieldValueObj.toString()) <= 0) {
                    return null;
                }
                // 增加一个判断,如果当前字段是数值型字段,且值为-1,也忽略
                if (!field.getType().equals(String.class) && JdbcUtils.isSimpleClass(field.getType()) && Long.valueOf(fieldValueObj.toString()) == -1) {
                    return null;
                }
                tmp.append(" and");
            } else {
                // update操作，直接禁止对seqid进行操作
                if (isSeqId) {
                    return null;
                }
            }
            boolean date = Date.class.isAssignableFrom(field.getType());
            boolean addQuote = field.getType() == String.class || date;
            if (!useQuestionmark && date) {
                fieldValueObj = DateStringUtil.DEFAULT.format((Date) fieldValueObj);// 09-07-03 所有时间相关类型都先转变成默认格式
            }
            tmp.append(" `").append(field.getName()).append("`").append(cond);
            if (!useQuestionmark && addQuote) {
                tmp.append("'");
            }
            String fieldValue = useQuestionmark ? "?" : fieldValueObj.toString();
            if (StringTools.isNotEmpty(valueTemplate)) {
                fieldValue = useQuestionmark ? valueTemplate.replaceAll(WhereCondition.PH, "?") : valueTemplate.replaceAll(WhereCondition.PH, fieldValue);
            }
            tmp.append(fieldValue);
            if (!useQuestionmark && addQuote) {
                tmp.append("'");
            }
            if (!useAnd) {
                tmp.append(",");
            }
            return fieldValueObj;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    public static Object appendQueryConditionByField(Object data, Field field, StringBuilder tmp, String condition, String valueTemplate, boolean useQuestionmark) {
        return _appendSBByField(data, field, tmp, condition, valueTemplate, useQuestionmark, true);
    }

    public static Object appendUpdateValueByField(Object data, Field field, StringBuilder tmp, boolean useQuestionmark) {
        return _appendSBByField(data, field, tmp, null, null, useQuestionmark, false);
    }

    public static Object appendValuesByField(Object data, Field field, StringBuilder tmp, StringBuilder tmp2, boolean useQuestionmark) {
        try {
            // 获得对应值
            Object fieldValueObj = null;
            try {
                fieldValueObj = cn.wantedonline.puppy.util.ReflectConvention.getValue(data, field);
            } catch (Exception e) {
                fieldValueObj = field.get(data);
            }
            // insert操作中，当字段值为空时，忽略此字段
            if (null == fieldValueObj) {
                return null;
            }
            boolean isSeqId = "seqid".equalsIgnoreCase(field.getName());
            if (isSeqId) {
                return null;
            }
            boolean date = Date.class.isAssignableFrom(field.getType());
            boolean addQuote = field.getType() == String.class || date;
            if (!useQuestionmark && date) {
                fieldValueObj = DateStringUtil.DEFAULT.format((Date) fieldValueObj);// 09-07-03 所有时间相关类型都先转变成默认格式
            }
            tmp.append("`").append(field.getName()).append("`,");
            if (!useQuestionmark && addQuote) {
                tmp2.append("'");
            }
            tmp2.append(useQuestionmark ? "?" : fieldValueObj.toString());
            if (!useQuestionmark && addQuote && !isSeqId) {
                tmp2.append("'");
            }
            tmp2.append(",");
            return fieldValueObj;
        } catch (Exception e) {
            log.error("", e);
        }
        return null;
    }

    /**
     * 字符串是否在数组内，不区分大小写
     * 
     * @param one
     * @param arrays
     * @return
     */
    public static boolean isNotContains(String one, String[] arrays) {
        return !isContains(one, arrays);
    }

    public static boolean isContains(String one, String[] arrays) {
        if (arrays == null) {
            return false;
        }
        for (String str : arrays) {
            if (one.equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

}
