package nl.naturalis.yokete.db.read;

import java.sql.ResultSet;
import java.util.function.Function;

/**
 * Extracts a single value from a {@link ResultSet}, possibly converting it to the another type or
 * value afterwards.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
class ValueExtractor<COLUMN_TYPE, FIELD_TYPE> {

  private final ColumnReader<COLUMN_TYPE> reader;
  private final Adapter<COLUMN_TYPE, FIELD_TYPE> adapter;

  ValueExtractor(ColumnReader<COLUMN_TYPE> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  ValueExtractor(ColumnReader<COLUMN_TYPE> reader, Function<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this(reader, (x, y) -> adapter.apply(x));
  }

  ValueExtractor(ColumnReader<COLUMN_TYPE> reader, Adapter<COLUMN_TYPE, FIELD_TYPE> adapter) {
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
