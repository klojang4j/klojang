package nl.naturalis.yokete.util;

import java.lang.invoke.MethodHandle;
import java.sql.ResultSet;
import java.util.function.Function;

/*
 * Captures the point at which a value finally jumps from the ResultSet to the bean (hence the
 * name).
 *
 */
class Synapse<T, R> {

  private final ColumnReader<T> reader;
  private final Adapter<T, R> adapter;

  Synapse(ColumnReader<T> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  Synapse(ColumnReader<T> reader, Function<T, R> adapter) {
    this(reader, (x, y, z) -> adapter.apply(x));
  }

  Synapse(ColumnReader<T> reader, Adapter<T, R> adapter) {
    this.reader = reader;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  R fire(ResultSet rs, int columnIndex, Class<?> targetType, ResultSetReaderConfig cfg)
      throws Throwable {
    MethodHandle method = reader.getMethod();
    Class<?> clazz = reader.getClassArgument();
    T val;
    if (clazz == null) {
      val = (T) method.invoke(rs, columnIndex);
    } else {
      val = (T) method.invoke(rs, columnIndex, clazz);
    }
    if (adapter == null) {
      return (R) val;
    }
    return adapter.adapt(val, targetType, cfg);
  }
}
