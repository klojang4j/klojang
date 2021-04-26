package nl.naturalis.yokete.db;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import nl.naturalis.common.ExceptionMethods;
import static java.lang.invoke.MethodHandles.lookup;

class ColumnReader<COLUMN_TYPE> {

  static final ColumnReader<String> GET_STRING = newReader("getString", String.class);
  static final ColumnReader<Integer> GET_INT = newReader("getInt", int.class);
  static final ColumnReader<Float> GET_FLOAT = newReader("getFloat", float.class);
  static final ColumnReader<Double> GET_DOUBLE = newReader("getDouble", double.class);
  static final ColumnReader<Long> GET_LONG = newReader("getLong", long.class);
  static final ColumnReader<Short> GET_SHORT = newReader("getShort", short.class);
  static final ColumnReader<Byte> GET_BYTE = newReader("getByte", byte.class);
  static final ColumnReader<Boolean> GET_BOOLEAN = newReader("getBoolean", boolean.class);
  static final ColumnReader<Date> GET_DATE = newReader("getDate", Date.class);
  static final ColumnReader<Time> GET_TIME = newReader("getTime", Time.class);
  static final ColumnReader<Timestamp> GET_TIMESTAMP = newReader("getTimestamp", Timestamp.class);
  static final ColumnReader<BigDecimal> GET_BIG_DECIMAL =
      newReader("getBigDecimal", BigDecimal.class);

  // Invokes <T> ResultSet.getObject(columnIndex, Class<T>)
  static <T> ColumnReader<T> newObjectReader(Class<T> returnType) {
    MethodType mt = MethodType.methodType(Object.class, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnReader<>(mh, returnType);
  }

  private final MethodHandle method;

  // If this is ColumnReader invokes ResultSet.getObject(int, Class), then
  // classArg will be the Class object passed in as the second argument to
  // getObject. In anyother case classArg will be null.
  private final Class<?> classArg;

  private ColumnReader(MethodHandle method) {
    this(method, null);
  }

  private ColumnReader(MethodHandle method, Class<?> classArg) {
    this.method = method;
    this.classArg = classArg;
  }

  COLUMN_TYPE readColumn(ResultSet rs, int columnIndex) throws Throwable {
    COLUMN_TYPE val;
    if (classArg == null) {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex);
    } else {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex, classArg);
    }
    return rs.wasNull() ? null : val;
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
