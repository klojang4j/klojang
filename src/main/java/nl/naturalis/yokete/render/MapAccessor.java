package nl.naturalis.yokete.render;

import java.util.Map;
import nl.naturalis.common.check.Check;
import nl.naturalis.yokete.template.Template;

/**
 * A simple map-based {@code Accessor} implementation. The {@code Map} passed to the {@link
 * #access(Map, String) getValue} method is assumed to be a simple, two-dimensional key-value store
 * without nested structures.
 *
 * @author Ayco Holleman
 */
public class MapAccessor implements Accessor<Map<String, Object>> {

  private final Template template;
  private final NameMapper mapper;

  /**
   * Creates a {@code KeyValueAccessor} that assumes a one-to-once correspondence between template
   * variable names and map keys.
   */
  public MapAccessor(Template template) {
    this(template, NameMapper.NOOP);
  }

  /**
   * Creates a {@code KeyValueAccessor} that translates template variable names using the specified
   * operator.
   *
   * @param nameMapper
   */
  public MapAccessor(Template template, NameMapper nameMapper) {
    this.template = Check.notNull(template).ok();
    this.mapper = Check.notNull(nameMapper).ok();
  }

  @Override
  public Object access(Map<String, Object> from, String varName) throws RenderException {
    String key = Check.notNull(varName, "varName").ok(s -> mapper.map(template, s));
    return Check.notNull(from, "from").ok().getOrDefault(key, UNDEFINED);
  }

  @Override
  public Accessor<?> getAccessorForTemplate(Template nestedTemplate, Object nestedSourceData) {
    return this;
  }
}
