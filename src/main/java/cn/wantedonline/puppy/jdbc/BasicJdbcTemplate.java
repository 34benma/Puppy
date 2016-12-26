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

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import javax.sql.DataSource;

import cn.wantedonline.puppy.jdbc.exception.DataAccessException;
import cn.wantedonline.puppy.jdbc.exception.InvalidQueryForObjectException;
import cn.wantedonline.puppy.jdbc.util.*;
import cn.wantedonline.puppy.util.DateStringUtil;
import cn.wantedonline.puppy.util.Log;
import cn.wantedonline.puppy.util.concurrent.ConcurrentUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * @author ling
 * @author zengdong
 * @author hujiachao
 */
public class BasicJdbcTemplate implements BasicJdbcOperations, JdbcQuerierExtend {

    public abstract class InnerTemplate<T> {

        protected Connection conn;
        protected PreparedStatement pst;
        protected T result;
        protected ResultSet rs;
        protected Statement st;

        public InnerTemplate<T> execute() throws DataAccessException {
            try {
                conn = getConnection();
                initResult();
                this.process();
            } catch (Throwable e) {
                handleException(e);
                throw new InvalidQueryForObjectException("JdbcTemplate execute is fail!", e);
            } finally {
                JdbcUtils.release(rs);
                rs = null;
                JdbcUtils.release(st);
                st = null;
                JdbcUtils.release(pst);
                pst = null;
                releaseConnection(conn);
                conn = null;
            }
            return this;
        }

        public T getResult() {
            return result;
        }

        protected void handleException(Throwable e) {
        }

        protected void initResult() {
        }

        public abstract void process() throws SQLException;
    }

    private static Map<QueryKey, Map<Field, ResultSetGetter>> declaredGetterCache = new ConcurrentHashMap<QueryKey, Map<Field, ResultSetGetter>>();
    // private static final Map<QueryKey, Reference<Map<Field, ResultSetGetter>>> declaredGetterCache = Collections.synchronizedMap(new WeakHashMap<QueryKey, Reference<Map<Field,
    // ResultSetGetter>>>());//使用weakHashMap直接会把QueryKey垃圾回收
    public static final boolean springTransactionalEnable = cn.wantedonline.puppy.util.ReflectConvention.isClassFound("org.springframework.transaction.support.TransactionSynchronizationManager");

    private Map<Field, ResultSetGetter> getDeclaredGetterMap(Class<?> clazz, String[] args, boolean isInc) {// 这里不考虑同步问题,不考虑cache容量问题
        QueryKey queryKey = new QueryKey(clazz, args, isInc);
        Map<Field, ResultSetGetter> result = declaredGetterCache.get(queryKey);
        if (result != null) {
            // System.err.println("FOUND " + queryKey);
            return result;
        }

        Field[] fileds = clazz.getDeclaredFields();
        Map<Field, ResultSetGetter> resultSetGetterMap = new HashMap<Field, ResultSetGetter>();
        for (Field field : fileds) {
            if (!SimpleFieldsIterator.ignore(field, isInc, args)) {
                ResultSetGetter rsfg = JdbcUtils.getResultSetGetter(field.getType(), latin1Compatible);
                if (rsfg == null) { // 找不到说明不是simpleClass
                    continue;
                }
                resultSetGetterMap.put(field, rsfg);
            }
        }
        // System.err.println("CACHE " + queryKey);
        declaredGetterCache.put(queryKey, resultSetGetterMap);
        return resultSetGetterMap;
    }

    protected DataSource dataSource;
    private Logger log = Log.getLogger();
    private boolean printDSWhenFail = false;
    protected boolean springTransactional;
    private Logger typematcher_log = LoggerFactory.getLogger("com.xunlei.jdbc.typematcher");

    // 为了解决latin1的字符集问题，有几种解决办法，都不完美
    // 1.http://blog.csdn.net/workwithwebis3w/article/details/5875314
    // 修改CharsetMapping 中getJavaEncodingForMysqlEncoding return "Cp1252"; 改成 return "gbk";
    // 优点：查询语句都ok，但是修改语句不支持，另外不能扩大字符集到 gb18030

