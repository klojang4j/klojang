package org.klojang.x.accessors;

import org.klojang.db.Row;
import org.klojang.render.Accessor;
import org.klojang.render.NameMapper;
import org.klojang.render.RenderException;
import nl.naturalis.common.check.Check;

public class RowAccessor implements Accessor<Row> {

  private final NameMapper mapper;

  public RowAccessor(NameMapper nameMapper) {
    this.mapper = Check.notNull(nameMapper).ok();
  }

  @Override
  public Object access(Row row, String name) throws RenderException {
    String colName = mapper.map(name);
    if (!row.hasColumn(colName)) {
      return UNDEFINED;
    }
    return row.get(colName);
  }
}
