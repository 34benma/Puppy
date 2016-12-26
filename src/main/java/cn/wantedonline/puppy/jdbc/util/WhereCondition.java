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

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public class WhereCondition {

    public static final String PH = "#placeholder#";
    private String condition = "=";
    // 必需
    private String fieldName;
    private String valueTemplate;

    public WhereCondition(String fieldName, String condition, String valueTemplate) {
        this.fieldName = fieldName;
        if (condition != null) {
            this.condition = condition;
        }
        this.valueTemplate = valueTemplate;
    }

    public WhereCondition(String fieldName) {
        this.fieldName = fieldName;
    }

    protected String getCondition() {
        return condition;
    }

    protected String getFieldName() {
        return fieldName;
    }

    protected String getValueTemplate() {
        return valueTemplate;
    }

    protected void setCondition(String condition) {
        this.condition = condition;
    }

    protected void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    protected void setValueTemplate(String valueTemplate) {
        this.valueTemplate = valueTemplate;
    }
}
