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

  static PSSetter<String> SET_STRING = setter("setString", String.class);
  static PSSetter<Integer> SET_INT = setter("setInt", int.class);
  static PSSetter<Double> SET_DOUBLE = setter("setDouble", double.class);
  static PSSetter<Long> SET_LONG = setter("setLong", long.class);
  static PSSetter<Float> SET_FLOAT = setter("setFloat", float.class);
  static PSSetter<Short> SET_SHORT = setter("setShort", short.class);
  static PSSetter<Byte> SET_BYTE = setter("setByte", byte.class);
  static PSSetter<Boolean> SET_BOOLEAN = setter("setBoolean", boolean.class);
  static PSSetter<BigDecimal> SET_BIG_DECIMAL = setter("setBigDecimal", BigDecimal.class);
  static PSSetter<Date> SET_DATE = setter("setDate", Date.class);
  static PSSetter<Time> SET_TIME = setter("setTime", Time.class);
  static PSSetter<Timestamp> SET_TIMESTAMP = setter("setTimestamp", Timestamp.class);

  static final PSSetter<Object> SET_OBJECT_FOR_TIMESTAMP = objectSetter(TIMESTAMP);

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
    if (paramValue == null) {
      SET_STRING.method.invoke(ps, paramIndex, (String) null);
    } else if (targetSqlType == null) {
      method.invoke(ps, paramIndex, paramValue);
    } else {
      method.invoke(ps, paramIndex, paramValue, targetSqlType);
    }
  }

  private static PSSetter<Object> objectSetter(int targetSqlType) {
    MethodType mt = MethodType.methodType(void.class, int.class, Object.class, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, "setObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new PSSetter<>(mh, Object.class, targetSqlType);
  }

  private static <X> PSSetter<X> setter(String methodName, Class<X> paramType) {
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