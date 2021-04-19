package nl.naturalis.yokete.accessors;

import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.render.Accessor;
import nl.naturalis.yokete.render.RenderException;
import nl.naturalis.yokete.template.Template;
import nl.naturalis.yokete.util.NameMapper;
import nl.naturalis.yokete.util.Row;

public class RowAccessor implements Accessor<Row> {

  private final Template template;
  private final NameMapper mapper;

  /**
   * Creates a {@code KeyValueAccessor} that assumes a one-to-once correspondence between template
   * variable names and map keys.
   */
  public RowAccessor() {
    this.template = null;
    this.mapper = null;
  }

  /**
   * Creates a {@code KeyValueAccessor} that translates template variable names using the specified
   * operator.
   *
   * @param nameMapper
   */
  public RowAccessor(Template template, NameMapper nameMapper) {
    this.template = Check.notNull(template).ok();
    this.mapper = Check.notNull(nameMapper).ok();
  }

  @Override
  public Object access(Row row, String name) throws RenderException {
    String colName = Check.notNull(name, "name").ok();
    if (mapper != null) {
      colName = mapper.map(template, colName);
    }
    if (!row.hasColumn(colName)) {
      return UNDEFINED;
    }
    return Check.notNull(row, "row").ok().get(colName);
  }
}