    // 优化：增加以下语句，这样就可以扩大字符集（原因是要把gb18030放到CharsetMapping的配置中，配置成 双字节的字符）
    // tempNumBytesMap.put("gb18030", Constants.integerValueOf(2));
    // 273行 + "GB18030 =         gb18030,"
    // 这样就查询而言是比较完美的

    // 2.修改ResultSetRow的 protected String getString(String encoding, MySQLConnection conn, byte[] value, int offset, int length) throws SQLException {
    // 增加以下代码
    // if ("Cp1252".equals(encoding)) {
    // try {
    // return new String(value, offset, length, "GB18030");
    // } catch (UnsupportedEncodingException e) {
    // }
    // }
    // 优点：查询语句ok，字符集可以扩大到gb18030,但是修改语句不支持，而且判断语句有点山寨

    // 3. 修改jdbcTemplate增加latin1Compatible
    // 优点： 使用prepareStatement的修改语句都可控制没有问题，但是statement的还是有问题，查询语句有部分不支持
    // 也就是不支持 如batchUpdate[使用statement情况] ,不支持queryForListMap
    private boolean latin1Compatible;

    public boolean isLatin1Compatible() {
        return latin1Compatible;
    }

    public void setLatin1Compatible(boolean latin1Compatible) {
        this.latin1Compatible = latin1Compatible;
    }

