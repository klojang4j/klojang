package nl.naturalis.yokete.db.write;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.*;
import nl.naturalis.common.ExceptionMethods;
import static java.lang.invoke.MethodHandles.lookup;
import static java.sql.Types.*;

public class ParamWriter<PARAM_TYPE> {

  static ParamWriter<String> SET_STRING = newBinder("setString", String.class);
  static ParamWriter<Integer> SET_INT = newBinder("setInt", int.class);
  static ParamWriter<Double> SET_DOUBLE = newBinder("setDouble", double.class);
  static ParamWriter<Long> SET_LONG = newBinder("setLong", long.class);
  static ParamWriter<Float> SET_FLOAT = newBinder("setFloat", float.class);
  static ParamWriter<Short> SET_SHORT = newBinder("setShort", short.class);
  static ParamWriter<Byte> SET_BYTE = newBinder("setByte", byte.class);
  static ParamWriter<Boolean> SET_BOOLEAN = newBinder("setBoolean", boolean.class);
  static ParamWriter<BigDecimal> SET_BIG_DECIMAL = newBinder("setBigDecimal", BigDecimal.class);
  static ParamWriter<Date> SET_DATE = newBinder("setDate", Date.class);
  static ParamWriter<Time> SET_TIME = newBinder("setTime", Time.class);
  static ParamWriter<Timestamp> SET_TIMESTAMP = newBinder("setTimestamp", Timestamp.class);

  static final ParamWriter<Object> SET_OBJECT_FOR_TIMESTAMP = newObjectBinder(TIMESTAMP);

  private final MethodHandle method;
  private final Class<PARAM_TYPE> paramType;
  private final Integer targetSqlType;

  private ParamWriter(MethodHandle method, Class<PARAM_TYPE> paramType) {
    this.method = method;
    this.paramType = paramType;
    this.targetSqlType = null;
  }

  private ParamWriter(MethodHandle method, Class<PARAM_TYPE> paramType, int targetSqlType) {
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

  private static ParamWriter<Object> newObjectBinder(int targetSqlType) {
    MethodType mt = MethodType.methodType(void.class, int.class, Object.class, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, "setObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ParamWriter<>(mh, Object.class, targetSqlType);
  }

  private static <X> ParamWriter<X> newBinder(String methodName, Class<X> paramType) {
    MethodType mt = MethodType.methodType(void.class, int.class, paramType);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(PreparedStatement.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ParamWriter<>(mh, paramType);
  }
}
