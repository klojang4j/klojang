package nl.naturalis.yokete.db.read;

import java.sql.ResultSet;
import java.util.function.Function;

/**
 * Combines a {@link ColumnReader} and, possibly, an {@link Adapter} to produce a value that can be
 * set on a field within a JavaBean.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
class ValueProducer<COLUMN_TYPE, FIELD_TYPE> {

  private final ColumnReader<COLUMN_TYPE> reader;
  private final Adapter<COLUMN_TYPE, FIELD_TYPE> adapter;

  ValueProducer(ColumnReader<COLUMN_TYPE> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  ValueProducer(ColumnReader<COLUMN_TYPE> reader, Function<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this(reader, (x, y) -> adapter.apply(x));
  }

  ValueProducer(ColumnReader<COLUMN_TYPE> reader, Adapter<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this.reader = reader;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  FIELD_TYPE getValue(ResultSet rs, int columnIndex, Class<FIELD_TYPE> toType) throws Throwable {
    COLUMN_TYPE val = reader.readColumn(rs, columnIndex);
    if (adapter == null) {
      return (FIELD_TYPE) val;
    }
    return adapter.adapt(val, toType);
  }
}
