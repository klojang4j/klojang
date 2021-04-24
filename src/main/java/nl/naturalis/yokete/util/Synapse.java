package nl.naturalis.yokete.util;

import java.sql.ResultSet;
import java.util.function.Function;

/*
 * Captures the point at which a value finally jumps from the ResultSet to the bean (hence the
 * name).
 *
 */
class Synapse<COLUMN_TYPE, TARGET_TYPE> {

  private final ColumnReader<COLUMN_TYPE> reader;
  private final Adapter<COLUMN_TYPE, TARGET_TYPE> adapter;

  Synapse(ColumnReader<COLUMN_TYPE> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  Synapse(ColumnReader<COLUMN_TYPE> reader, Function<COLUMN_TYPE, TARGET_TYPE> adapter) {
    this(reader, (x, y) -> adapter.apply(x));
  }

  Synapse(ColumnReader<COLUMN_TYPE> reader, Adapter<COLUMN_TYPE, TARGET_TYPE> adapter) {
    this.reader = reader;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  TARGET_TYPE fire(ResultSet rs, int columnIndex, Class<TARGET_TYPE> targetType) throws Throwable {
    COLUMN_TYPE val = reader.readColumn(rs, columnIndex);
    if (adapter == null) {
      return (TARGET_TYPE) val;
    }
    return adapter.adapt(val, targetType);
  }
}
