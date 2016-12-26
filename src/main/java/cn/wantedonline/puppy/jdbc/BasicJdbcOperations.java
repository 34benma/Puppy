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

import cn.wantedonline.puppy.jdbc.exception.DataAccessException;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 一组基本的JDBC操作接口，由JdbcTemplate来实现。
 * 
 * 如果编码有问题可以在执行前调用
 * jdbcTemplate.update("set names latin1;"); 还要再确认是不是要这样：new String(content.getBytes("ISO-8859-1"),"gbk");
 * 注意调用execute不会有效果
 *
 * @author thunder
 */
public interface BasicJdbcOperations {

    /**
     * 执行批量更新语句
     * 
     * @param sql 所有sql语句
     * @return 每个更新语句更新成功的记录数
     * @throws DataAccessException 执行时出错
     */
    public int[] batchUpdate(final String... sql) throws DataAccessException;

    /**
     * 使用prepareStatement执行批量更新语句
     * 
     * @param sql sql语句
     * @param args 各个sql语句对应占位符的值
     * @return 每个更新语句更新成功的记录数
     * @throws DataAccessException 执行时出错
     */
    public int[] batchUpdate(final String sql, final List<Object[]> args) throws DataAccessException;

    /**
     * 执行一个sql语句
     * 
     * @param sql 执行的sql语句
     * @param objs sql语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @throws DataAccessException 执行时出错
     */
    public void execute(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 执行一个sql语句
     * 
     * @param sql 执行的sql语句
     * @param objs sql语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object集合
     * @throws DataAccessException 执行时出错
     */
    public void execute(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 执行插入语句，并返回更新条数
     */
    public int insertAndReturnUpdateCount(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 执行插入语句，并返回更新条数
     */
    public int insertAndReturnUpdateCount(String sql, Collection<Object> args) throws DataAccessException;

    /**
     * 执行一个插入语句
     * 
     * @param sql 执行的sql语句
     * @param objs sql语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return 自增字段的值
     * @throws DataAccessException 执行时出错
     */
    public long insert(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 执行一个插入语句
     * 
     * @param sql 执行的sql语句
     * @param objs sql语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @return 自增字段的值
     * @throws DataAccessException 执行时出错
     */
    public long insert(final String sql, final Collection<Object> args) throws DataAccessException;

    public void query(String sql, RowCallbackHandler callback, Object... objs);

    public void query(String sql, RowCallbackHandler callback, final Collection<Object> args);

    /**
     * 查询sql，返回一个双精度数值结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return double型结果
     * @throws DataAccessException 执行时出错
     */
    public double queryForDouble(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql，返回一个双精度数值结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return double型结果
     * @throws DataAccessException 执行时出错
     */
    public double queryForDouble(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 查询sql，返回一个浮点数结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return float型结果
     * @throws DataAccessException 执行时出错
     */
    public float queryForFloat(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql，返回一个浮点数结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @return float型结果
     * @throws DataAccessException 执行时出错
     */
    public float queryForFloat(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 查询sql，返回一个整数结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return int型结果
     * @throws DataAccessException 执行时出错
     */
    public int queryForInt(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql，返回一个整数结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @return int型结果
     * @throws DataAccessException 执行时出错
     */
    public int queryForInt(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 查询sql,返回一个结果集合
     * @param clazz 若为封装类，则会通过反射对其变量进行赋值，若为数值包装类，原始数据类型及String，则直接返回此类型对应的值
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @param additionSettingFileds 仅对封装类起作用， 指封装类中赋值时要增加设置的字段
     * 
     * @return 结果集合
     * @throws DataAccessException 执行时出错
     */
    public <T> List<T> queryForList(final Class<T> clazz, final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql,返回一个结果集合
     * @param clazz 若为封装类，则会通过反射对其变量进行赋值，若为数值包装类，原始数据类型及String，则直接返回此类型对应的值
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @param additionSettingFileds 仅对封装类起作用， 指封装类中赋值时要增加设置的字段
     * 
     * @return 结果集合
     * @throws DataAccessException 执行时出错
     */
    public <T> List<T> queryForList(final Class<T> clazz, final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 查询sql，返回一个长整型数值结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return long型结果
     * @throws DataAccessException 执行时出错
     */
    public long queryForLong(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql，返回一个长整型数值结果
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @return long型结果
     * @throws DataAccessException 执行时出错
     */
    public long queryForLong(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 获得一个封装类对象
     * @param clazz 封装类名
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @param additionSettingFileds 封装类中赋值时要增加设置的字段
     * 
     * @return 封装类对象
     * @throws DataAccessException 执行查询时出错
     */
    public <T> T queryForObject(Class<T> clazz, final String sql, Object... objs) throws DataAccessException;

    /**
     * 获得一个封装类对象
     * @param clazz 封装类名
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @param additionSettingFileds 封装类中赋值时要增加设置的字段
     * 
     * @return 封装类对象
     * @throws DataAccessException 执行查询时出错
     */
    public <T> T queryForObject(Class<T> clazz, final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 查询sql，返回一个字符串
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @return 字符串值
     * @throws DataAccessException 执行时出错
     */
    public String queryForString(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 查询sql，返回一个字符串
     * 
     * @param sql 查询语句
     * @param objs 查询语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @return 字符串值
     * @throws DataAccessException 执行时出错
     */
    public String queryForString(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 执行一个更新语句
     * 
     * @param sql 执行的更新语句
     * @param objs 更新语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object[]数组
     * @throws DataAccessException 执行时出错
     * @return 更新成功的记录数
     * @throws DataAccessException 执行时出错
     */
    public int update(final String sql, final Object... objs) throws DataAccessException;

    /**
     * 执行一个更新语句
     * 
     * @param sql 执行的更新语句
     * @param objs 更新语句用到的PrepareStatemnet的?占位符对应的值，按顺序组成的Object列表
     * @throws DataAccessException 执行时出错
     * @return 更新成功的记录数
     * @throws DataAccessException 执行时出错
     */
    public int update(final String sql, final Collection<Object> args) throws DataAccessException;

    /**
     * 执行查询，返回一个Map的列表
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    public List<Map<String, Object>> queryForListMap(String sql, Object... objs);

    /**
     * 执行查询，返回一个Map的列表
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    public List<Map<String, Object>> queryForListMap(String sql, final Collection<Object> args);

    /**
     * 执行查询，返回一个Map
     * 
     * @return 如果没有查到任何东西，返回null
     */
    public Map<String, Object> queryForMap(String sql, Object... objs);

    /**
     * 执行查询，返回一个Map
     * 
     * @return 如果没有查到任何东西，返回null
     */
    public Map<String, Object> queryForMap(String sql, final Collection<Object> args);

    /**
     * show processlist
     */
    public List<Process> showProcessList();
}
