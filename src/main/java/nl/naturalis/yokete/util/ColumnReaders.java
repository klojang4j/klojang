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

class ColumnReaders {

  private static ColumnReaders INSTANCE;

  static ColumnReaders getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new ColumnReaders();
    }
    return INSTANCE;
  }

  static final ColumnReader GET_STRING = newReader("getString", String.class);
  static final ColumnReader GET_INT = newReader("getInt", int.class);
  static final ColumnReader GET_SHORT = newReader("getShort", short.class);
  static final ColumnReader GET_BYTE = newReader("getByte", byte.class);
  static final ColumnReader GET_LONG = newReader("getLong", long.class);
  static final ColumnReader GET_DOUBLE = newReader("getDouble", double.class);
  static final ColumnReader GET_FLOAT = newReader("getFloat", float.class);
  static final ColumnReader GET_BOOLEAN = newReader("getBoolean", boolean.class);

  static ColumnReader newGetObjectInvoker(Class<?> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class, Class.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, "getObject", mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnReader(mh, returnType);
  }

  private final Map<Integer, ColumnReader> readers;

  private ColumnReaders() {
    readers = createReaderCache();
  }

  ColumnReader getReader(int sqlType) {
    // This implicitly checks that the specified int is one of the
    // static final int constants in the java.sql.Types class
    String typeName = SQLTypeNames.getTypeName(sqlType);
    Check.that(sqlType).is(keyIn(), readers, "Unsupported SQL type: %s", typeName);
    return readers.get(sqlType);
  }

  private static Map<Integer, ColumnReader> createReaderCache() {
    Map<Integer, ColumnReader> tmp = new HashMap<>();
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
    ColumnReader invoker = newGetObjectInvoker(LocalDateTime.class);
    tmp.put(DATE, invoker);
    tmp.put(TIMESTAMP, invoker);
    tmp.put(TIMESTAMP_WITH_TIMEZONE, newGetObjectInvoker(OffsetDateTime.class));

    invoker = newGetObjectInvoker(BigDecimal.class);
    tmp.put(NUMERIC, invoker);
    tmp.put(DECIMAL, invoker);

    tmp.put(ARRAY, newGetObjectInvoker(Object[].class));
    return Map.copyOf(tmp);
  }

  private static ColumnReader newReader(String methodName, Class<?> returnType) {
    MethodType mt = MethodType.methodType(returnType, int.class);
    MethodHandle mh;
    try {
      mh = lookup().findVirtual(ResultSet.class, methodName, mt);
    } catch (NoSuchMethodException | IllegalAccessException e) {
      throw ExceptionMethods.uncheck(e);
    }
    return new ColumnReader(mh);
  }
}
