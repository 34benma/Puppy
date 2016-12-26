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
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.sql.DataSource;

import cn.wantedonline.puppy.jdbc.exception.InvalidSetPreparedStatementParamException;
import cn.wantedonline.puppy.util.CharsetTools;
import cn.wantedonline.puppy.util.StringTools;

/**
 *
 * @since V0.5.0
 * @author thunder
 */
public abstract class JdbcUtils {

    public static DataSource DATASOURCE;

    private static final Map<Class<?>, ResultSetGetter> simpleClazz = initSimpleClazz();

    public static String getLogCollectionMsg(String msgtitle, Collection<?> c) {
        String title = msgtitle;
        if (StringTools.isEmpty(title)) {
            title = "getLogCollectionMsg";
        }
        StringBuilder msg = new StringBuilder(title);
        msg.append("[").append(c.size()).append("]");
        for (Object e : c) {
            msg.append("\n\t");
            msg.append(StringTools4Bean.listingObject(e));
        }
        return msg.toString();
    }

    public static String getLogMapMsg(String msgtitle, Map<?, ?> c) {
        String title = msgtitle;
        if (StringTools.isEmpty(title)) {
            title = "getLogMapMsg";
        }
        StringBuilder msg = new StringBuilder(title);
        msg.append("[").append(c.size()).append("]");
        for (Object e : c.keySet()) {
            Object value = c.get(e);
            msg.append("\n\t").append(StringTools4Bean.listingObject(e)).append("\n\t\t").append(StringTools4Bean.listingObject(value));
        }
        return msg.toString();
    }

    // 請茬佌輸扖鈥★魰<请在此输入火星文>
    // gb2312 不能完全显示，gbk是ok的，gb1
    public static final Charset latin1CompatibleCharset = CharsetTools.GB18030;

    @SuppressWarnings("unchecked")
    public static final <T> T getObject(ResultSet rs, Class<T> clazz, boolean latin1Compatible) throws SQLException {
        if (latin1Compatible && String.class.equals(clazz)) {
            return (T) new String(rs.getBytes(1), latin1CompatibleCharset);
        }
        return (T) rs.getObject(1); // latin1
    }

    private static ResultSetGetter latin1CompatibleStringGetter = new ResultSetGetter() {

        @Override
        public Object get(ResultSet rs) throws SQLException {
            byte[] bytes = rs.getBytes(1);
            return null != bytes ? new String(rs.getBytes(1), latin1CompatibleCharset) : "";
        }

        @Override
        public Object get(ResultSet rs, Field field) throws SQLException {
            byte[] bytes = rs.getBytes(field.getName());
            return null != bytes ? new String(bytes, latin1CompatibleCharset) : "";
        }
    };

    public static ResultSetGetter getResultSetGetter(Class<?> clazz, boolean latin1Compatible) {
        if (latin1Compatible && String.class.equals(clazz)) {
            return latin1CompatibleStringGetter;
        }
        return simpleClazz.get(clazz);
    }

    private static Map<Class<?>, ResultSetGetter> initSimpleClazz() {
        Map<Class<?>, ResultSetGetter> simpleClz = new HashMap<Class<?>, ResultSetGetter>(13);
        ResultSetGetter rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getString(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getString(field.getName());
            }
        };
        simpleClz.put(String.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getInt(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getInt(field.getName());
            }
        };
        simpleClz.put(Integer.class, rsg);
        simpleClz.put(int.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getFloat(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getFloat(field.getName());
            }
        };
        simpleClz.put(Float.class, rsg);
        simpleClz.put(float.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getLong(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                String fname = field.getName();
                RegardAs ra = field.getAnnotation(RegardAs.class);
                if (ra != null && ra.value() == RegardTypes.DATE) { // 将long型用作受理Date类型
                    return rs.getDate(fname).getTime();
                }
                return rs.getLong(fname);
            }
        };
        simpleClz.put(Long.class, rsg);
        simpleClz.put(long.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getByte(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getByte(field.getName());
            }
        };
        simpleClz.put(Byte.class, rsg);
        simpleClz.put(byte.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getShort(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getShort(field.getName());
            }
        };
        simpleClz.put(Short.class, rsg);
        simpleClz.put(short.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getDouble(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getDouble(field.getName());
            }
        };
        simpleClz.put(Double.class, rsg);
        simpleClz.put(double.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getBigDecimal(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getBigDecimal(field.getName());
            }
        };
        simpleClz.put(BigDecimal.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getBoolean(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getBoolean(field.getName());
            }
        };
        simpleClz.put(Boolean.class, rsg);
        simpleClz.put(boolean.class, rsg);
        rsg = new ResultSetGetter() {

            @Override
            public Object get(ResultSet rs) throws SQLException {
                return rs.getObject(1);
            }

            @Override
            public Object get(ResultSet rs, Field field) throws SQLException {
                return rs.getObject(field.getName());
            }
        };
        simpleClz.put(BigInteger.class, rsg);
        simpleClz.put(Date.class, rsg);
        simpleClz.put(Time.class, rsg);
        simpleClz.put(Timestamp.class, rsg);

        return simpleClz;
    }

    public static boolean isSimpleClass(Class<?> clazz) {
        return simpleClazz.containsKey(clazz);
    }

    // private static final Logger log = Log.getLogger();
    public static void release(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                // ex.printStackTrace();
            }
        }
    }

    public static void release(PreparedStatement ps) {
        if (ps != null) {
            try {
                ps.clearParameters();
                ps.close();
            } catch (SQLException ex) {
                // ex.printStackTrace();
            }
        }
    }

    public static void release(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException ex) {
                // ex.printStackTrace();
            }
        }
    }

    public static void release(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
            } catch (SQLException ex) {
                // ex.printStackTrace();
            }
        }
    }

    public static void setPreparedStatementArg(PreparedStatement ps, int index, Object o, boolean latin1Compatible) throws SQLException {
        if (latin1Compatible && o instanceof String) {
            byte[] bytes = o.toString().getBytes(latin1CompatibleCharset);
            ps.setObject(index, bytes);
        } else {
            ps.setObject(index, o);
        }
    }

    public static void setPreparedStatementArgs(final PreparedStatement ps, final Object[] objs, boolean latin1Compatible) {
        if (objs == null) {
            return;
        }
        try {
            int i = 0;
            for (Object o : objs) {
                i++;
                setPreparedStatementArg(ps, i, o, latin1Compatible);
            }
        } catch (SQLException e) {
            throw new InvalidSetPreparedStatementParamException("Set the param of PreparedStatement is error", e);
        }
    }
}
