package nl.naturalis.yokete.db.rs;

import java.sql.ResultSet;
import java.util.function.Function;
import nl.naturalis.common.ModulePrivate;

/**
 * Extracts a single value from a {@link ResultSet} and possibly converts it to the type of the
 * JavaBean field that the value is destined for.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
@ModulePrivate
public class Emitter<COLUMN_TYPE, FIELD_TYPE> {

  private final RSGetter<COLUMN_TYPE> reader;
  private final Adapter<COLUMN_TYPE, FIELD_TYPE> adapter;

  Emitter(RSGetter<COLUMN_TYPE> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  Emitter(RSGetter<COLUMN_TYPE> reader, Function<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this(reader, (x, y) -> adapter.apply(x));
  }

  Emitter(RSGetter<COLUMN_TYPE> reader, Adapter<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this.reader = reader;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  public FIELD_TYPE getValue(ResultSet rs, int columnIndex, Class<FIELD_TYPE> toType)
      throws Throwable {
    COLUMN_TYPE val = reader.readColumn(rs, columnIndex);
    if (adapter == null) {
      return (FIELD_TYPE) val;
    }
    return adapter.adapt(val, toType);
  }
}
