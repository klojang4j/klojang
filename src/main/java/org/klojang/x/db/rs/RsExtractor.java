package org.klojang.x.db.rs;

import java.sql.ResultSet;
import java.util.function.Function;
import nl.naturalis.common.ModulePrivate;
import static nl.naturalis.common.ObjectMethods.ifNotNull;

/**
 * Extracts a single value from a {@link ResultSet} and possibly converts it to the type of the
 * JavaBean field that the value is destined for using an {@link Adapter}.
 *
 * @author Ayco Holleman
 * @param <COLUMN_TYPE>
 * @param <FIELD_TYPE>
 */
@ModulePrivate
public class RsExtractor<COLUMN_TYPE, FIELD_TYPE> {

  private final RsMethod<COLUMN_TYPE> reader;
  private final Adapter<COLUMN_TYPE, FIELD_TYPE> adapter;

  RsExtractor(RsMethod<COLUMN_TYPE> reader) {
    this.reader = reader;
    this.adapter = null;
  }

  RsExtractor(RsMethod<COLUMN_TYPE> reader, Function<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this(reader, (x, y) -> ifNotNull(x, adapter::apply));
  }

  RsExtractor(RsMethod<COLUMN_TYPE> reader, Adapter<COLUMN_TYPE, FIELD_TYPE> adapter) {
    this.reader = reader;
    this.adapter = adapter;
  }

  @SuppressWarnings("unchecked")
  public FIELD_TYPE getValue(ResultSet rs, int columnIndex, Class<FIELD_TYPE> toType)
      throws Throwable {
    COLUMN_TYPE val = reader.call(rs, columnIndex);
    if (adapter == null) {
      return (FIELD_TYPE) val;
    }
    return adapter.adapt(val, toType);
  }
}
