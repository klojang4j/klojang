package nl.naturalis.yokete.db.write;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Time;
import java.sql.Timestamp;
import nl.naturalis.common.ExceptionMethods;
import static java.lang.invoke.MethodHandles.lookup;

public class ColumnWriter<T> {

  static ColumnWriter<String> SET_STRING = newWriter("setString", String.class);
  static ColumnWriter<Integer> SET_INT = newWriter("setInt", int.class);
  static ColumnWriter<Double> SET_DOUBLE = newWriter("setDouble", double.class);
  static ColumnWriter<Long> SET_LONG = newWriter("setLong", long.class);
  static ColumnWriter<Float> SET_FLOAT = newWriter("setFloat", float.class);
  static ColumnWriter<Short> SET_SHORT = newWriter("setShort", short.class);
  static ColumnWriter<Byte> SET_BYTE = newWriter("setByte", byte.class);
  static ColumnWriter<Boolean> SET_BOOLEAN = newWriter("setBoolean", boolean.class);
  static ColumnWriter<BigDecimal> SET_BIG_DECIMAL = newWriter("setBigDecimal", BigDecimal.class);
  static ColumnWriter<Date> SET_DATE = newWriter("setDate", Date.class);
  static ColumnWriter<Time> SET_TIME = newWriter("setTime", Time.class);
  static ColumnWriter<Timestamp> SET_TIMESTAMP = newWriter("setTimestamp", Timestamp.class);

  private final MethodHandle method;

  private ColumnWriter(MethodHandle method) {
    this.method = method;
  }

  private static <U> ColumnWriter<U> newWriter(String methodName, Class<U> paramType) {
    MethodType mt = MethodType.methodType(void.class, int.class, paramType);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnWriter<>(mh);
  }
}
