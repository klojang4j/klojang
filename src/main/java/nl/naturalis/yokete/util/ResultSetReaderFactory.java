package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.Tuple;
import nl.naturalis.common.check.Check;
import static java.lang.invoke.MethodHandles.lookup;

public class ResultSetReaderFactory {

  private static ResultSetReaderFactory instance;

  public static ResultSetReaderFactory instance() {
    if (instance == null) {
      instance = new ResultSetReaderFactory();
    }
    return instance;
  }

  // Maps SQL types to the ResultSet methods, like ResultSet.getString(columnLabel);
  private final Map<Integer, Tuple<MethodHandle, Class<?>>> mhCache;

  private ResultSetReaderFactory() {
    Map<Integer, Tuple<MethodHandle, Class<?>>> tmp = new HashMap<>();
    try {

      /* String ResultSet.getString(int columnIndex) */
      MethodType mt = MethodType.methodType(String.class, int.class);
      MethodHandle mh = lookup().findVirtual(ResultSet.class, "getString", mt);
      tmp.put(Types.VARCHAR, only(mh));
      tmp.put(Types.LONGVARCHAR, only(mh));
      tmp.put(Types.NVARCHAR, only(mh));
      tmp.put(Types.LONGNVARCHAR, only(mh));
      tmp.put(Types.CHAR, only(mh));
      tmp.put(Types.CLOB, only(mh));

      /* int ResultSet.getInt(int columnIndex) */
      mt = MethodType.methodType(int.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getInt", mt);
      tmp.put(Types.INTEGER, only(mh));
      tmp.put(Types.SMALLINT, only(mh)); // don't bother with short

      /* float ResultSet.getFloat(int columnIndex) */
      mt = MethodType.methodType(float.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getFloat", mt);
      tmp.put(Types.FLOAT, only(mh));

      /* double ResultSet.getDouble(int columnIndex) */
      mt = MethodType.methodType(double.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getDouble", mt);
      tmp.put(Types.DOUBLE, only(mh));
      tmp.put(Types.REAL, only(mh));

      /* long ResultSet.getLong(int columnIndex) */
      mt = MethodType.methodType(long.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getLong", mt);
      tmp.put(Types.BIGINT, only(mh));

      /* byte ResultSet.getByte(int columnIndex) */
      mt = MethodType.methodType(byte.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getByte", mt);
      tmp.put(Types.TINYINT, only(mh));

      /* <T> T ResultSet.getObject(int columnIndex, Class<T> convertTo) */
      mt = MethodType.methodType(Object.class, int.class, Class.class);
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
      tmp.put(Types.DATE, Tuple.of(mh, LocalDate.class));
      tmp.put(Types.TIMESTAMP, Tuple.of(mh, LocalDateTime.class));
      tmp.put(Types.TIMESTAMP_WITH_TIMEZONE, Tuple.of(mh, OffsetDateTime.class));

      Tuple<MethodHandle, Class<?>> tuple = Tuple.of(mh, BigDecimal.class);
      tmp.put(Types.NUMERIC, tuple);
      tmp.put(Types.DECIMAL, tuple);

    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    mhCache = Map.copyOf(tmp);
  }

  private static Tuple<MethodHandle, Class<?>> only(MethodHandle mh) {
    return Tuple.of(mh, null);
  }

  public ResultSetMappifier getMappifier(ResultSet rs) throws SQLException {
    RsReadInfo[] infos = createResultSetReadInfo(rs);
    return new ResultSetMappifier(infos, infos.length);
  }

  public ResultSetMappifier getMappifier(ResultSet rs, int mapSize) throws SQLException {
    RsReadInfo[] infos = createResultSetReadInfo(rs);
    return new ResultSetMappifier(infos, mapSize);
  }

  private RsReadInfo[] createResultSetReadInfo(ResultSet rs) throws SQLException {
    ResultSetMetaData rsmd = Check.notNull(rs).ok().getMetaData();
    int sz = rsmd.getColumnCount();
    RsReadInfo[] infos = new RsReadInfo[sz];
    for (int idx = 0; idx < sz; ++idx) {
      int jdbcIdx = idx + 1; // JDBC is one-based!
      int sqlType = rsmd.getColumnType(jdbcIdx);
      Tuple<MethodHandle, Class<?>> tuple = mhCache.get(sqlType);
      if (tuple != null) {
        String label = rsmd.getColumnLabel(jdbcIdx);
        MethodHandle rsMethod = tuple.getLeft();
        Class<?> secondArg = tuple.getRight();
        infos[idx] = new RsReadInfo(jdbcIdx, label, sqlType, rsMethod, secondArg);
      } else {
        Check.fail("Unsupported data type: %s", rsmd.getColumnTypeName(idx));
      }
    }
    return infos;
  }
}
