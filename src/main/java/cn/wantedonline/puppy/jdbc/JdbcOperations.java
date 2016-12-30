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

package cn.wantedonline.puppy.jdbc;

import java.util.Collection;
import java.util.Map;

/**
 * <pre>
 * 此类封装了额外的高级数据库操作，其通过vo类反射其变量来构造sql语句 要特别注意，这里的默认构造规则：
 *  <LI>查询条件只支持一系列and条件</LI> 
 *  <LI>当seqid<=0时，不加入到查询条件中</LI>
 *  <LI>增删改查操作中，当字段值为null时，直接忽略此字段</LI>
 *  <LI>查询操作中，当字符型字段的字段值trim后值为""时，也忽略此字段</LI>
 *  <LI>当字段为数值型时,如果值为-1,忽略此字段</LI>
 * </pre>
 * 
 * @author thunder
 * @since V0.6.0
 */
public interface JdbcOperations extends BasicJdbcOperations {

    public <T> long countObject(T data);

    public <T> long countObjectExclude(T data, String... excludeField);

    public <T> long countObjectInclude(T data, String... includeField);

    public void deleteObject(Class<?> clazz, long... seqids);

    public void deleteObject(Object data);

    public void deleteObjectExclude(Object data, String... excludeField);

    public void deleteObjectInclude(Object data, String... includeField);

    public <T> T findObject(T data);

    public <T> T findObjectExclude(T data, String... excludeField);

    public <T> T findObjectInclude(T data, String... includeField);

    public <T> T getObject(Class<T> clazz, long seqid);

    public long insertObject(Object data);

    public long insertObjectExclude(Object data, String... excludeField);

    public long insertObjectInclude(Object data, String... includeField);

    public void insertObjects(Collection<?> datas);

    /**
     * 批量插入一组vo
     * 
     * @param datas 传入一个map,key为要存储的vo,value为存储时要排除的字段名,及对应insertObject(Object data, String... excludeFieldNames)中的两个参数
     */
    public void insertObjectsExclude(Map<Object, String[]> datas);

    public void insertObjectsInclude(Map<Object, String[]> datas);

    public void updateObject(Object data);

    public void updateObjectExclude(Object data, String... excludeField);

    public void updateObjectInclude(Object data, String... includeField);

    /**
     * 批量更新一组vo
     * 
     * @param datas 传入一个map,key为要更新的vo,value为存储时要排除的字段名,及对应updateObject(Object data, String... excludeFieldNames)中的两个参数
     */
    public void updateObjects(Collection<?> datas);

    /**
     * 批量更新一组vo
     * 
     * @param datas 传入一个map,key为要更新的vo,value为存储时要排除的字段名,及对应updateObject(Object data, String... excludeFieldNames)中的两个参数
     */
    public void updateObjectsExclude(Map<Object, String[]> datas);

    public void updateObjectsInclude(Map<Object, String[]> datas);
}
