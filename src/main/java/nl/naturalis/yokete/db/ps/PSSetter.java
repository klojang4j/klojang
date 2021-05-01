package nl.naturalis.yokete.db.ps;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.*;
import nl.naturalis.common.ExceptionMethods;
import static java.lang.invoke.MethodHandles.lookup;
import static java.sql.Types.*;

/**
 * Represents one of the {@code setXXX} methods of {@link PreparedStatement} and contains a {@link
 * MethodHandle} for calling it.
 *
 * @author Ayco Holleman
 * @param <PARAM_TYPE>
 */
public class PSSetter<PARAM_TYPE> {

  static PSSetter<String> SET_STRING = newBinder("setString", String.class);
  static PSSetter<Integer> SET_INT = newBinder("setInt", int.class);
  static PSSetter<Double> SET_DOUBLE = newBinder("setDouble", double.class);
  static PSSetter<Long> SET_LONG = newBinder("setLong", long.class);
  static PSSetter<Float> SET_FLOAT = newBinder("setFloat", float.class);
  static PSSetter<Short> SET_SHORT = newBinder("setShort", short.class);
  static PSSetter<Byte> SET_BYTE = newBinder("setByte", byte.class);
  static PSSetter<Boolean> SET_BOOLEAN = newBinder("setBoolean", boolean.class);
  static PSSetter<BigDecimal> SET_BIG_DECIMAL = newBinder("setBigDecimal", BigDecimal.class);
  static PSSetter<Date> SET_DATE = newBinder("setDate", Date.class);
  static PSSetter<Time> SET_TIME = newBinder("setTime", Time.class);
  static PSSetter<Timestamp> SET_TIMESTAMP = newBinder("setTimestamp", Timestamp.class);

  static final PSSetter<Object> SET_OBJECT_FOR_TIMESTAMP = newObjectBinder(TIMESTAMP);

  private final MethodHandle method;
  private final Class<PARAM_TYPE> paramType;
  private final Integer targetSqlType;

  private PSSetter(MethodHandle method, Class<PARAM_TYPE> paramType) {
    this.method = method;
    this.paramType = paramType;
    this.targetSqlType = null;
  }

  private PSSetter(MethodHandle method, Class<PARAM_TYPE> paramType, int targetSqlType) {
    this.method = method;
    this.paramType = paramType;
    this.targetSqlType = targetSqlType;
  }

  Class<PARAM_TYPE> getParamType() {
    return paramType;
  }

  void bindValue(PreparedStatement ps, int paramIndex, PARAM_TYPE paramValue) throws Throwable {
    if (targetSqlType == null) {
      method.invoke(ps, paramIndex, paramValue);
    } else {
      method.invoke(ps, paramIndex, paramValue, targetSqlType);
    }
  }

  private static PSSetter<Object> newObjectBinder(int targetSqlType) {
    MethodType mt = MethodType.methodType(void.class, int.class, Object.class, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, "setObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PSSetter<>(mh, Object.class, targetSqlType);
  }

  private static <X> PSSetter<X> newBinder(String methodName, Class<X> paramType) {
    MethodType mt = MethodType.methodType(void.class, int.class, paramType);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PSSetter<>(mh, paramType);
  }
}
