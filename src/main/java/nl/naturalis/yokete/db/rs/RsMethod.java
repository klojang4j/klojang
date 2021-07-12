package nl.naturalis.yokete.db.rs;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import nl.naturalis.common.ExceptionMethods;
import static java.lang.invoke.MethodHandles.lookup;

/**
 * Represents one of the {@code getXXX} methods of {@code ResultSet}. Note that in spite of the name
 * this class is not built around Java reflection but rather relies on {@code java.lang.invoke}.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 */
class RsMethod<COLUMN_TYPE> {

  static final RsMethod<String> GET_STRING = getter("getString", String.class);
  static final RsMethod<Integer> GET_INT = getter("getInt", int.class);
  static final RsMethod<Float> GET_FLOAT = getter("getFloat", float.class);
  static final RsMethod<Double> GET_DOUBLE = getter("getDouble", double.class);
  static final RsMethod<Long> GET_LONG = getter("getLong", long.class);
  static final RsMethod<Short> GET_SHORT = getter("getShort", short.class);
  static final RsMethod<Byte> GET_BYTE = getter("getByte", byte.class);
  static final RsMethod<Boolean> GET_BOOLEAN = getter("getBoolean", boolean.class);
  static final RsMethod<Date> GET_DATE = getter("getDate", Date.class);
  static final RsMethod<Time> GET_TIME = getter("getTime", Time.class);
  static final RsMethod<Timestamp> GET_TIMESTAMP = getter("getTimestamp", Timestamp.class);
  static final RsMethod<BigDecimal> GET_BIG_DECIMAL = getter("getBigDecimal", BigDecimal.class);

  // Invokes <T> ResultSet.getObject(columnIndex, Class<T>)
  static <T> RsMethod<T> objectGetter(Class<T> returnType) {
    MethodType mt = MethodType.methodType(Object.class, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new RsMethod<>(mh, returnType);
  }

  private final MethodHandle method;

  // If this is ColumnReader invokes ResultSet.getObject(int, Class), then
  // classArg will be the Class object passed in as the second argument to
  // getObject. In anyother case classArg will be null.
  private final Class<?> classArg;

  private RsMethod(MethodHandle method) {
    this(method, null);
  }

  private RsMethod(MethodHandle method, Class<?> classArg) {
    this.method = method;
    this.classArg = classArg;
  }

  public COLUMN_TYPE call(ResultSet rs, int columnIndex) throws Throwable {
    COLUMN_TYPE val;
    if (classArg == null) {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex);
    } else {
      val = (COLUMN_TYPE) method.invoke(rs, columnIndex, classArg);
    }
    return rs.wasNull() ? null : val;
  }

  private static <T> RsMethod<T> getter(String methodName, Class<T> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new RsMethod<>(mh);
  }
}
