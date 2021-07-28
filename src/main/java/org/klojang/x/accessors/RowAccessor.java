package org.klojang.x.accessors;

import org.klojang.db.Row;
import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;

public class RowAccessor implements Accessor<Row> {

  private final NameMapper nm;

  public RowAccessor(NameMapper nameMapper) {
    this.nm = nameMapper;
  }

  @Override
  public Object access(Row row, String name) throws RenderException {
    String colName = nm == null ? name : nm.map(name);
    return row.hasColumn(colName) ? row.getValue(colName) : UNDEFINED;
  }
}