    public BasicJdbcTemplate(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public int[] batchUpdate(final String... sql) throws DataAccessException {
        if (latin1Compatible) {
            throw new InvalidQueryForObjectException("latin1Compatible not support batchUpate by Statement");
        }
        long beginTime = System.currentTimeMillis();
        if (sql.length == 0) {
            return null;
        }
        String info = null;
        if (log.isDebugEnabled()) {
            StringBuilder tmp = new StringBuilder(sql.length);
            tmp.append(" sql execute:\n");
            for (String s : sql) {
                tmp.append("\t").append(s).append("\n");
            }
            tmp.deleteCharAt(tmp.length() - 1);
            info = tmp.toString();
            log.debug(info);
        }
        Connection conn = null;
        int[] result = null;
        Statement st = null;
        boolean isActualTransactionActive = isActualTransactionActive();
        try {
            conn = getConnection();
            if (!isActualTransactionActive) {
                conn.setAutoCommit(false);
            }
            try {
                st = conn.createStatement();
                for (String s : sql) {
                    st.addBatch(s);
                }
                result = st.executeBatch();
            } finally {
                if (!isActualTransactionActive) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            if (info == null) {
                StringBuilder tmp = new StringBuilder(sql.length);
                tmp.append(" sql execute:\n");
                for (String s : sql) {
                    tmp.append("\t").append(s).append("\n");
                }
                tmp.deleteCharAt(tmp.length() - 1);
                info = tmp.toString();
            }
            StringBuilder tmp = new StringBuilder("Jdbc Fail:").append(info);
            if (printDSWhenFail) {
                tmp.append("\n\t").append(getDataSource());
            }
            throw new InvalidQueryForObjectException(tmp.toString(), e);
        } finally {
            JdbcUtils.release(st);
            releaseConnection(conn);
        }
        return result;
    }

    @Override
    public int[] batchUpdate(final String sql, final List<Object[]> args) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            StringBuilder tmp = new StringBuilder(args.size());
            tmp.append(" sql execute:\n");
            for (int i = 0; i < args.size(); i++) {
                tmp.append("\t").append(DebugUtil.merge(sql, args.get(i))).append("\n");
            }
            tmp.deleteCharAt(tmp.length() - 1);
            info = tmp.toString();
            log.debug(info);
        }
        Connection conn = null;
        PreparedStatement pst = null;
        int[] result = null;
        ResultSet rs = null;
        boolean isActualTransactionActive = isActualTransactionActive();
        try {
            conn = getConnection();
            if (!isActualTransactionActive) {
                conn.setAutoCommit(false);
            }
            try {
                pst = conn.prepareStatement(sql);
                for (Object[] e : args) {
                    for (int i = 0; i < e.length; i++) {
                        // pst.setObject(i + 1, e[i]);
                        JdbcUtils.setPreparedStatementArg(pst, i + 1, e[i], isActualTransactionActive);
                        // latin1
                    }
                    pst.addBatch();
                }
                result = pst.executeBatch();
            } finally {
                if (!isActualTransactionActive) {
                    conn.commit();
                }
            }
        } catch (SQLException e) {
            if (info == null) {
                StringBuilder tmp = new StringBuilder(args.size());
                tmp.append(" sql execute:\n");
                for (int i = 0; i < args.size(); i++) {
                    tmp.append("\t").append(DebugUtil.merge(sql, args.get(i))).append("\n");
                }
                tmp.deleteCharAt(tmp.length() - 1);
                info = tmp.toString();
            }
            StringBuilder tmp = new StringBuilder("Jdbc Fail:").append(info);
            if (printDSWhenFail) {
                tmp.append("\n\t").append(getDataSource());
            }
            throw new InvalidQueryForObjectException(tmp.toString(), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    @Override
    public void execute(final String sql, final Object... objs) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            pst.execute();
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
    }

    private static final Logger statlog = Log.getLoggerWithPrefix("stat");

    private int maxGetConnectionsTimes = 5;

    public Connection getConnection() throws SQLException {
        // 为了尽量减少拿不到连接的情况，这里如果拿不到连接就尝试重新拿
        // 如果还是拿不到那就抛出错误吧
        SQLException ex1 = null;
        CannotGetJdbcConnectionException ex2 = null;
        for (int i = 0; i < maxGetConnectionsTimes; i++) {
            try {
                return _getConnection();
            } catch (SQLException e) {
                ex1 = e;
                statlog.debug("{} JDBC第{}次getConnection()失败 {}", DateStringUtil.DEFAULT_DAY.now(), i + 1, e.getClass().getSimpleName());
                ConcurrentUtil.threadSleep(1500);
            } catch (CannotGetJdbcConnectionException e) {
                ex2 = e;
                statlog.debug("{} JDBC第{}次getConnection()失败 {}", DateStringUtil.DEFAULT_DAY.now(), i + 1, e.getClass().getSimpleName());
                ConcurrentUtil.threadSleep(1500);
            }
        }
        if (null != ex1) {
            throw ex1;
        }
        if (null != ex2) {
            throw ex2;
        }
        return null;
    }

    private Connection _getConnection() throws SQLException {
        if (springTransactional) {
            return DataSourceUtils.getConnection(getDataSource());
        }
        return getDataSource().getConnection();
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public String getFailMsg(String sql) {
        return getFailMsg(null, sql, null);
    }

    public String getFailMsg(String mergedInfo, String sql, Object[] objs) {
        return getFailMsg(mergedInfo, sql, objs, null, false);
    }

    public String getFailMsg(String mergedInfo, String sql, Object[] objs, Class<?> clazz, boolean isInclude, String... args) {
        String finalSQL = mergedInfo;
        if (finalSQL == null) {
            finalSQL = DebugUtil.merge(sql, objs);
        }
        StringBuilder tmp = new StringBuilder("Jdbc Fail:").append(finalSQL);
        if (clazz != null) {
            tmp.append("\t").append(clazz.getSimpleName());
            if (args != null && args.length > 0) {
                tmp.append("(").append(isInclude ? "include " : "exclude ").append(Arrays.toString(args)).append(")");
            }
        }
        if (printDSWhenFail) {
            tmp.append("\n\t").append(getDataSource());
        }
        return tmp.toString();
    }

    public Logger getLog() {
        return log;
    }

    public Logger getTypematcher_log() {
        return typematcher_log;
    }

    @Override
    public int insertAndReturnUpdateCount(String sql, Collection<Object> args) throws DataAccessException {
        return insertAndReturnUpdateCount(sql, args.toArray());
    }

    /**
     * 执行插入语句，并返回更新条数
     */
    @Override
    public int insertAndReturnUpdateCount(final String sql, final Object... objs) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        int result = -1;
        ResultSet rs = null;
        PreparedStatement pst = null;
        Statement st = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            pst.execute();
            result = pst.getUpdateCount();
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            JdbcUtils.release(st);
            releaseConnection(conn);
        }
        return result;
    }

    /**
     * 执行插入语句，并返回自增字段
     */
    @Override
    public long insert(final String sql, final Object... objs) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        long result = -1L;
        ResultSet rs = null;
        PreparedStatement pst = null;
        Statement st = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            pst.execute();
            rs = pst.getGeneratedKeys();
            if (rs.next()) {
                result = rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            JdbcUtils.release(st);
            releaseConnection(conn);
        }
        return result;
    }

