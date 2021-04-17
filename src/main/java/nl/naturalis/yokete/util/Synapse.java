package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.util.function.Function;

/*
 * Captures the point at which a value finally jumps from the ResultSet to the bean (hence the
 * name).
 *
 */
class Synapse {

  private final ColumnReader reader;
  private final Function<Object, Object> adapter;

  Synapse(ColumnReader getter) {
    this(getter, Function.identity());
  }

  Synapse(ColumnReader getter, Function<Object, Object> adapter) {
    this.reader = getter;
    this.adapter = adapter;
  }

  Object fire(ResultSet rs, int columnIndex) throws Throwable {
    MethodHandle method = reader.getMethod();
    Class<?> clazz = reader.getClassArgument();
    Object resultSetValue;
    if (clazz == null) {
      resultSetValue = method.invoke(rs, columnIndex);
    } else {
      resultSetValue = method.invoke(rs, columnIndex, clazz);
    }
    if (adapter == null) {
      return resultSetValue;
    }
    return adapter.apply(resultSetValue);
  }
}
