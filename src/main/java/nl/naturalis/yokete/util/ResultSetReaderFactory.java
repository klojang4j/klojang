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
  private final Map<Integer, Tuple<MethodHandle, Class<?>>> methodHandleLookup;

  private ResultSetReaderFactory() {
    Map<Integer, Tuple<MethodHandle, Class<?>>> tmp = new HashMap<>();
    try {
      MethodType mt = MethodType.methodType(String.class, String.class);
      MethodHandle mh = lookup().findVirtual(ResultSet.class, "getString", mt);
      tmp.put(Types.VARCHAR, only(mh));
      tmp.put(Types.LONGVARCHAR, only(mh));
      tmp.put(Types.NVARCHAR, only(mh));
      tmp.put(Types.LONGNVARCHAR, only(mh));
      tmp.put(Types.CHAR, only(mh));
      tmp.put(Types.CLOB, only(mh));

      mt = MethodType.methodType(int.class, String.class);
      mh = lookup().findVirtual(ResultSet.class, "getInt", mt);
      tmp.put(Types.INTEGER, only(mh));
      tmp.put(Types.SMALLINT, only(mh)); // don't bother with short

      mt = MethodType.methodType(float.class, String.class);
      mh = lookup().findVirtual(ResultSet.class, "getFloat", mt);
      tmp.put(Types.FLOAT, only(mh));

      mt = MethodType.methodType(double.class, String.class);
      mh = lookup().findVirtual(ResultSet.class, "getDouble", mt);
      tmp.put(Types.DOUBLE, only(mh));
      tmp.put(Types.REAL, only(mh));

      mt = MethodType.methodType(long.class, String.class);
      mh = lookup().findVirtual(ResultSet.class, "getLong", mt);
      tmp.put(Types.BIGINT, only(mh));

      mt = MethodType.methodType(byte.class, int.class);
      mh = lookup().findVirtual(ResultSet.class, "getByte", mt);
      tmp.put(Types.TINYINT, only(mh));

      mt = MethodType.methodType(Object.class, String.class, Class.class);
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
    methodHandleLookup = Map.copyOf(tmp);
  }

  private static Tuple<MethodHandle, Class<?>> only(MethodHandle mh) {
    return Tuple.of(mh, null);
  }

  public ResultSetMappifier getMappifier(ResultSetMetaData rsmd) throws SQLException {
    int sz = Check.notNull(rsmd).ok().getColumnCount();
    Map<String, Tuple<MethodHandle, Class<?>>> invokers = new HashMap<>(sz);
    for (int idx = 0; idx < sz; ++idx) {
      Integer sqlType = Integer.valueOf(rsmd.getColumnType(idx));
      Tuple<MethodHandle, Class<?>> tuple = methodHandleLookup.get(sqlType);
      if (tuple != null) {
        String label = rsmd.getColumnLabel(idx);
        invokers.put(label, tuple);
      } else {
        Check.fail("Unsupported data type: %s", rsmd.getColumnTypeName(idx));
      }
    }
    return new ResultSetMappifier(Map.copyOf(invokers));
  }
}