    /**
     * 执行插入语句，并返回自增字段，如果有duplicate key就不插入，直接返回-1
     */
    public long insertIgnoreDuplicate(final String sql, final Object... objs) throws DataAccessException {
        try {
            return insert(sql, objs);
        } catch (DataAccessException e) {
            return -1;
        }
    }

    public boolean isActualTransactionActive() {
        return springTransactional && TransactionSynchronizationManager.isActualTransactionActive();
    }

    public boolean isPrintDSWhenFail() {
        return printDSWhenFail;
    }

    public boolean isSpringTransactional() {
        return springTransactional;
    }

    @Override
    @Deprecated
    /**
     * 此方法不建议在外部调用!
     */
    public void query(final String sql, final RowCallbackHandler callback, Object... objs) {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            rs = pst.executeQuery();
            while (rs.next()) {
                callback.processRow(rs);
            }
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
    }

    @Override
    public double queryForDouble(String sql, Object... objs) throws DataAccessException {
        Object r = this.queryOneResult(Object.class, sql, objs);
        if (r != null && !r.getClass().isAssignableFrom(Double.class)) {
            typematcher_log.warn("queryOneResult return a " + r.getClass() + " result,but u want to get Double");
        }
        return r == null ? -1 : Double.valueOf(r.toString());
    }

    @Override
    public float queryForFloat(final String sql, final Object... objs) throws DataAccessException {
        Object r = this.queryOneResult(Object.class, sql, objs);
        if (r != null && !r.getClass().isAssignableFrom(Float.class)) {
            typematcher_log.warn("queryOneResult return a " + r.getClass() + " result,but u want to get Float");
        }
        return r == null ? -1 : Float.valueOf(r.toString());
    }

    @Override
    public int queryForInt(final String sql, final Object... objs) throws DataAccessException {
        Object r = this.queryOneResult(Object.class, sql, objs);
        if (r != null && !r.getClass().isAssignableFrom(Integer.class)) {
            typematcher_log.warn("queryOneResult return a " + r.getClass() + " result,but u want to get Integer");
        }
        return r == null ? -1 : Integer.valueOf(r.toString());
    }

    @Override
    public <T> List<T> queryForList(Class<T> clazz, String sql, Object... objs) throws DataAccessException {
        return queryForList(clazz, sql, objs, null, false);
    }

    /** 上次刷新表的时间，刷表要求间隔时间至少10min */
    public static long lastFlushTableTime = 0;

    @SuppressWarnings({
        "unchecked",
        "resource"
    })
    private <T> List<T> queryForList(final Class<T> clazz, final String sql, final Object[] objs, final String[] args, boolean isInclude) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        boolean isInc = isInclude;
        if (isInclude) {
            if (args == null || args.length == 0) {
                isInc = false;
            }
        }
        String info = null;
        if (log.isDebugEnabled()) {
            StringBuilder tmp = new StringBuilder();
            tmp.append(DebugUtil.merge(sql, objs));
            tmp.append("\t").append(clazz.getSimpleName());
            if (args != null && args.length > 0) {
                tmp.append("(").append(isInc ? "include " : "exclude ").append(Arrays.toString(args)).append(")");
            }
            info = tmp.toString();
            log.debug(info);
        }
        Connection conn = null;
        List<T> result = new ArrayList<T>(6);
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            rs = pst.executeQuery();

