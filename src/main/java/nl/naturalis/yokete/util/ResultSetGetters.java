package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import nl.naturalis.common.ExceptionMethods;
import nl.naturalis.common.check.Check;
import static java.lang.invoke.MethodHandles.lookup;
import static java.sql.Types.*;
import static nl.naturalis.common.check.CommonChecks.keyIn;

class ResultSetGetters {

  private static ResultSetGetters INSTANCE;

  static ResultSetGetters getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ResultSetGetters();
    }
    return INSTANCE;
  }

  static final ResultSetGetter GET_STRING = newInvoker("getString", String.class);
  static final ResultSetGetter GET_INT = newInvoker("getInt", int.class);
  static final ResultSetGetter GET_SHORT = newInvoker("getShort", short.class);
  static final ResultSetGetter GET_BYTE = newInvoker("getByte", byte.class);
  static final ResultSetGetter GET_LONG = newInvoker("getLong", long.class);
  static final ResultSetGetter GET_DOUBLE = newInvoker("getDouble", double.class);
  static final ResultSetGetter GET_FLOAT = newInvoker("getFloat", float.class);
  static final ResultSetGetter GET_BOOLEAN = newInvoker("getBoolean", boolean.class);

  static ResultSetGetter newGetObjectInvoker(Class<?> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ResultSetGetter(mh, returnType);
  }

  private final Map<Integer, ResultSetGetter> getters;

  private ResultSetGetters() {
    getters = createGetterCache();
  }

  ResultSetGetter getGetter(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), getters, "Unsupported SQL type: %s", typeName);
    return getters.get(sqlType);
  }

  private static Map<Integer, ResultSetGetter> createGetterCache() {
    Map<Integer, ResultSetGetter> tmp = new HashMap<>();
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

    tmp.put(TIME, newGetObjectInvoker(LocalTime.class));
    ResultSetGetter invoker = newGetObjectInvoker(LocalDateTime.class);
    tmp.put(DATE, invoker);
    tmp.put(TIMESTAMP, invoker);
    tmp.put(TIMESTAMP_WITH_TIMEZONE, newGetObjectInvoker(OffsetDateTime.class));

    invoker = newGetObjectInvoker(BigDecimal.class);
    tmp.put(NUMERIC, invoker);
    tmp.put(DECIMAL, invoker);

    tmp.put(ARRAY, newGetObjectInvoker(Object[].class));
    return Map.copyOf(tmp);
  }

  private static ResultSetGetter newInvoker(String methodName, Class<?> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ResultSetGetter(mh);
  }
}
