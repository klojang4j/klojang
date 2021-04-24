package nl.naturalis.yokete.db;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static java.lang.invoke.MethodHandles.lookup;
import static java.sql.Types.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;

class ColumnReaders {

  private static ColumnReaders INSTANCE;

  static ColumnReaders getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaders();
    }
    return INSTANCE;
  }

  static final ColumnReader<String> GET_STRING = newReader("getString", String.class);
  static final ColumnReader<Integer> GET_INT = newReader("getInt", int.class);
  static final ColumnReader<Short> GET_SHORT = newReader("getShort", short.class);
  static final ColumnReader<Byte> GET_BYTE = newReader("getByte", byte.class);
  static final ColumnReader<Long> GET_LONG = newReader("getLong", long.class);
  static final ColumnReader<Double> GET_DOUBLE = newReader("getDouble", double.class);
  static final ColumnReader<Float> GET_FLOAT = newReader("getFloat", float.class);
  static final ColumnReader<BigDecimal> GET_BIG_DECIMAL =
      newReader("getBigDecimal", BigDecimal.class);
  static final ColumnReader<Boolean> GET_BOOLEAN = newReader("getBoolean", boolean.class);
  static final ColumnReader<Date> GET_DATE = newReader("getDate", Date.class);
  static final ColumnReader<Time> GET_TIME = newReader("getTime", Time.class);
  static final ColumnReader<Timestamp> GET_TIMESTAMP = newReader("getTimestamp", Timestamp.class);

  static <T> ColumnReader<T> newGetObjectInvoker(Class<T> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnReader<>(mh, returnType);
  }

  private final Map<Integer, ColumnReader<?>> readers;

  private ColumnReaders() {
    readers = createReaderCache();
  }

  @SuppressWarnings("unchecked")
  <T> ColumnReader<T> getReader(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), readers, "Unsupported SQL type: %s", typeName);
    return (ColumnReader<T>) readers.get(sqlType);
  }

  private static Map<Integer, ColumnReader<?>> createReaderCache() {
    Map<Integer, ColumnReader<?>> tmp = new HashMap<>();
    tmp.put(VARCHAR, GET_STRING);
    tmp.put(LONGVARCHAR, GET_STRING);
    tmp.put(NVARCHAR, GET_STRING);
    tmp.put(LONGNVARCHAR, GET_STRING);
    tmp.put(CHAR, GET_STRING);
    tmp.put(CLOB, GET_STRING);

    tmp.put(INTEGER, GET_INT);
    tmp.put(SMALLINT, GET_SHORT);
    tmp.put(TINYINT, GET_BYTE);
    tmp.put(BIT, GET_BYTE);
    tmp.put(DOUBLE, GET_DOUBLE);
    tmp.put(REAL, GET_DOUBLE);
    tmp.put(FLOAT, GET_FLOAT);
    tmp.put(BIGINT, GET_LONG);

    tmp.put(BOOLEAN, GET_BOOLEAN);

    tmp.put(DATE, GET_DATE);
    tmp.put(TIME, GET_TIME);

    tmp.put(TIMESTAMP, newGetObjectInvoker(LocalDateTime.class));
    tmp.put(TIMESTAMP_WITH_TIMEZONE, newGetObjectInvoker(OffsetDateTime.class));

    tmp.put(NUMERIC, GET_BIG_DECIMAL);
    tmp.put(DECIMAL, GET_BIG_DECIMAL);

    tmp.put(ARRAY, newGetObjectInvoker(Object[].class));
    return Map.copyOf(tmp);
  }

  private static <T> ColumnReader<T> newReader(String methodName, Class<T> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnReader<>(mh);
  }
}