            ResultSetGetter rsg = JdbcUtils.getResultSetGetter(clazz, latin1Compatible);
            if (rsg != null) {
                while (rs.next()) {
                    result.add((T) rsg.get(rs));
                    // latin1
                }
                return result;
            }
            Map<Field, ResultSetGetter> resultSetGetterMap = getDeclaredGetterMap(clazz, args, isInc);
            while (rs.next()) {
                T indata;
                String fname = "";
                try {
                    indata = clazz.newInstance();
                    for (Map.Entry<Field, ResultSetGetter> entry : resultSetGetterMap.entrySet()) {
                        Field field = entry.getKey();
                        fname = field.getName();
                        setValue(clazz, field, indata, entry.getValue().get(rs, field));
                    }
                    result.add(indata);
                } catch (Exception ex) {
                    throw new InvalidQueryForObjectException("query list error (field: " + fname + ")." + getFailMsg(info, sql, objs, clazz, isInc, args), ex);
                }
            }
        } catch (SQLException e) {
            // 由于数据中心拷贝数据表过来时，有时候会因为各种原因木有flush table导致坏表
            // 这里如果报表坏了，就自动flush tables
            String msg = e.getMessage();
            if (null != msg && (msg.contains("is marked as crashed and should be repaired") || msg.contains("Can't find file"))) {
                synchronized (BasicJdbcTemplate.class) {
                    if (System.currentTimeMillis() - lastFlushTableTime > 10 * 60 * 1000) {
                        lastFlushTableTime = System.currentTimeMillis();
                        if (null != conn) {
                            JdbcUtils.release(pst);
                            try {
                                pst = conn.prepareStatement("FLUSH TABLES");
                                pst.execute();
                            } catch (SQLException ex) {
                                throw new InvalidQueryForObjectException("FLUSH TABLES FAILED !", ex);
                            }
                        }
                    }
                }
            }
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs, clazz, isInc, args), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    @Override
    public <T> List<T> queryForListExclude(Class<T> clazz, String sql, Object[] objs, String... excludeFields) throws DataAccessException {
        return queryForList(clazz, sql, objs, excludeFields, false);
    }

    @Override
    public <T> List<T> queryForListExclude(Class<T> clazz, String sql, String... excludeFields) throws DataAccessException {
        return queryForList(clazz, sql, null, excludeFields, false);
    }

    @Override
    public <T> List<T> queryForListInclude(Class<T> clazz, String sql, Object[] objs, String... includeFields) throws DataAccessException {
        return queryForList(clazz, sql, objs, includeFields, true);
    }

    @Override
    public <T> List<T> queryForListInclude(Class<T> clazz, String sql, String... includeFields) throws DataAccessException {
        return queryForList(clazz, sql, null, includeFields, true);
    }

    /**
     * 执行查询，返回一个Map的列表（字段名称大小写和原来保持一致）
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    public List<Map<String, Object>> queryForListMapOriginalCase(String sql, Object... objs) {
        return queryForListMap(sql, false, objs);
    }

    /**
     * 执行查询，返回一个Map的列表（map的字段全部转为小写）
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    @Override
    public List<Map<String, Object>> queryForListMap(String sql, Object... objs) {
        return queryForListMap(sql, true, objs);
    }

    /**
     * 执行查询，返回一个Map的列表(字段有序)
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    private List<Map<String, Object>> queryForListMap(String sql, boolean toLowerCase, Object... objs) {
        if (latin1Compatible) {
            throw new InvalidQueryForObjectException("latin1Compatible not support queryForListMap by Statement");
        }
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            StringBuilder tmp = new StringBuilder();
            tmp.append(DebugUtil.merge(sql, objs));
            info = tmp.toString();
            log.debug(info);
        }
        Connection conn = null;
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            rs = pst.executeQuery();
            while (rs.next()) {
                ResultSetMetaData rsm = rs.getMetaData();
                int count = rsm.getColumnCount();
                Map<String, Object> ins = new LinkedHashMap<String, Object>(count);
                for (int i = 1; i <= count; i++) {
                    // 字段名统一使用小写
                    String columnName = toLowerCase ? rsm.getColumnLabel(i).toLowerCase() : rsm.getColumnLabel(i);
                    ins.put(columnName, rs.getObject(i)); // latin1
                }
                result.add(ins);
            }
        } catch (Exception e) {
            log.error("failed to execute query: {}, {}", info, e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    /**
     * 执行查询，返回一个封装Map的列表（map的字段保持原样）
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    public List<DbMap> queryForListDbMap(String sql, Object... objs) {
        return queryForListDbMap(sql, false, objs);
    }

    /**
     * 执行查询，返回一个Map的列表(字段有序)
     * 
     * @return 如果查询出错，或者没有查到任何东西，返回null
     */
    private List<DbMap> queryForListDbMap(String sql, boolean toLowerCase, Object... objs) {
        if (latin1Compatible) {
            throw new InvalidQueryForObjectException("latin1Compatible not support queryForListMap by Statement");
        }
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            StringBuilder tmp = new StringBuilder();
            tmp.append(DebugUtil.merge(sql, objs));
            info = tmp.toString();
            log.debug(info);
        }
        Connection conn = null;
        List<DbMap> result = new ArrayList<DbMap>();
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            rs = pst.executeQuery();
            while (rs.next()) {
                ResultSetMetaData rsm = rs.getMetaData();
                int count = rsm.getColumnCount();
                DbMap ins = new DbMap();
                for (int i = 1; i <= count; i++) {
                    // 字段名统一使用小写
                    String columnName = toLowerCase ? rsm.getColumnLabel(i).toLowerCase() : rsm.getColumnLabel(i);
                    ins.put(columnName, rs.getObject(i)); // latin1
                }
                result.add(ins);
            }
        } catch (Exception e) {
            log.error("failed to execute query: {}, {}", info, e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    @Override
    public long queryForLong(final String sql, final Object... objs) throws DataAccessException {
        Object r = this.queryOneResult(Object.class, sql, objs);
        if (r != null && !r.getClass().isAssignableFrom(Long.class)) {
            typematcher_log.warn("queryOneResult return a " + r.getClass() + " result,but u want to get Long");
        }
        return r == null ? -1 : Long.valueOf(r.toString());
    }

    /**
     * 执行查询，返回一个Map（map的字段全部转为小写）
     */
    @Override
    public Map<String, Object> queryForMap(String sql, Object... objs) {
        List<Map<String, Object>> result = queryForListMap(sql, true, objs);
        if (result.size() > 0) {
            return result.get(0);
        }
        return Collections.emptyMap();
    }

    /**
     * 执行查询，返回一个Map（字段名称大小写和原来保持一致）
     */
    public Map<String, Object> queryForMapOriginalCase(String sql, Object... objs) {
        List<Map<String, Object>> result = queryForListMap(sql, false, objs);
        if (result.size() > 0) {
            return result.get(0);
        }
        return Collections.emptyMap();
    }

    /**
     * 执行查询，返回一个封装Map（map的字段全部保持跟数据库一致）
     */
    public DbMap queryForDbMap(String sql, Object... objs) {
        List<DbMap> result = queryForListDbMap(sql, false, objs);
        if (result.size() > 0) {
            return result.get(0);
        }
        return new DbMap();
    }

    @Override
    public <T> T queryForObject(Class<T> clazz, String sql, Object... objs) throws DataAccessException {
        return queryForObject(clazz, sql, objs, null, false);
    }

    private <T> T queryForObject(final Class<T> clazz, final String sql, final Object[] objs, final String[] args, boolean isInclude) throws DataAccessException {
        List<T> list = this.queryForList(clazz, sql, objs, args, isInclude);
        if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() == 0) {
            return null;
        } else {
            StringBuilder sb = new StringBuilder("found multi result\t");
            sb.append(DebugUtil.merge(sql, objs));
            throw new InvalidQueryForObjectException(sb.toString());
        }
    }

    @Override
    public <T> T queryForObjectExclude(Class<T> clazz, String sql, Object[] objs, String... excludeFields) throws DataAccessException {
        return queryForObject(clazz, sql, objs, excludeFields, false);
    }

    @Override
    public <T> T queryForObjectExclude(Class<T> clazz, String sql, String... excludeFields) throws DataAccessException {
        return queryForObject(clazz, sql, null, excludeFields, false);
    }

    @Override
    public <T> T queryForObjectInclude(Class<T> clazz, String sql, Object[] objs, String... includeFields) throws DataAccessException {
        return queryForObject(clazz, sql, objs, includeFields, true);
    }

    @Override
    public <T> T queryForObjectInclude(Class<T> clazz, String sql, String... includeFields) throws DataAccessException {
        return queryForObject(clazz, sql, null, includeFields, true);
    }

    @Override
    public String queryForString(final String sql, Object... objs) throws DataAccessException {
        return this.queryOneResult(String.class, sql, objs);
    }

    private <T> T queryOneResult(Class<T> clazz, final String sql, final Object... objs) {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        T result = null;
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            rs = pst.executeQuery();
            if (rs.next()) {
                result = JdbcUtils.getObject(rs, clazz, latin1Compatible);// latin1
            }
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    public void releaseConnection(Connection conn) {
        if (springTransactional) {
            DataSourceUtils.releaseConnection(conn, getDataSource());
        } else {
            JdbcUtils.release(conn);
        }
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void setLog(Logger log) {
        this.log = log;
    }

    public void setPrintDSWhenFail(boolean printDSWhenFail) {
        this.printDSWhenFail = printDSWhenFail;
    }

    public void setSpringTransactional(boolean springTransactional) {
        this.springTransactional = springTransactionalEnable && springTransactional;
    }

    public void setTypematcher_log(Logger typematcher_log) {
        this.typematcher_log = typematcher_log;
    }

    private void setValue(Class<?> clazz, Field field, Object indata, Object value) throws Exception {
        // 不找getter方法
        field.setAccessible(true);
        field.set(indata, value);
    }

    @Override
    public int update(final String sql, final Object... objs) throws DataAccessException {
        long beginTime = System.currentTimeMillis();
        String info = null;
        if (log.isDebugEnabled()) {
            info = DebugUtil.merge(sql, objs);
            log.debug(info);
        }
        Connection conn = null;
        int result = 0;
        ResultSet rs = null;
        PreparedStatement pst = null;
        try {
            conn = getConnection();
            pst = conn.prepareStatement(sql);
            JdbcUtils.setPreparedStatementArgs(pst, objs, latin1Compatible);
            result = pst.executeUpdate();
        } catch (SQLException e) {
            throw new InvalidQueryForObjectException(getFailMsg(info, sql, objs), e);
        } finally {
            JdbcUtils.release(rs);
            JdbcUtils.release(pst);
            releaseConnection(conn);
        }
        return result;
    }

    @Override
    public void execute(String sql, Collection<Object> args) throws DataAccessException {
        execute(sql, args.toArray());
    }

    @Override
    public long insert(String sql, Collection<Object> args) throws DataAccessException {
        return insert(sql, args.toArray());
    }

    /**
     * 执行插入语句，并返回自增字段，如果有duplicate key就不插入，直接返回-1
     */
    public long insertIgnoreDuplicate(String sql, Collection<Object> args) throws DataAccessException {
        return insertIgnoreDuplicate(sql, args.toArray());
    }

    @Override
    @Deprecated
    public void query(String sql, RowCallbackHandler callback, Collection<Object> args) {
        query(sql, callback, args.toArray());
    }

    @Override
    public double queryForDouble(String sql, Collection<Object> args) throws DataAccessException {
        return queryForDouble(sql, args.toArray());
    }

    @Override
    public float queryForFloat(String sql, Collection<Object> args) throws DataAccessException {
        return queryForFloat(sql, args.toArray());
    }

    @Override
    public int queryForInt(String sql, Collection<Object> args) throws DataAccessException {
        return queryForInt(sql, args.toArray());
    }

    @Override
    public <T> List<T> queryForList(Class<T> clazz, String sql, Collection<Object> args) throws DataAccessException {
        return queryForList(clazz, sql, args.toArray());
    }

    @Override
    public long queryForLong(String sql, Collection<Object> args) throws DataAccessException {
        return queryForLong(sql, args.toArray());
    }

    @Override
    public <T> T queryForObject(Class<T> clazz, String sql, Collection<Object> args) throws DataAccessException {
        return queryForObject(clazz, sql, args.toArray());
    }

    @Override
    public String queryForString(String sql, Collection<Object> args) throws DataAccessException {
        return queryForString(sql, args.toArray());
    }

    @Override
    public int update(String sql, Collection<Object> args) throws DataAccessException {
        return update(sql, args.toArray());
    }

    public List<Map<String, Object>> queryForListMapOriginalCase(String sql, Collection<Object> args) {
        return queryForListMap(sql, false, args.toArray());
    }

    @Override
    public List<Map<String, Object>> queryForListMap(String sql, Collection<Object> args) {
        return queryForListMap(sql, true, args.toArray());
    }

    @Override
    public Map<String, Object> queryForMap(String sql, Collection<Object> args) {
        return queryForMap(sql, args.toArray());
    }

    public Map<String, Object> queryForMapOriginalCase(String sql, Collection<Object> args) {
        return queryForMapOriginalCase(sql, args.toArray());
    }

    public List<DbMap> queryForListDbMap(String sql, Collection<Object> args) {
        return queryForListDbMap(sql, true, args.toArray());
    }

    @Override
    public List<Process> showProcessList() {
        return queryForList(Process.class, "show processlist");
    }
}
